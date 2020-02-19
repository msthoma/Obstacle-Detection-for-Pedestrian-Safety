package cy.org.rise.obsai


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_obstacle_edit.*
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File

class ObstacleEditFragment : Fragment() {

    private lateinit var mapView: MapView
    val args: ObstacleEditFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_obstacle_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val photoFile: File? = try {
            File(args.currentPhoto)
        } catch (ex: IllegalArgumentException) {
            Log.e(TAG(), "Error getting image file")
            null
        }

        photoFile?.also {
            context?.also { context ->
                val photoUri =
                    FileProvider.getUriForFile(
                        context, "com.example.android.fileprovider", it
                    )
                Picasso.get().load(photoUri).into(obstacle_image_view)
//                crop_image_view.setImageUriAsync(photoUri)
//                crop_image_view.isAutoZoomEnabled = true
//                crop_image_view.scaleType = CropImageView.ScaleType.FIT_CENTER
            }
        }

        // set possible obstacle labels in spinner
        ArrayAdapter.createFromResource(
            context!!, // TODO fix !!
            R.array.obstacles_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = arrayAdapter
        }

        button_submit.setOnClickListener { v ->
            v.findNavController().navigate(R.id.action_obstacleEditFragment_to_obstacleListFragment)
        }

        button_edit_photo.setOnClickListener { v ->
            v.findNavController().navigate(
                ObstacleEditFragmentDirections.actionObstacleEditFragmentToCropFragment(
                    photoFile?.absolutePath ?: ""
                )
            )
        }

        // map configuration - see https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library for details
        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context))
        mapView = map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        // set photo location on map
        val mapController: IMapController = mapView.controller
        mapController.setZoom(18.5)
        val geoPoint = GeoPoint(35.16989, 33.36116)
        mapView.setExpectedCenter(geoPoint)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
