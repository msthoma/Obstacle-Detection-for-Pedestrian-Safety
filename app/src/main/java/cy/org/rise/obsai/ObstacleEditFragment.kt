package cy.org.rise.obsai


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_obstacle_edit.*
import java.io.File
import java.io.IOException

class ObstacleEditFragment : Fragment() {

    val args: ObstacleEditFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_obstacle_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG(), args.currentPhoto)
        val photoFile: File? = try {
            File(args.currentPhoto)
        } catch (ex: IOException) {
            Log.e(TAG(), "Error creating image file")
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
            }
        }

        ArrayAdapter.createFromResource(
            context!!, // TODO fix !!
            R.array.obstacles_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = arrayAdapter
        }
    }
}
