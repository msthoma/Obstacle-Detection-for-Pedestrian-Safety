package cy.org.rise.obsai.ui


import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.theartofdev.edmodo.cropper.CropImageView
import cy.org.rise.obsai.R
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.TAG
import kotlinx.android.synthetic.main.fragment_crop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Fragment used for cropping obstacle photos.
 *
 * Android Image Cropper library is used for cropping,
 * see - [https://github.com/ArthurHub/Android-Image-Cropper]
 */
@ExperimentalStdlibApi
class CropFragment : Fragment() {

    // Arguments from the edit fragment
    private val args: ObstacleEditFragmentArgs by navArgs()

    private lateinit var currentObstacle: Obstacle
    private lateinit var cropImageView: CropImageView

    private lateinit var photoExifTags: Map<String, String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Set toolbar menu
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_crop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initialize crop view
        cropImageView = crop_image_view
        // Get obstacle object passed by the edit fragment
        currentObstacle = args.currentObstacle

        // Attempt to read photo file
        val photoFile: File? = try {
            File(currentObstacle.photoPath)
        } catch (ex: IllegalArgumentException) {
            Log.e(TAG(), "Error getting image file")
            null
        }

        // Values of all TAGS
        // https://developer.android.com/reference/kotlin/androidx/exifinterface/media/ExifInterface
        // https://regex101.com/
        // Python Regex r"&quot;([a-zA-Z]{2,})&quot;"
        val ins = requireContext().assets?.open("ExifTags.txt")
        val tagList = mutableListOf<String>()
        ins?.bufferedReader()?.forEachLine {
            tagList.add(it)
        }
        ins?.close()
        tagList.remove("Orientation")

        // Set photo in crop view
        photoFile?.also { file ->

            val exifInterface = ExifInterface(file)

            photoExifTags = buildMap {
                tagList.forEach { tag ->
                    if (exifInterface.hasAttribute(tag)) {
                        this[tag] = exifInterface.getAttribute(tag) as String
                    }
                }
            }

            Log.d(TAG(), photoExifTags.toString())
//            for (prop in ExifInterface::class.members) {
//                if (prop.name.contains("TAG", ignoreCase = true)) {
//                    Log.d(
//                        TAG(),
//                        "${prop.name} ${prop.name}"
//                    )
//                    Log.d(TAG(), exifInterface.javaClass.declaredFields[0].toString())
//                }
//            }

            context?.also { context ->
                val photoUri =
                    FileProvider.getUriForFile(
                        context, "com.example.android.fileprovider", file
                    )

                // Set crop view properties
                cropImageView.apply {
                    setImageUriAsync(photoUri)
                    isAutoZoomEnabled = true
                    scaleType = CropImageView.ScaleType.FIT_CENTER
                    isShowProgressBar = true
                }
            }
        }

        // Listen for cropped photo
        cropImageView.setOnCropImageCompleteListener { _, result ->
            // overwrite original photo file (https://stackoverflow.com/a/673014) and navigate back
            photoFile?.let { photo ->
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        // save file on background thread
                        result.bitmap.compress(
                            Bitmap.CompressFormat.JPEG, 100, FileOutputStream(photo)
                        ).also {
                            // if Exif data existed in the original, write them to the cropped photo
                            if (::photoExifTags.isInitialized) {
                                val croppedExifInterface = ExifInterface(photo)

                                photoExifTags.forEach { (tag, value) ->
                                    croppedExifInterface.setAttribute(tag, value)
                                }
                                croppedExifInterface.saveAttributes()
                            }
                        }
                    }.let { success ->
                        Log.d(TAG(), "Saving cropped photo result: $success")
                        if (success) {
                            Toast.makeText(
                                context,
                                "Error saving cropped photo, please try again",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        // Navigate back to edit fragment
                        findNavController().navigate(
                            CropFragmentDirections.actionCropFragmentToObstacleEditFragment(
                                currentObstacle
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_crop, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_rotate_photo -> {
                // Rotate photo 90o clockwise
                cropImageView.rotateImage(90)
                true
            }
            R.id.action_confirm_edit_photo -> {
                // Crops photo, OnCropImageCompleteListener above listens for result
                cropImageView.getCroppedImageAsync()
                true
            }
            R.id.action_cancel_edit_photo -> {
                // Cancel by navigating back to edit fragment
                findNavController().navigate(
                    CropFragmentDirections.actionCropFragmentToObstacleEditFragment(
                        currentObstacle
                    )
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
