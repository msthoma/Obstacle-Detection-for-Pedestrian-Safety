package cy.org.rise.obsai.ui


import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color.argb
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
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
import kotlinx.android.synthetic.main.fragment_obstacle_edit.*
import java.io.File

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

        val items = listOf("Material", "Design", "Components", "Android")

        val itemAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        (select_obstacle_type_text_material.editText as? AutoCompleteTextView)?.setAdapter(
            itemAdapter
        )

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

        // Set obstacle label choices in spinner
        requireContext()
            .let {
                ArrayAdapter.createFromResource(
                    it,
                    R.array.obstacle_types_array,
                    android.R.layout.simple_spinner_dropdown_item
                )
            }.also { arrayAdapter ->
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = arrayAdapter

                // When coming from crop fragment, if the type was already set, restore its value
                if (currentObstacle.obstacleType != "") {
                    spinner.setSelection(arrayAdapter.getPosition(currentObstacle.obstacleType))
                }
            }

        button_edit_photo.setOnClickListener {
            // Save type before going to the crop fragment
            currentObstacle.obstacleType = spinner.selectedItem.toString()
            findNavController().navigate(
                ObstacleEditFragmentDirections.actionObstacleEditFragmentToCropFragment(
                    currentObstacle
                )
            )
        }

        // Setup map view
        mapView = map
        mapView.onCreate(null) // here a mapViewBundle should be passed instead of null
        mapView.getMapAsync { googleMap ->
            val obstaclePosition = LatLng(
                currentObstacle.location.latitude,
                currentObstacle.location.longitude
            )

            // Add marker indicating the obstacle
            googleMap.addMarker(MarkerOptions().position(obstaclePosition).title("Marker"))

            // Move camera to appropriate position
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(obstaclePosition, 12f))

            // Add map boundaries
            val CYPRUS = LatLngBounds(
                LatLng(34.520142, 32.186723), // Southwest corner
                LatLng(35.738372, 34.644546) // Northeast corner
            )
            googleMap.setLatLngBoundsForCameraTarget(CYPRUS)

            // Set min zoom, so user cannot zoom out too much (1 is world, 20 buildings)
            googleMap.setMinZoomPreference(7.5f)
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
                // get obstacle type array
                val obstacleTypes = resources.getStringArray(R.array.obstacle_types_array)

                // Make sure the user has chosen an obstacle type before submitting
                if (spinner.selectedItem.toString() == obstacleTypes[0]) {
                    // Show toast message
                    Toast.makeText(
                        context,
                        R.string.toast_type_selection_warning,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    // Highlight spinner with type choices
                    ObjectAnimator.ofObject(
                        spinner,
                        "backgroundColor",
                        ArgbEvaluator(),
                        // Colors need to be in ARGB form to work with the animator
                        argb(100, 255, 255, 255), // white
                        argb(100, 255, 0, 0), // red
                        argb(100, 255, 255, 255) // white
                    ).setDuration(1000).start()
                } else {
                    // Save obstacle type
                    currentObstacle.obstacleType = spinner.selectedItem.toString()
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
        requireContext().let { context ->
            MaterialDialog(context).show {
                title(R.string.dialog_discard_title)
                message(R.string.dialog_discard_msg)
                icon(R.drawable.ic_warning_black_24dp)
                positiveButton(R.string.dialog_discard_positive) {
                    // by navigating back with the action below, the back stack is popped up to the
                    // list fragment, and so a back press there does not return the user back here
                    findNavController().navigate(
                        R.id.action_obstacleEditFragment_to_obstacleListFragment
                    )
                }
                negativeButton(R.string.dialog_negative_button) { dismiss() }
                lifecycleOwner(viewLifecycleOwner)
            }
        }
    }

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
