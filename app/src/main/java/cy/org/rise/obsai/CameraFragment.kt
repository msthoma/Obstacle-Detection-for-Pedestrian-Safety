package cy.org.rise.obsai

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
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


class CameraFragment : Fragment(), SensorEventListener {
    // Camera/photo related vars
    private lateinit var cameraView: CameraView
    lateinit var currentPhotoPath: String

    // Location related vars
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Sensor related vars
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var sensor: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set camera settings
        // most of the other settings for CameraView are set in the activity's xml layout
        cameraView = camera_view
        cameraView.setLifecycleOwner(this)

        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {

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
                                Log.d(TAG(), "photo file ready: " + file.absolutePath)
                                val obs = createCurrentObstacle()
                                Log.d(TAG(), "obstacle created ${obs.toString()}")
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

        // Get last known location (below a service is started that will provide a more up to date
        // location if it becomes available)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!) // TODO fix
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            currentLocation = location
            coordinates.text =
                "Location: Lat:" + location.latitude + ", Long:" + location.longitude
        }
        // TODO update last known location with any updates from below
        // TODO basically update currentLocation var if there are updates
        getLocationUpdates()

        // Orientation stuff
        sensorManager =
            context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager // TODO fix
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
        // TODO fix getting location updates (currently only showing last known location)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!) // TODO fix
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

    private fun createCurrentObstacle(): Obstacle {
        return Obstacle(
            obstacle = "new",
            obs_type = "",
            photo = currentPhotoPath,
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            // TODO will these numbers change during saving the obstacle?
            x = accelerometerReading[0].toDouble(),
            y = accelerometerReading[1].toDouble(),
            z = accelerometerReading[2].toDouble()
        )
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()

        // Don't receive any more updates from orientation sensors
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        // comment //TODO fix
        startLocationUpdates()

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG(), "Sensor accuracy changed to $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(
                    event.values,
                    0,
                    accelerometerReading,
                    0,
                    accelerometerReading.size
                )
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(
                    event.values,
                    0,
                    magnetometerReading,
                    0,
                    magnetometerReading.size
                )
            }
            orientation.text = "Orientation: " + accelerometerReading.asList().toString()
        }
    }
}
