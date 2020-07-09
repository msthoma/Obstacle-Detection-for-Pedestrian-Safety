package cy.org.rise.obsai.ui

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
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import cy.org.rise.obsai.R
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.TAG
import cy.org.rise.obsai.utils.roundTo
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment used for capturing photos, geo-tagging them, and saving phone orientation at the time of
 * capture.
 *
 * The CameraView library is used for interacting with the camera
 * see [https://github.com/natario1/CameraView].
 *
 * For getting location, see documentation at [https://developer.android.com/training/location].
 *
 * For getting orientation, see
 * [https://developer.android.com/guide/topics/sensors/sensors_overview].
 */
class CameraFragment : Fragment(), SensorEventListener {
    // Camera/photo related vars
    private lateinit var cameraView: CameraView
    private lateinit var currentPhotoPath: String

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
        setHasOptionsMenu(true)
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

                photoFile?.let { tempPhotoFile ->
                    result.toFile(tempPhotoFile) { finalPhotoFile ->
                        finalPhotoFile?.let {
                            val action = CameraFragmentDirections
                                .actionCameraFragmentToObstacleEditFragment(
                                    createCurrentObstacle()
                                )
                            findNavController().navigate(action)
                        } ?: throw IOException("Unable to save obstacle photo")
                    }
                }
            }
        })

//        cameraView.addFrameProcessor { frame ->
//            if (frame.dataClass == Image::class.java) {
//                Log.d(TAG(), "frame: ${frame.size} ${frame.time}")
//            }
//        }

        camera_button.setOnClickListener {
            Log.d(TAG(), "camera button pressed")
            cameraView.takePicture()
        }

        // Get last known location (below in getLocationUpdates() a service is started that will
        // provide a more up to date location if it becomes available)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { updateObstacleLocation(it) }
        }

        getLocationUpdates()

        // Orientation stuff
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("JPEG_${timeStamp}", ".jpg", storageDir).apply {
            currentPhotoPath = this.absolutePath
        }
    }

    private fun getLocationUpdates() {
        // this is used to get any updates to the location of the user, in case they change
        // during taking a photo of an obstacle
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 20000 // 20s
            fastestInterval = 10000 // 10s
            smallestDisplacement = 2f // 2m
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let { updateObstacleLocation(it.lastLocation) }
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
        // make a copies of the orientation arrays, just to be safe (probably unnecessary)
        val orientationAnglesCopy = orientationAngles.copyOf()
        val accelerometerReadingCopy = accelerometerReading.copyOf()
        val magnetometerReadingCopy = magnetometerReading.copyOf()

        // in case location has not been initialized, set location to zero
        val obsLocation = if (::currentLocation.isInitialized) {
            Obstacle.Location(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )
        } else {
            Obstacle.Location(latitude = 0.0, longitude = 0.0)
        }

        return Obstacle(
            obstacleType = "",
            photoPath = currentPhotoPath,
            location = obsLocation,
            altitude = if (::currentLocation.isInitialized) currentLocation.altitude else 0.0,
            locationAccuracy = if (::currentLocation.isInitialized) currentLocation.accuracy else
                0.0f,
            orientation = Obstacle.Orientation(
                // Note the order of the axes, zxy, NOT xyz
                z = orientationAnglesCopy[0].toDouble(), // Azimuth (degrees of rotation about the -z axis)
                x = orientationAnglesCopy[1].toDouble(), // Pitch (degrees of rotation about the x axis)
                y = orientationAnglesCopy[2].toDouble() // Roll (degrees of rotation about the y axis)
            ),
            accelerometer = Obstacle.Orientation(
                x = accelerometerReadingCopy[0].toDouble(),
                y = accelerometerReadingCopy[1].toDouble(),
                z = accelerometerReadingCopy[2].toDouble()
            ),
            compass = Obstacle.Orientation(
                x = magnetometerReadingCopy[0].toDouble(),
                y = magnetometerReadingCopy[1].toDouble(),
                z = magnetometerReadingCopy[2].toDouble()
            )
        )
    }

    private fun updateObstacleLocation(location: Location) {
        currentLocation = location

        // location in some cases can be NULL see
        // https://developer.android.com/training/location/retrieve-current#last-known

        if (photo_details.isVisible) {
            coordinates.text = "Location: ${currentLocation.latitude}, ${currentLocation
                .longitude}\nAltitude ${location.altitude.roundTo(2)}"
        }

        // this saves location in photo's EXIF data
        cameraView.setLocation(currentLocation.latitude, currentLocation.longitude)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()

        // Don't receive any more updates from orientation sensors
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
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
            // Calculate device orientation by combining accelerometer and magneticField, see
            // https://developer.android.com/guide/topics/sensors/sensors_position#sensors-pos-orient
            // Results may need to be translated using remapCoordinateSystem(), see
            // https://developer.android.com/reference/android/hardware/SensorManager#remapCoordinateSystem(float[],%20int,%20int,%20float[])
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading
            )
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // Set details in overlay view
            if (photo_details.isVisible) {
                val orientationArray = orientationAngles.joinToString(transform = { fl ->
                    fl.roundTo(3).toString()
                })
                val compassArray = magnetometerReading.joinToString(transform = { fl ->
                    fl.roundTo(3).toString()
                })
                accelerometer.text = "Orientation: ${orientationArray}"
                compass.text = "Compass: ${compassArray}"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_photo_details -> {
                if (photo_details.isVisible) {
                    photo_details.visibility = View.GONE
                } else {
                    photo_details.visibility = View.VISIBLE
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_camera, menu)
    }
}
