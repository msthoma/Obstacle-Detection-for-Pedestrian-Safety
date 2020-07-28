package cy.org.rise.obsai.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout.LayoutParams
import android.widget.RadioButton
import android.widget.RadioGroup
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import cy.org.rise.obsai.R
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.*
import cy.org.rise.obsai.utils.Constants.CITY_ZOOM_LEVEL
import cy.org.rise.obsai.utils.Constants.CYPRUS
import cy.org.rise.obsai.utils.Constants.DEFAULT_ZOOM_LEVEL
import cy.org.rise.obsai.utils.Constants.MIN_ZOOM_LEVEL
import cy.org.rise.obsai.utils.Constants.NICOSIA_CENTER
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
    // TODO 28/07/20 DON"T save type in obstacle!! use another variable
    private lateinit var currentObstacle: Obstacle
    private lateinit var customEditText: TextInputEditText
    private lateinit var customEditTextLayout: TextInputLayout
    private lateinit var fabSubmit: ExtendedFloatingActionButton
    private lateinit var lastEmptyRadioButton: RadioButton
    private lateinit var mapView: MapView
    private lateinit var obsTypeArray: Array<String>
    private lateinit var radioGroup: RadioGroup
    private lateinit var typeEditText: EditText
    private lateinit var typeSelectionDialog: MaterialDialog
    private lateinit var typeSelectionDialogLayout: ConstraintLayout
    private val args: ObstacleEditFragmentArgs by navArgs()
    private val viewModel: ObstacleViewModel by viewModels {
        InjectorUtils.provideObstacleViewModelFactory(this)
    }
    private var start by Delegates.notNull<Long>()
    private var locationNotManuallyEdited = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true) // Set toolbar menu
        start = System.currentTimeMillis() // Save creation time
        return inflater.inflate(R.layout.fragment_obstacle_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentObstacle = args.currentObstacle // Get current obstacle from previous fragment args
        typeEditText = select_obstacle_type_edit_text
        fabSubmit = fab_submit

        // If type is already available set it in editText, otherwise show selection dialog
        if (currentObstacle.obstacleType.isNotEmpty()) {
            typeEditText.setText(currentObstacle.obstacleType)
        } else {
            showTypeSelectionDialog()
        }

        // Set obstacle photo in image view
        try {
            File(currentObstacle.photoPath)
        } catch (ex: IllegalArgumentException) {
            Log.e(TAG(), "Error getting image file")
            null
        }?.let { photoFile ->
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
                // TODO add slight delay here, so indicator is shown!!
                // TODO also add time limit, if results are not available show alphabetical
                cnnResult.onFailure {
                    Log.d(TAG("CNN failure"), it.toString())
                    obsTypeArray = processCnnResults()

                    // make sure dialog is currently being displayed
                    if (::typeSelectionDialog.isInitialized) {
                        populateSelectionList(onlyTop5 = false) // show ALL types alphabetically
                        toggleAnalysisIndicator() // hide image analysis indicator
                    }
                }
                cnnResult.onSuccess { result ->
                    Log.d(TAG("CNN success"), result.toString())
                    obsTypeArray = processCnnResults(result)

                    // make sure dialog is currently being displayed
                    if (::typeSelectionDialog.isInitialized) {
                        populateSelectionList(onlyTop5 = true) // show only top 5 types
                        toggleAnalysisIndicator() // hide image analysis indicator
                        toggleCnnExplanationAndMoreButton() // show CNN explanation & More button
                        typeSelectionDialogLayout.button_show_more_types?.setOnClickListener {
                            // listen for clicks on More button
                            populateSelectionList(onlyTop5 = false)
                            toggleCnnExplanationAndMoreButton()
                        }

                        // save CNN results in obstacle, as well as the CNN processing time
                        currentObstacle.apply {
                            typeProbabilitiesCNN = result.map { (k, v) ->
                                // sanitize types so they play well when JSONified
                                k.filterNot {
                                    setOf(' ', '(', ')', '.', '-', '/').contains(it)
                                } to v
                            }.toMap()
                            timeUntilCnnResults = System.currentTimeMillis() - start
                        }
                        Log.d(TAG(), "${currentObstacle.timeUntilCnnResults} ms until CNN results")
                    }
                }
            })

        typeEditText.apply {
            setOnClickListener {
                // When dialog is triggered here, it means it was shown before, so it is shown expanded
                showTypeSelectionDialog()
                // Show ALL types, alphabetic or based on CNN. Also set previously selected value.
                // NOTE: if the user pressed cancel on the dialog initially, the obstacle type may
                // be empty, but that is dealt with by the populateSelectionList() function
                // TODO scroll to current selection if available
                populateSelectionList(onlyTop5 = false, setSelected = currentObstacle.obstacleType)
                toggleAnalysisIndicator() // hide indicator
            }
            // clear errors when editText value changes
            addTextChangedListener { select_obstacle_type_layout.error = null }
        }

        button_edit_photo.setOnClickListener {
            // Go to photo cropping fragment
            findNavController().navigate(
                ObstacleEditFragmentDirections.actionObstacleEditFragmentToCropFragment(
                    // TODO 24/07/20 when user comes back from crop, respect any location edits, don't track GPS
                    currentObstacle
                )
            )
        }

        setupMapView() // Setup map view

        fabSubmit.setOnClickListener { checkAndSubmitObstacle() }

        // display discard confirmation dialog when up is pressed (overriding onSupportNavigateUp
        // in MainActivity enables re-routing of up callback here)
        requireActivity().onBackPressedDispatcher.addCallback(this) { discardConfirmationDialog() }
    }

    /** Manages obstacle map view. */
    @SuppressLint("MissingPermission") // permission is checked below before it is used
    private fun setupMapView() {
        mapView = map
        mapView.onCreate(null) // TODO fix, here a mapViewBundle should be passed instead of null
        mapView.getMapAsync { googleMap ->

            val obsPosition = currentObstacle.getLocationAsLatLong()

            googleMap.apply {
                if (obsPosition.latitude != 0.0) {
                    // Add obstacle marker
                    addMarker(MarkerOptions().position(obsPosition).title("Marker"))
                    // Move map camera to above obstacle position
                    moveCamera(CameraUpdateFactory.newLatLngZoom(obsPosition, DEFAULT_ZOOM_LEVEL))
                } else {
                    // In case location is empty, move camera above the general area of Nicosia
                    moveCamera(CameraUpdateFactory.newLatLngZoom(NICOSIA_CENTER, CITY_ZOOM_LEVEL))
                }

                setLatLngBoundsForCameraTarget(CYPRUS) // Add map boundaries

                setMinZoomPreference(MIN_ZOOM_LEVEL) // Set min zoom (1 is world, 20 buildings)

                // Listen for long clicks on map, which allows user to change location manually
                setOnMapLongClickListener { latLng ->
                    locationNotManuallyEdited = false
                    clear()
                    addMarker(MarkerOptions().position(latLng))
                    currentObstacle.setLocationFromLatLong(latLng) // Save location as set by user
                    // TODO here the altitude should be updated as well, does maps provided it
                    //  somewhere? Or perhaps set it to 0
                    //  also location accuracy
                }

                // Make sure we still have location permission before enabling location layer on map
                if (isAllGranted(Permission.ACCESS_FINE_LOCATION)) {
                    viewModel.locationLiveData.observe(viewLifecycleOwner, Observer { newLoc ->
                        if (locationNotManuallyEdited) {
                            clear()
                            addMarker(
                                MarkerOptions().position(LatLng(newLoc.latitude, newLoc.longitude))
                            )
                            currentObstacle.location.apply {
                                this.latitude = newLoc.latitude
                                this.longitude = newLoc.longitude
                            }
                        }
                    })

                    isMyLocationEnabled = true // Enable myLocation layer and button
                    setOnMyLocationButtonClickListener { false }
                    setOnMyLocationClickListener {
                        // TODO 24/07/20 here move marker to current location if user clicks on
                        //  location dot, but only after the user has manually changed location
                        //  by long clicking on map. Also maybe afterwards re-make marker to
                        //  follow location dot?
                        if (!locationNotManuallyEdited) {
                            clear()
                            addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
                            currentObstacle.location.apply {
                                this.latitude = it.latitude
                                this.longitude = it.longitude
                                // TODO 24/07/20 include altitude, accuracy
                            }
                            locationNotManuallyEdited = true
                        }
                    }
                }

                setOnCameraMoveStartedListener {
                    fabSubmit.shrink() // Shrink FAB when user is moving the map around
                    lifecycleScope.launch {
                        // TODO add more checks here, check if it is extended or not
                        delay(8000)
                        fabSubmit.extend()
                    }
                }
            }
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
                discardConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** Displays the obstacle type selection dialog. */
    private fun showTypeSelectionDialog() {
        // create dialog (re-created each time this function is called)
        typeSelectionDialog = MaterialDialog(requireContext()).customView(
            R.layout.type_selection_dialog,
            scrollable = true
        )

        // get references to views that are of interest
        typeSelectionDialogLayout = typeSelectionDialog.getCustomView() as ConstraintLayout
        radioGroup = typeSelectionDialogLayout.types_radio_group

        // set up the other properties of the dialog
        typeSelectionDialog.apply {
            noAutoDismiss() // important, otherwise dialog is dismissed without the checks below
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
                            requireContext().toast(R.string.toast_provide_valid_type)
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
                    requireContext().toast(R.string.toast_make_selection, short = true)
                }
            }
            negativeButton(R.string.dialog_cancel_button) { dismiss() }
            lifecycleOwner(viewLifecycleOwner)
            show()
        }
    }

    /** Shows confirmation dialog when back or up are pressed, or menu cancel action is selected. */
    private fun discardConfirmationDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.dialog_discard_title)
            message(R.string.dialog_discard_msg)
            icon(R.drawable.ic_warning_black_24dp)
            lifecycleOwner(viewLifecycleOwner)
            negativeButton(R.string.dialog_cancel_button) { dismiss() }
            positiveButton(R.string.dialog_discard_positive) {
                try {
                    File(currentObstacle.photoPath).delete() // delete obstacle photo
                } catch (ex: IOException) {
                    Log.e(TAG(), "Error deleting obstacle photo", ex)
                }

                // by navigating back with the action below, the back stack is popped up to the
                // list fragment, and so a back press there does not return the user back here
                findNavController().navigate(
                    R.id.action_obstacleEditFragment_to_obstacleListFragment
                )
            }
        }
    }

    /** Submits current obstacle, provided that all required information has been entered. */
    private fun checkAndSubmitObstacle() {
        if (allRequiredInfoEntered()) {
            viewModel.insertObstacle(currentObstacle)
            requireContext().toast(R.string.toast_obstacle_submitted)
            findNavController().navigate(R.id.action_obstacleEditFragment_to_obstacleListFragment)
        }
    }

    /** Checks whether all required obstacle information has been entered (type and location). */
    private fun allRequiredInfoEntered(): Boolean =
        if (select_obstacle_type_edit_text.text.toString().isBlank()) {
            // Check if type was selected
            select_obstacle_type_layout.error = getString(R.string.error_type_not_selected)
            false
        } else if (currentObstacle.location.latitude == 0.0 || currentObstacle.location.longitude == 0.0) {
            // Check if location was selected
            requireContext().toast(R.string.toast_location_required)
            false
        } else {
            true
        }

    /**
     * Populates the contents of the type selection dialog.
     *
     * @param onlyTop5 whether to show only the top 5 choices or all of them
     * @param setSelected mark radio button with this value as checked; specifying this will
     * show all types regardless of what value the onlyTop5 parameter has
     */
    private fun populateSelectionList(onlyTop5: Boolean, setSelected: String = "") {
        val showOnlyTop5 = if (setSelected.isNotBlank()) false else onlyTop5

        radioGroup.removeAllViews() // make sure any previous entries are removed

        // create view params for individual Radio Buttons
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams.bottomMargin = 20

        // populate radio group, respecting any limits set on number of type; when a limit is
        // specified, all types are set in the group, but the ones above the limit are marked as GONE
        obsTypeArray.forEachIndexed { i, obsType ->
            radioGroup.addView(RadioButton(requireContext()).also { rb ->
                rb.id = ID_OFFSET + i // set radio button IDs in the form of ID_OFFSET + i (ints)
                rb.text = obsType
                // add more margin for last non-empty rb
                if (i == obsTypeArray.size - 1) layoutParams.bottomMargin = 40
                rb.layoutParams = layoutParams
                if (showOnlyTop5 && i >= 5) rb.visibility = View.GONE
                if (setSelected.isNotBlank() && obsType == setSelected) rb.isChecked = true
            })
        }

        layoutParams.bottomMargin = 75 // larger margin for last empty rb, to accommodate editText

        // add empty radio button (no text) at bottom
        radioGroup.addView(RadioButton(requireContext()).also { rb ->
            rb.id = ID_OFFSET + obsTypeArray.size + 1
            rb.layoutParams = layoutParams
            if (showOnlyTop5) rb.visibility = View.GONE
            lastEmptyRadioButton = rb // get a reference to it
        })

        // get customEditText and set its visibility
        customEditTextLayout = typeSelectionDialogLayout.type_custom_input_layout
        customEditText = typeSelectionDialogLayout.type_custom_input_edittext
        if (showOnlyTop5) {
            customEditTextLayout.visibility = View.GONE
        } else {
            customEditTextLayout.visibility = View.VISIBLE
        }

        // set listeners to customEditText and radioGroup
        customEditText.apply {
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) lastEmptyRadioButton.isChecked = true
            }
            setOnClickListener { lastEmptyRadioButton.isChecked = true }
            addTextChangedListener { currentText ->
                if (currentText.toString().trim().length > 2) customEditTextLayout.error = null
            }
        }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != lastEmptyRadioButton.id) {
                radioGroup.findViewById<RadioButton>(checkedId).also { rb ->
                    currentObstacle.obstacleType = rb.text.toString() // capture current selection
                    Log.d(TAG(), "current selection ${currentObstacle.obstacleType}")
                }
                // remove focus and clear errors from customEditText
                customEditText.apply {
                    clearFocus(); hideKeyboard(); customEditTextLayout.error = null
                }
            } else {
                // when checkedId == lastEmptyRadioButton.id select editText
                customEditText.apply { requestFocus(); showKeyboard() }
            }
        }
    }

    /**
     * Retrieves alphabetical obstacle type array from resources.
     *
     * @return alphabetical obstacle type array
     */
    private fun getAlphabeticalTypeArray(): Array<String> =
        resources.getStringArray(R.array.obstacle_types_array).toList().sorted().toTypedArray()

    /**
     * Transforms CNN results so they can be displayed in the UI, by sorting them and adding any
     * types that are not part of the CNN; in case the results are empty (CNN failure), an
     * alphabetical list is returned.
     *
     * TODO make this return Map instead of Array
     * @param results processed CNN results
     */
    private fun processCnnResults(results: Map<String, Float>? = null): Array<String> =
        if (results.isNullOrEmpty()) {
            val alphabetic = getAlphabeticalTypeArray().map { it to 0.0f }.toMap()
            // for now convert to array
            alphabetic.keys.toTypedArray()
        } else {
            // sort CNN results (largest to smallest probability)
            val sorted = results.toList().sortedByDescending { (_, value) -> value }.toMap()
            // again, for now convert to simple array
            val sortedArray = sorted.keys.toTypedArray()
            // add types that are not part of the CNN
            val diff = getAlphabeticalTypeArray().filterNot {
                sortedArray.toSet().contains(it)
            }.sorted().toTypedArray()
            sortedArray + diff
        }

    /** Toggles visibility of the image analysis indicator, and the other dialog contents. */
    private fun toggleAnalysisIndicator() = typeSelectionDialogLayout.apply {
        dialog_analysis_indicator?.toggleVisibility()
        dialog_contents?.toggleVisibility()
    }

    /** Toggles visibility of the CNN explanation message, as well as the Show more button. */
    private fun toggleCnnExplanationAndMoreButton() = typeSelectionDialogLayout.apply {
        cnn_explanation?.toggleVisibility()
        button_show_more_types?.toggleVisibility()
    }

    /** Override of function required by map view. */
    override fun onResume() {
        super.onResume(); mapView.onResume()
    }

    /** Override of function required by map view. */
    override fun onStart() {
        super.onStart(); mapView.onStart()
    }

    /** Override of function required by map view. */
    override fun onStop() {
        super.onStop(); mapView.onStop()
    }

    /** Override of function required by map view. */
    override fun onDestroy() {
        super.onDestroy(); mapView.onDestroy()
    }

    /** Override of function required by map view. */
    override fun onLowMemory() {
        super.onLowMemory(); mapView.onLowMemory()
    }

    /** Override of function required by map view. */
    override fun onPause() {
        super.onPause(); mapView.onPause()
    }

    private companion object {
        const val ID_OFFSET = 100
    }
}
