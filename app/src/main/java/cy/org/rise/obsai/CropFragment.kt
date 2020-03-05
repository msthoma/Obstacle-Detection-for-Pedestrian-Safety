package cy.org.rise.obsai


import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.theartofdev.edmodo.cropper.CropImageView
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.TAG
import kotlinx.android.synthetic.main.fragment_crop.*
import java.io.File
import java.io.FileOutputStream

/**
 * A simple [Fragment] subclass.
 */
class CropFragment : Fragment() {

    private val args: ObstacleEditFragmentArgs by navArgs()
    private lateinit var currentObstacle: Obstacle
    private lateinit var cropImageView: CropImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Set toolbar menu
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_crop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initialize view and obstacle
        cropImageView = crop_image_view
        currentObstacle = args.currentObstacle

        // Attempt to read photo file
        val photoFile: File? = try {
            File(currentObstacle.photo)
        } catch (ex: IllegalArgumentException) {
            Log.e(TAG(), "Error getting image file")
            null
        }

        // Set photo in crop view
        photoFile?.also {
            context?.also { context ->
                val photoUri =
                    FileProvider.getUriForFile(
                        context, "com.example.android.fileprovider", it
                    )
                crop_image_view.setImageUriAsync(photoUri)
                crop_image_view.isAutoZoomEnabled = true
                crop_image_view.scaleType = CropImageView.ScaleType.FIT_CENTER
                crop_image_view.isShowProgressBar = true
            }
        }

        // Listen for cropped photo
        cropImageView.setOnCropImageCompleteListener { _, result ->
            // cropImageView.setImageBitmap(result.bitmap)

            // overwrite original photo file https://stackoverflow.com/a/673014/3755276
            // this action probably happens on the UI thread
            // TODO move action to background thread
            result.bitmap.compress(
                Bitmap.CompressFormat.JPEG, 100, FileOutputStream(photoFile!!)
            )

            // Navigate back to edit fragment
            findNavController().navigate(
                CropFragmentDirections.actionCropFragmentToObstacleEditFragment(currentObstacle)
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_crop, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_rotate_photo -> {
                cropImageView.rotateImage(90)
                true
            }
            R.id.action_confirm_edit_photo -> {
                // Crops image, OnCropImageCompleteListener above listens for result
                cropImageView.getCroppedImageAsync()
                true
            }
            R.id.action_cancel_edit_photo -> {
                // Cancel by navigating back to edit fragment
                findNavController().navigate(
                    CropFragmentDirections.actionCropFragmentToObstacleEditFragment(currentObstacle)
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
