package cy.org.rise.obsai.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import cy.org.rise.obsai.R
import cy.org.rise.obsai.utils.InjectorUtils
import cy.org.rise.obsai.utils.TAG

class MapFragment : Fragment() {

    private val viewModel: ObstacleViewModel by viewModels {
        InjectorUtils.provideObstacleViewModelFactory(this)
    }

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val NICOSIA_CENTER = LatLng(35.169933, 33.361071)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NICOSIA_CENTER, 12f))
        viewModel.allServerObstacles.observe(viewLifecycleOwner, Observer { obstacles ->
            Log.d(TAG(), "got ${obstacles?.size} obstacles")
            Log.d(TAG(), "obstacle 1: ${obstacles?.get(0)}")
            obstacles?.forEach {
                Log.d(TAG(), "adding obstacle at ${it.getLocationAsLatLong()}")
                googleMap.addMarker(MarkerOptions().position(it.getLocationAsLatLong()))
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}
