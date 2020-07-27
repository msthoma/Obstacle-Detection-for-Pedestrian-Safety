package cy.org.rise.obsai.ui

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.assent.Permission
import com.afollestad.assent.isAllGranted
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import cy.org.rise.obsai.R
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.InjectorUtils
import cy.org.rise.obsai.utils.TAG
import cy.org.rise.obsai.utils.formatAsStr
import cy.org.rise.obsai.utils.getUniqueAppInstallID
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
 */
class CameraFragment : Fragment() {
    // Camera/photo related vars
    private lateinit var cameraView: CameraView
    private lateinit var currentPhotoPath: String

    // Location related vars
    private lateinit var currentLocation: Location

    // Sensor related vars
    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)
    private var orientationAngles = FloatArray(3)

    private val viewModel: ObstacleViewModel by viewModels {
        InjectorUtils.provideObstacleViewModelFactory(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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

        camera_button.setOnClickListener {
            Log.d(TAG(), "camera button pressed")
            cameraView.takePicture()
        }

        viewModel.apply {
            // Get last known location and subscribe to location updates
            if (isAllGranted(Permission.ACCESS_FINE_LOCATION)) {
                locationLiveData.observe(viewLifecycleOwner, Observer { newLoc ->
                    // location in some cases can be NULL see
                    // https://developer.android.com/training/location/retrieve-current#last-known
                    currentLocation = newLoc

                    // Set location in overlay view
                    if (photo_details.isVisible) {
                        location.text = getString(
                            R.string.detail_location,
                            "${currentLocation.latitude}, ${currentLocation.longitude}"
                        )
                    }

                    // this saves location in photo's EXIF data
                    cameraView.setLocation(currentLocation.latitude, currentLocation.longitude)
                })
            }

            // Track orientation
            orientationLiveData.observe(viewLifecycleOwner, Observer {
                accelerometerReading = it.accelerometer
                magnetometerReading = it.compass
                orientationAngles = it.orientation

                // Set details in overlay view
                if (photo_details.isVisible) {
                    accelerometer.text =
                        getString(R.string.detail_accelerometer, accelerometerReading.formatAsStr())
                    compass.text =
                        getString(R.string.detail_compass, magnetometerReading.formatAsStr())
                    orientation.text =
                        getString(R.string.detail_orientation, orientationAngles.formatAsStr())
                }
            })
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("JPEG_$timeStamp", ".jpg", storageDir).apply {
            currentPhotoPath = this.absolutePath
        }
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
            appInstallID = requireContext().getUniqueAppInstallID(),
            obstacleType = "", // filled later by user
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
