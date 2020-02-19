package cy.org.rise.obsai

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.gms.location.*
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CameraFragment : Fragment() {
    private lateinit var cameraView: CameraView
    lateinit var currentPhotoPath: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // most of the other settings for CameraView are set in the activity's xml layout
        cameraView = camera_view
        cameraView.setLifecycleOwner(this)

        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
//                super.onPictureTaken(result)

                val photoFile: File? = try {
                    createImageFile()

                } catch (ex: IOException) {
                    Log.e(TAG(), "Error creating image file")
                    null
                }

                photoFile?.let {
                    result.run {
                        toFile(it) { file ->
                            file?.let {
                                // TODO pass to obstacle edit fragment
                                Log.d(TAG(), "photo file ready: " + file.absolutePath)
                                val action =
                                    CameraFragmentDirections
                                        .actionCameraFragmentToObstacleEditFragment(
                                            file.absolutePath
                                        )
                                cameraView.findNavController().navigate(action)
                            }
                        }
                    }
                }
            }
        })

        camera_button.setOnClickListener {
            Log.d(TAG(), "camera button pressed")
            cameraView.takePicture()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            recognized.text = "Lat:" + location.latitude + ", Long:" + location.longitude
        }

        getLocationUpdates()
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        locationRequest = LocationRequest.create().apply {
            interval = 50000
            fastestInterval = 50000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 10f // 10m
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.d(TAG(), "location callback")
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.d(TAG(), "Lat:" + location.latitude + ", Long:" + location.longitude)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
}
