package cy.org.rise.obsai


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
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
        photoFile?.also { Picasso.get().load(photoFile).into(obstacle_image_view) }

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
            currentObstacle.obs_type = spinner.selectedItem.toString()
            viewModel.insertObstacle(currentObstacle)
            findNavController().navigate(
                R.id.action_obstacleEditFragment_to_obstacleListFragment
            )
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

        // map configuration - see https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library for details
//        Configuration.getInstance()
//            .load(context, PreferenceManager.getDefaultSharedPreferences(context))
//        mapView.setTileSource(TileSourceFactory.MAPNIK)
//        // set photo location on map
//        val mapController: IMapController = mapView.controller
//        mapController.setZoom(18.5)
//        val geoPoint = GeoPoint(35.16989, 33.36116)
//        mapView.setExpectedCenter(geoPoint)

        // Setup map view
        mapView = map
        mapView.onCreate(null) // TODO fix passing mapViewBundle instead of null
        mapView.getMapAsync { googleMap ->
            // Add marker indicating the obstacle
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(currentObstacle.latitude, currentObstacle.longitude))
                    .title("Marker")
            )

            // Add map boundaries
            val CYPRUS = LatLngBounds(
                LatLng(34.520142, 32.186723), // Southwest corner
                LatLng(35.738372, 34.644546) // Northeast corner
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(CYPRUS, 0))
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
