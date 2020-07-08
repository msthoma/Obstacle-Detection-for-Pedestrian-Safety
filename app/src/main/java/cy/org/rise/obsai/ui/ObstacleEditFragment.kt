package cy.org.rise.obsai.ui


import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import cy.org.rise.obsai.R
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.InjectorUtils
import cy.org.rise.obsai.utils.TAG
import cy.org.rise.obsai.utils.hideKeyboard
import cy.org.rise.obsai.utils.showKeyboard
import kotlinx.android.synthetic.main.fragment_obstacle_edit.*
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException
import kotlin.properties.Delegates

/**
 * Fragment that displays the recently photographed obstacle, shows its position on the map,
 * allows editing of the photo, and prompts the user to select the obstacle's type.
 *
 * The process of displaying the Google map is quite complicated, see documentation
 * [here](https://developers.google.com/maps/documentation/android-sdk/map).
 *
 * The API key for Google Maps is defined in the AndroidManifest, and needs to be obtained from
 * Google cloud first (see documentation linked above).
 */
class ObstacleEditFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var currentObstacle: Obstacle
    private lateinit var typeEditText: EditText
    private val args: ObstacleEditFragmentArgs by navArgs()

    // TFLite related vars
    private val IMAGE_MEAN = 0.0f
    private val IMAGE_STD = 255.0f
    private val PROBABILITY_MEAN = 0.0f
    private val PROBABILITY_STD = 1.0f
    private lateinit var tflite: Interpreter
    private lateinit var rgbBitmap: Bitmap
    private lateinit var inputImageBuffer: TensorImage
    private lateinit var outputProbabilityBuffer: TensorBuffer
    private lateinit var probabilityProcessor: TensorProcessor
    private lateinit var labels: List<String>
    private var imageSizeX by Delegates.notNull<Int>()
    private var imageSizeY by Delegates.notNull<Int>()
    private val yuvBytes = arrayOfNulls<ByteArray>(3)
    private var rgbBytes: IntArray? = null

    private val viewModel: ObstacleViewModel by viewModels {
        InjectorUtils.provideObstacleViewModelFactory(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Set toolbar menu
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_obstacle_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Get current obstacle
        currentObstacle = args.currentObstacle

        // Try to get the file from the arguments passed from the camera fragment
        val photoFile: File? = try {
            File(currentObstacle.photoPath)
        } catch (ex: IllegalArgumentException) {
            Log.e(TAG(), "Error getting image file")
            null
        }

        // If the photo file exists, set it in image view
        photoFile?.also {
            Picasso.get()
                .load(photoFile)
                // If cache is enabled, the photo won't refresh after it's cropped
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                // Resize to screen width, and automatically determine available height
                .resize(resources.displayMetrics.widthPixels, 0)
                .into(obstacle_image_view)
        }

        // Setup all required for TFLite
        val tfliteModel = FileUtil.loadMappedFile(requireContext(), "cnn128RGB.tflite")
        tflite = Interpreter(tfliteModel, Interpreter.Options())

        labels = FileUtil.loadLabels(requireContext(), "cnnRGB_labels.txt")

        val imageTensorIndex = 0
        val imageShape = tflite.getInputTensor(imageTensorIndex).shape()
        imageSizeY = imageShape[1]
        imageSizeX = imageShape[2]

        val imageDataType = tflite.getInputTensor(imageTensorIndex).dataType()
        val probabilityTensorIndex = 0
        val probabilityShape = tflite.getOutputTensor(probabilityTensorIndex).shape()
        val probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType()

        inputImageBuffer = TensorImage(imageDataType)

        outputProbabilityBuffer =
            TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)

        probabilityProcessor =
            TensorProcessor.Builder().add(NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD)).build()


        // Setup type selection dialog
        typeEditText = select_obstacle_type_edit_text

        // Create shuffled type array mimicking results from CNN
        val obsTypeArray = shuffledTypeList()

        // Show custom dialog for obstacle type selection
        typeEditText.setOnClickListener {
            val dialog = MaterialDialog(requireContext()).customView(
                R.layout.type_selection_dialog,
                scrollable = true
            )

            // get references to views on dialog that are of interest
            val customDialogView = dialog.getCustomView() as ConstraintLayout

            val radioGroup = customDialogView.findViewById<RadioGroup>(R.id.types_radio_group)

            // initially only show 5 most likely types, as determined by the CNN (hide the rest)
            populateRadioGroupTypeList(radioGroup, obsTypeArray, 5)

            // get references to last radio button and customEditText
            val customEditText =
                customDialogView.findViewById<EditText>(R.id.type_custom_input_edittext)
            // when radio buttons are added, they are given IDs in the form 1000 + index in obsTypeArray
            val lastEmptyRadioButton =
                radioGroup.findViewById<RadioButton>(1000 + obsTypeArray.size + 1)

            // show more button is clicked
            customDialogView.findViewById<TextView>(R.id.button_show_more_types)
                .setOnClickListener { showMoreButton ->
                    // reveal all possible types
                    for (i in 0..obsTypeArray.size + 1)
                        radioGroup.findViewById<RadioButton>(1000 + i)?.visibility = View.VISIBLE

                    // hide show more button
                    showMoreButton.visibility = View.GONE

                    // show custom editText
                    customEditText.visibility = View.VISIBLE
                }

            // set listeners to all to regulate their behaviour
            customEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) lastEmptyRadioButton.isChecked = true
            }
            customEditText.setOnClickListener {
                lastEmptyRadioButton.isChecked = true
            }
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId != lastEmptyRadioButton.id) {
                    // capture current selection
                    radioGroup.findViewById<RadioButton>(checkedId).also { rb ->
                        currentObstacle.obstacleType = rb.text.toString()
                        Log.d(TAG(), "current selection ${currentObstacle.obstacleType}")
                    }
                    // clear any focus on customEditText
                    customEditText.apply {
                        clearFocus()
                        hideKeyboard()
                    }
                } else {
                    // when checkedId == lastEmptyRadioButton.id it means editText should
                    // be selected
                    customEditText.apply {
                        requestFocus()
                        showKeyboard()
                    }
                }
            }

            // finally, display the dialog
            dialog.apply {
                noAutoDismiss()
                title(text = getString(R.string.dialog_select_type_title))
                positiveButton(R.string.dialog_OK_button) {
                    val checkedId = radioGroup.checkedRadioButtonId
                    if (checkedId != -1) {
                        Log.d(
                            TAG(), "current selection ${radioGroup.findViewById<RadioButton>
                                (checkedId).text}"
                        )
                        if (checkedId == lastEmptyRadioButton.id) {
                            // customEditText selected, make sure text is not too short or empty
                            val currentText = customEditText.editableText.toString()
                            if (currentText.length > 2) {
                                Log.d(TAG(), "current custom text $currentText")
                                currentObstacle.obstacleType = currentText
                                typeEditText.setText(currentObstacle.obstacleType)
                                dismiss()
                            } else {
                                // text provided too short/empty
                                Toast.makeText(
                                    context, "Please provide a valid type", Toast
                                        .LENGTH_SHORT
                                ).show()
                                // focus on editText
                                lastEmptyRadioButton.parent
                                    .requestChildFocus(lastEmptyRadioButton, lastEmptyRadioButton)
                                // TODO change to material.textfield.TextInputEditText to show
                                //  errors
                            }
                        } else {
                            // selection is from predefined list, good to go
                            typeEditText.setText(currentObstacle.obstacleType)
                            dismiss()
                        }
                    } else {
                        // -1 means none selected
                        Toast.makeText(context, "Please make a selection", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                negativeButton(R.string.dialog_cancel_button) { dismiss() }
                lifecycleOwner(viewLifecycleOwner)
                dialog.show()
            }
        }

        // Clear any error message present editText value changes
        select_obstacle_type_edit_text.addTextChangedListener {
            select_obstacle_type_layout.error = null
        }

        button_edit_photo.setOnClickListener {
            // Save type before going to the crop fragment
            currentObstacle.obstacleType = select_obstacle_type_edit_text.text.toString()
            findNavController().navigate(
                ObstacleEditFragmentDirections.actionObstacleEditFragmentToCropFragment(
                    currentObstacle
                )
            )
        }

        // Setup map view
        mapView = map
        mapView.onCreate(null) // TODO fix, here a mapViewBundle should be passed instead of null
        mapView.getMapAsync { googleMap ->

            val obstaclePosition = currentObstacle.getLocationAsLatLong()

            val CYPRUS = LatLngBounds(
                LatLng(34.520142, 32.186723), // Southwest corner
                LatLng(35.738372, 34.644546) // Northeast corner
            )

            googleMap.apply {
                // Add marker and set map camera position
                if (obstaclePosition.latitude != 0.0) {
                    // Add marker indicating the obstacle, if location provided is not 0, 0
                    addMarker(MarkerOptions().position(obstaclePosition).title("Marker"))
                    // Move map camera to above obstacle position
                    moveCamera(CameraUpdateFactory.newLatLngZoom(obstaclePosition, 12f))
                } else {
                    // In case location is empty, move camera above the general area of Nicosia
                    moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.169933, 33.361071), 12f))
                }

                // Add map boundaries
                setLatLngBoundsForCameraTarget(CYPRUS)

                // Set min zoom, so user cannot zoom out too much (1 is world, 20 buildings)
                setMinZoomPreference(7.5f)

                // Listen for long clicks on map, which allows user to change location manually
                setOnMapLongClickListener { latLng ->
                    // TODO add indication that long click changes position (with overlay?)
                    clear()
                    addMarker(MarkerOptions().position(latLng))

                    // Save location indicated by user
                    // TODO here the altitude should be updated as well, does maps provided it
                    //  somewhere?
                    currentObstacle.setLocationFromLatLong(latLng)
                }
            }

//            TODO when user clicks my location button, marker should move to location provided
//             by GPS, but fragment must first be able to get current position, perhaps by moving
//             location tracking logic to view model
//             see https://stackoverflow.com/questions/57961791/how-to-use-locationlistener-in-mvvm
//             https://stackoverflow.com/questions/47619739/how-to-track-current-location-in-android-with-new-architecture-components
//            googleMap.isMyLocationEnabled = true
//            googleMap.setOnMyLocationButtonClickListener {
//                see here https://developers.google.com/maps/documentation/android-sdk/location#my-location
//            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // display discard confirmation dialog on back press, and also on up press (when up
            // is pressed, overriding onSupportNavigateUp in MainActivity enables re-routing of
            // up here)
            displayDiscardConfirmationDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_confirm_edit_obstacle -> {
                // Make sure the user has chosen an obstacle type before submitting
                if (allRequiredInfoEntered()) {
                    // Save obstacle type
                    currentObstacle.obstacleType = select_obstacle_type_edit_text.text.toString()
                    viewModel.insertObstacle(currentObstacle)
                    findNavController().navigate(
                        R.id.action_obstacleEditFragment_to_obstacleListFragment
                    )
                }
                true
            }
            R.id.action_cancel_edit_obstacle -> {
                displayDiscardConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayDiscardConfirmationDialog() {
        // shows confirmation dialog in the cases of back press, up press, menu cancel option
        MaterialDialog(requireContext()).show {
            title(R.string.dialog_discard_title)
            message(R.string.dialog_discard_msg)
            icon(R.drawable.ic_warning_black_24dp)

            positiveButton(R.string.dialog_discard_positive) {
                // first delete photo of obstacle that was taken
                try {
                    File(currentObstacle.photoPath).delete()
                } catch (ex: IOException) {
                    Log.e(TAG(), "Error deleting obstacle photo", ex)
                }

                // by navigating back with the action below, the back stack is popped up to the
                // list fragment, and so a back press there does not return the user back here
                findNavController().navigate(
                    R.id.action_obstacleEditFragment_to_obstacleListFragment
                )
            }

            negativeButton(R.string.dialog_cancel_button) { dismiss() }
            lifecycleOwner(viewLifecycleOwner)
        }
    }

    private fun allRequiredInfoEntered(): Boolean {
        var allEntered = true
        // Check if type was selected
        if (select_obstacle_type_edit_text.text.toString() == "") {
            allEntered = false
            select_obstacle_type_layout.error =
                getString(R.string.error_type_not_selected)
        }
        // Check if location was selected
        if (currentObstacle.location.latitude == 0.0 || currentObstacle.location.longitude == 0.0) {
            allEntered = false
            Toast.makeText(
                requireContext(), getString(R.string.toast_location_required),
                Toast.LENGTH_LONG
            ).show()
        }
        return allEntered
    }

    private fun populateRadioGroupTypeList(
        radioGroup: RadioGroup, obsTypeArray: Array<String>,
        listLimit: Int = obsTypeArray.size,
        addEmptyRadioButtonAtBottom: Boolean = true
    ) {
        // make sure any previous entries are removed
        radioGroup.removeAllViews()

        // create view params for individual Radio Buttons
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.bottomMargin = 12

        // populate radio group, respecting any limits on number of items required
        obsTypeArray.forEachIndexed { i, obsType ->
            radioGroup.addView(RadioButton(context).also { rb ->
                rb.id = 1000 + i
                rb.text = obsType
                rb.layoutParams = layoutParams
                if (i >= listLimit) rb.visibility = View.GONE
            })
        }

        if (addEmptyRadioButtonAtBottom) {
            radioGroup.addView(RadioButton(context).also { rb ->
                // empty text
                rb.id = 1000 + obsTypeArray.size + 1
                rb.layoutParams = layoutParams
                rb.visibility = View.GONE
            })
        }
    }

    private fun shuffledTypeList(): Array<String> =
        resources.getStringArray(R.array.obstacle_types_array).toList().shuffled().toTypedArray()

    /**
     * Override of function required by map view.
     */
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    /**
     * Override of function required by map view.
     */
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    /**
     * Override of function required by map view.
     */
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    /**
     * Override of function required by map view.
     */
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    /**
     * Override of function required by map view.
     */
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    /**
     * Override of function required by map view.
     */
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
