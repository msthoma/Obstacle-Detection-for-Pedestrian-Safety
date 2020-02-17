package cy.org.rise.obsai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.otaliastudios.cameraview.CameraView
import kotlinx.android.synthetic.main.activity_camera.*


class CameraFragment : Fragment() {
    private lateinit var cameraView: CameraView

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
    }
}
