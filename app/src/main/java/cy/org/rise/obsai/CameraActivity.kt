package cy.org.rise.obsai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity() {
    private lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // most of the other settings for CameraView are set in the activity's xml layout
        cameraView = camera_view
        cameraView.setLifecycleOwner(this)
        cameraView.addCameraListener(Listener())
    }

    private class Listener : CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
        }
    }
}
