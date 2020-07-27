package cy.org.rise.obsai.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.assent.Permission
import com.afollestad.assent.isAllGranted
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import cy.org.rise.obsai.R
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.Constants.CITY_ZOOM_LEVEL
import cy.org.rise.obsai.utils.Constants.CYPRUS
import cy.org.rise.obsai.utils.Constants.DEFAULT_ZOOM_LEVEL
import cy.org.rise.obsai.utils.Constants.MIN_ZOOM_LEVEL
import cy.org.rise.obsai.utils.InjectorUtils
import cy.org.rise.obsai.utils.TAG
import cy.org.rise.obsai.utils.hideKeyboard
import cy.org.rise.obsai.utils.showKeyboard
import kotlinx.android.synthetic.main.fragment_obstacle_edit.*
import kotlinx.android.synthetic.main.type_selection_dialog.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    private lateinit var cnnResults: Array<String>
    private lateinit var currentObstacle: Obstacle
    private lateinit var fabSubmit: ExtendedFloatingActionButton
    private lateinit var mapView: MapView
    private lateinit var typeEditText: EditText
    private val args: ObstacleEditFragmentArgs by navArgs()
    private var analysisIndicatorNotShown = true
    private var fragCreationTime by Delegates.notNull<Long>()
    private var locationNotManuallyEdited = true

    private val viewModel: ObstacleViewModel by viewModels {
        InjectorUtils.provideObstacleViewModelFactory(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Set toolbar menu
        setHasOptionsMenu(true)
        // save create time
        fragCreationTime = System.currentTimeMillis()

        return inflater.inflate(R.layout.fragment_obstacle_edit, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Get views of interest
        typeEditText = select_obstacle_type_edit_text
        fabSubmit = fab_submit

        // Get current obstacle
        currentObstacle = args.currentObstacle
        // If type is already available, set it in editText
        if (currentObstacle.obstacleType != "") typeEditText.setText(currentObstacle.obstacleType)

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

        // Send photo to CNN for classification, and listen for results
        viewModel.analyzePhotoWithCNN(currentObstacle.photoPath)
            .observe(viewLifecycleOwner, Observer { cnnResult ->

                cnnResult.onFailure {
                    Log.d(TAG("FAILURE"), it.toString())
                }

                cnnResult.onSuccess { result ->
                    Log.d(TAG(), result.toString())
                    // sort CNN results (smallest to largest)
                    val sorted = result.toList().sortedBy { (_, value) -> value }.toMap()
                    Log.d(TAG(), sorted.toString())

                    // reverse results (only keys) for displaying in input dialog
                    sorted.keys.reversed().toTypedArray().let { cnnResults = it }

                    // save CNN results in obstacle, first sanitize keys
                    currentObstacle.typeProbabilitiesCNN = sorted.map { (k, v) ->
                        k.filterNot {
                            setOf(' ', '(', ')', '.', '-', '/').contains(it)
                        } to v
                    }.toMap()

                    val timeUntilCnnResults = System.currentTimeMillis() - fragCreationTime

                    currentObstacle.timeUntilCnnResults = timeUntilCnnResults
                    Log.d(TAG(), "$timeUntilCnnResults millis until CNN results")

                    // Automatically show dialog when results become available
                    if (timeUntilCnnResults <= 5000) {
                        if (currentObstacle.obstacleType == "") {
                            if (::cnnResults.isInitialized) {
                                if (cnnResults.isNotEmpty()) showTypeSelectionDialog()
                            }
                        }
                    }
                }
            })

        // Add listeners on editText
        typeEditText.apply {
            // Show custom dialog for obstacle type selection
            setOnClickListener {
                showTypeSelectionDialog()
            }

            // Clear any error message present editText value changes
            addTextChangedListener {
                select_obstacle_type_layout.error = null
            }
        }

        // Go to photo editing fragment
        button_edit_photo.setOnClickListener {
            findNavController().navigate(
                ObstacleEditFragmentDirections.actionObstacleEditFragmentToCropFragment(
                    // TODO: 24/07/20 when user comes back from crop, respect any location edits, don't track GPS
                    currentObstacle
                )
            )
        }

        // Setup map view
        mapView = map
        mapView.onCreate(null) // TODO fix, here a mapViewBundle should be passed instead of null
        mapView.getMapAsync { googleMap ->

            val obstaclePosition = currentObstacle.getLocationAsLatLong()

            googleMap.apply {
                // Add marker and set map camera position
                if (obstaclePosition.latitude != 0.0) {
                    // Add marker indicating the obstacle, if location provided is not 0, 0
                    addMarker(MarkerOptions().position(obstaclePosition).title("Marker"))
                    // Move map camera to above obstacle position
                    moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            obstaclePosition,
                            DEFAULT_ZOOM_LEVEL
                        )
                    )
                } else {
                    // In case location is empty, move camera above the general area of Nicosia
                    moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(35.169933, 33.361071),
                            CITY_ZOOM_LEVEL
                        )
                    )
                }

                // Add map boundaries
                setLatLngBoundsForCameraTarget(CYPRUS)

                // Set min zoom, so user cannot zoom out too much (1 is world, 20 buildings)
                setMinZoomPreference(MIN_ZOOM_LEVEL)

                // Listen for long clicks on map, which allows user to change location manually
                setOnMapLongClickListener { latLng ->
                    locationNotManuallyEdited = false
                    clear()
                    addMarker(MarkerOptions().position(latLng))

                    // Save location indicated by user
                    // TODO here the altitude should be updated as well, does maps provided it
                    //  somewhere? Or perhaps set it to 0
                    //  also location accuracy
                    currentObstacle.setLocationFromLatLong(latLng)
                }

                // make sure we still have location permission, if we don't, don't enable my
                // location layer on map
                if (isAllGranted(Permission.ACCESS_FINE_LOCATION)) {
                    Log.d(TAG(), "all granted")
                    viewModel.locationLiveData.observe(viewLifecycleOwner, Observer {
                        Log.d(TAG(), "new location")
                        if (locationNotManuallyEdited) {
                            clear()
                            addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
                            currentObstacle.location.apply {
                                this.latitude = it.latitude
                                this.longitude = it.longitude
                            }
                        }
                    })

                    // Enable myLocation layer and button
                    isMyLocationEnabled = true
                    setOnMyLocationButtonClickListener {
                        false
                    }
                    setOnMyLocationClickListener {
                        // TODO: 24/07/20 here move marker to current location if user clicks on
                        //  location dot, but only after the user has manually changed location
                        //  by long clicking on map. Also maybe afterwards re-make marker to
                        //  follow location dot?
                        if (!locationNotManuallyEdited) {
                            clear()
                            addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
                            currentObstacle.location.apply {
                                this.latitude = it.latitude
                                this.longitude = it.longitude
                                // TODO: 24/07/20 include altitude, accuracy
                            }
                            locationNotManuallyEdited = true
                        }
                    }
                }

                // Shrink FAB when user is moving the map around
                setOnCameraMoveStartedListener {
                    fabSubmit.shrink()
                    lifecycleScope.launch {
                        // TODO add more checks here
                        delay(8000)
                        fabSubmit.extend()
                    }
                }
            }
        }

        fabSubmit.setOnClickListener {
            checkAndSubmitObstacle()
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
            R.id.action_submit_obstacle -> {
                checkAndSubmitObstacle()
                true
            }
            R.id.action_cancel_edit_obstacle -> {
                displayDiscardConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showTypeSelectionDialog() {
        // get type array either from CNN results, or from resources if CNN classification
        // didn't work
        val obsTypeArray = if (::cnnResults.isInitialized) {
            if (cnnResults.isNotEmpty()) {
                // add types that are not part of the CNN
                val diff = getAlphabeticalTypeArray().filterNot {
                    cnnResults.toSet().contains(it)
                }.sorted().toTypedArray()
                cnnResults + diff
            } else getAlphabeticalTypeArray()
        } else {
            getAlphabeticalTypeArray()
        }

        if (currentObstacle.obstacleType.isNotBlank())
            Log.d(TAG("Index of current"), "${obsTypeArray.indexOf(currentObstacle.obstacleType)}")

        val dialog = MaterialDialog(requireContext()).customView(
            R.layout.type_selection_dialog,
            scrollable = true
        )

        // get references to views on dialog that are of interest
        val customDialogView = dialog.getCustomView() as ConstraintLayout
        val dialogAnalysisIndicator = customDialogView.dialog_analysis_indicator
        val dialogContents = customDialogView.dialog_contents
        val radioGroup = customDialogView.types_radio_group

        // initially only show 5 most likely types, as determined by the CNN (hide the rest)
        populateRadioGroupTypeList(radioGroup, obsTypeArray, 5)

        // get references to last radio button and customEditText
        val customEditTextLayout = customDialogView.type_custom_input_layout
        val customEditText = customDialogView.type_custom_input_edittext
        // when radio buttons are added, they are given IDs in the form 1000 + index in obsTypeArray
        val lastEmptyRadioButton =
            radioGroup.findViewById<RadioButton>(1000 + obsTypeArray.size + 1)

        // show analysis indicator when first launched
        if (analysisIndicatorNotShown) {
            analysisIndicatorNotShown = false
            lifecycleScope.launch {
                delay((1200..1800).random().toLong())
                dialogAnalysisIndicator.visibility = View.GONE
                dialogContents.visibility = View.VISIBLE
            }
        } else {
            dialogAnalysisIndicator.visibility = View.GONE
            dialogContents.visibility = View.VISIBLE
        }

        // show more button is clicked
        customDialogView.button_show_more_types?.setOnClickListener { showMoreButton ->
            // reveal all possible types
            for (i in 0..obsTypeArray.size + 1)
                radioGroup.findViewById<RadioButton>(1000 + i)?.visibility = View.VISIBLE

            // hide more button and CNN explanation
            showMoreButton.visibility = View.GONE
            customDialogView.cnn_explanation?.visibility = View.GONE

            // show custom editText
            customEditTextLayout.visibility = View.VISIBLE
        }

        // set listeners to all views to regulate their behaviour
        customEditText.apply {
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) lastEmptyRadioButton.isChecked = true
            }
            setOnClickListener {
                lastEmptyRadioButton.isChecked = true
            }
            addTextChangedListener { currentText ->
                if (currentText.toString().trim().length > 2) customEditTextLayout.error = null
            }
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
                    customEditTextLayout.error = null
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
                    if (checkedId == lastEmptyRadioButton.id) {
                        // customEditText selected, make sure text is not too short or empty
                        val currentText = customEditText.editableText.toString().trim()
                        if (currentText.length > 2) {
                            Log.d(TAG(), "current custom text $currentText")
                            currentObstacle.obstacleType = currentText
                            typeEditText.setText(currentObstacle.obstacleType)
                            dismiss()
                        } else {
                            // text provided too short/empty
                            Toast.makeText(
                                context, getString(R.string.toast_provide_valid_type), Toast
                                    .LENGTH_SHORT
                            ).show()
                            // focus on editText and set error
                            lastEmptyRadioButton.parent
                                .requestChildFocus(customEditTextLayout, customEditTextLayout)
                            customEditTextLayout.error =
                                getString(R.string.error_type_not_valid)
                        }
                    } else {
                        // selection is from predefined list, good to go
                        typeEditText.setText(currentObstacle.obstacleType)
                        dismiss()
                    }
                } else {
                    // -1 means none selected
                    Toast.makeText(
                        context,
                        getString(R.string.toast_make_selection),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            negativeButton(R.string.dialog_cancel_button) { dismiss() }
            lifecycleOwner(viewLifecycleOwner)
            dialog.show()
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

    private fun checkAndSubmitObstacle() {
        // Make sure the user has chosen an obstacle type before submitting
        if (allRequiredInfoEntered()) {
            // Obstacle type already saved in obstacle entity
            viewModel.insertObstacle(currentObstacle)
            try {
                // TODO: 11/07/20 is this necessary?
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_obstacle_submitted),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e(TAG(), "Exception while trying to show toast", e)
            }
            findNavController().navigate(
                R.id.action_obstacleEditFragment_to_obstacleListFragment
            )
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
        radioGroup: RadioGroup,
        obsTypeArray: Array<String>,
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

        layoutParams.bottomMargin = 20

        // populate radio group, respecting any limits on number of items required
        obsTypeArray.forEachIndexed { i, obsType ->
            radioGroup.addView(RadioButton(context).also { rb ->
                rb.id = 1000 + i
                rb.text = obsType
                // add more margin for last non-empty rb
                if (i == obsTypeArray.size - 1) layoutParams.bottomMargin = 40
                rb.layoutParams = layoutParams
                if (i >= listLimit) rb.visibility = View.GONE
            })
        }

        // larger margin for last empty rb, to accommodate editText
        layoutParams.bottomMargin = 75

        if (addEmptyRadioButtonAtBottom) {
            radioGroup.addView(RadioButton(context).also { rb ->
                // empty text
                rb.id = 1000 + obsTypeArray.size + 1
                rb.layoutParams = layoutParams
                rb.visibility = View.GONE
            })
        }
    }

    private fun getAlphabeticalTypeArray(): Array<String> =
        resources.getStringArray(R.array.obstacle_types_array).toList().sorted().toTypedArray()

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
