package cy.org.rise.obsai


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.theartofdev.edmodo.cropper.CropImageView
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.TAG
import kotlinx.android.synthetic.main.fragment_crop.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class CropFragment : Fragment() {

    val args: ObstacleEditFragmentArgs by navArgs()
    private lateinit var currentObstacle: Obstacle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentObstacle = args.currentObstacle!! // TODO fix !!
        val photoFile: File? = try {
            File(currentObstacle.photo)
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
                crop_image_view.setImageUriAsync(photoUri)
                crop_image_view.isAutoZoomEnabled = true
                crop_image_view.scaleType = CropImageView.ScaleType.FIT_CENTER
                crop_image_view.isShowProgressBar = true
            }
        }

        button_rotate.setOnClickListener { crop_image_view.rotateImage(90) }

        button_crop.setOnClickListener {
            findNavController().navigate(
                CropFragmentDirections.actionCropFragmentToObstacleEditFragment(currentObstacle)
            )
        }
    }
}
