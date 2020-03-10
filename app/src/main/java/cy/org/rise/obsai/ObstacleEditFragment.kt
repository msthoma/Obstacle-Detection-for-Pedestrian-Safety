package cy.org.rise.obsai


import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color.argb
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.InjectorUtils
import cy.org.rise.obsai.utils.TAG
import kotlinx.android.synthetic.main.fragment_obstacle_edit.*
import java.io.File

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
        return inflater.inflate(R.layout.fragment_obstacle_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Get current obstacle
        currentObstacle = args.currentObstacle

        // Try to get the file from the arguments passed from the camera fragment
        val photoFile: File? = try {
            File(currentObstacle.photo)
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

        // Set obstacle label choices in spinner
        ArrayAdapter.createFromResource(
            context!!, // TODO fix !!
            R.array.obstacles_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = arrayAdapter

            // When coming from crop fragment, if the type was already set, restore its value
            if (currentObstacle.obs_type != "") {
                spinner.setSelection(arrayAdapter.getPosition(currentObstacle.obs_type))
            }
        }

        // Setup OnClickListeners for buttons
        button_submit.setOnClickListener {
            // Save obstacle type
            Log.d(TAG(), "button submit pressed")

            if (spinner.selectedItem.toString() == "Select type...") {
                Toast.makeText(context, R.string.toast_type_selection_warning, Toast.LENGTH_SHORT)
                    .show()
                ObjectAnimator.ofObject(
                    spinner,
                    "backgroundColor",
                    ArgbEvaluator(),
                    argb(100, 255, 255, 255),
                    argb(100, 255, 0, 0),
                    argb(100, 255, 255, 255)
                ).setDuration(1000).start()
            } else {
                currentObstacle.obs_type = spinner.selectedItem.toString()
                viewModel.insertObstacle(currentObstacle)
                findNavController().navigate(
                    R.id.action_obstacleEditFragment_to_obstacleListFragment
                )
            }
        }

        button_edit_photo.setOnClickListener {
            // Save type before going to the crop fragment
            currentObstacle.obs_type = spinner.selectedItem.toString()
            findNavController().navigate(
                ObstacleEditFragmentDirections.actionObstacleEditFragmentToCropFragment(
                    currentObstacle
                )
            )
        }

        // Setup map view
        mapView = map
        mapView.onCreate(null) // TODO fix passing mapViewBundle instead of null
        mapView.getMapAsync { googleMap ->
            val obstaclePosition = LatLng(currentObstacle.latitude, currentObstacle.longitude)

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Handles pressing back in edit fragment, which should go back to the list fragment
            // instead of the camera
            findNavController().popBackStack(R.id.obstacleListFragment, false)

            // TODO add discard confirmation dialog here
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
