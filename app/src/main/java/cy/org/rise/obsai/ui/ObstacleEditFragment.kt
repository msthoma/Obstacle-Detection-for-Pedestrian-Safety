package cy.org.rise.obsai.ui


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
import java.io.File
import java.io.IOException

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
    private val args: ObstacleEditFragmentArgs by navArgs()

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

        obstacle_image_view.setOnClickListener {
            val dialog = MaterialDialog(requireContext()).customView(
                R.layout.type_selection_dialog,
                scrollable = true
            )

            val customDialogView = dialog.getCustomView() as ConstraintLayout

            val radioGroup = customDialogView.findViewById<RadioGroup>(R.id.types_radio_group)

            val obsTypeArray = shuffledTypeList()

            // initially populate with 5 most likely types, as determined by the CNN
            populateRadioGroupTypeList(radioGroup, obsTypeArray, 5)

            // show more button is clicked
            customDialogView.findViewById<TextView>(R.id.button_show_more_types)
                .setOnClickListener { showMoreButton ->
                    // show all possible types
                    // also add empty radio button at the end of list, for custom editText input
                    populateRadioGroupTypeList(
                        radioGroup, obsTypeArray,
                        addEmptyRadioButtonAtBottom = true
                    )

                    // hide show more button
                    showMoreButton.visibility = View.GONE

                    // show custom editText
                    val customTypeLL =
                        customDialogView.findViewById<LinearLayout>(R.id.type_custom_input_layout)
                    customTypeLL.visibility = View.VISIBLE

                    val customEditText =
                        customTypeLL.findViewById<EditText>(R.id.type_custom_input_edittext)

                    val lastEmptyRadioButton =
                        radioGroup.getChildAt(obsTypeArray.size) as RadioButton

                    customEditText.setOnFocusChangeListener { v, hasFocus ->
                        Log.d(TAG(), "customEditText hasFocus $hasFocus")
                        if (hasFocus) lastEmptyRadioButton.isChecked = true
                    }
                    customEditText.setOnClickListener {
                        Log.d(TAG(), "customEditText onclick")
                        lastEmptyRadioButton.isChecked = true
                    }
                    radioGroup.setOnCheckedChangeListener { _, checkedId ->
                        if (checkedId != lastEmptyRadioButton.id) {
                            customEditText.clearFocus()
                            customEditText.hideKeyboard()
                        } else {
                            // when checkedId == lastEmptyRadioButton.id it means editText should
                            // be selected
                            customEditText.requestFocus()
                            customEditText.showKeyboard()
                        }
                    }
                }

            dialog.apply {
                title(text = getString(R.string.dialog_select_type_title))
                positiveButton(text = getString(R.string.dialog_OK_button))
                lifecycleOwner(viewLifecycleOwner)
                dialog.show()
            }
        }

        // Set obstacle label choices in autoCompleteTextView
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.obstacle_types_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { arrayAdapter ->
            select_obstacle_type_edit_text.setAdapter(arrayAdapter)

            // When coming from crop fragment, if the type was already set, restore its value
            if (currentObstacle.obstacleType != "") {
                select_obstacle_type_edit_text.setText(
                    arrayAdapter.getItem(arrayAdapter.getPosition(currentObstacle.obstacleType))
                        .toString(), false
                )
            }
        }

        // Clear any error message present when view is clicked
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
                    // Move camera to appropriate position
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
        addEmptyRadioButtonAtBottom: Boolean = false
    ) {
        // make sure any previous entries are removed
        radioGroup.removeAllViews()

        // create view params for individual Radio Buttons
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
//        val margin = 40
        layoutParams.bottomMargin = 12

        // populate radio group, respecting any limits on number of items required
        obsTypeArray.sliceArray(IntRange(0, listLimit - 1)).forEach { obsType ->
            radioGroup.addView(RadioButton(context).also { rb ->
                rb.text = obsType
                rb.layoutParams = layoutParams
            })
        }

        if (addEmptyRadioButtonAtBottom) {
            radioGroup.addView(RadioButton(context).apply {
                // empty text
                this.layoutParams = layoutParams
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
