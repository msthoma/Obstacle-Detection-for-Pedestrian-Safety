package cy.org.rise.obsai.ui.appIntro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import cy.org.rise.obsai.R

/**
 * Based on AppIntroCustomLayoutFragment of Intro library
 *
 */
class CustomIntroFragment : Fragment(), SlideBackgroundColorHolder {

    private var layoutResId = 0
    private var mainLayout: LinearLayout? = null

    override var defaultBackgroundColor: Int = 0
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        layoutResId = arguments?.getInt(ARG_LAYOUT_RES_ID) ?: 0
        defaultBackgroundColor = arguments?.getInt(ARG_BG_COLOR) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layoutResId, container, false)
        mainLayout = view.findViewById(R.id.custom_intro_fragment_layout)
        mainLayout?.setBackgroundColor(defaultBackgroundColor)
        return view
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        mainLayout?.setBackgroundColor(backgroundColor)
    }

    companion object {
        private const val ARG_LAYOUT_RES_ID = "layoutResId"
        private const val ARG_BG_COLOR = "bg_color"

        @JvmStatic
        fun newInstance(layoutResID: Int, backgroundColor: Int): CustomIntroFragment {
            val args = Bundle()
            args.apply {
                putInt(ARG_LAYOUT_RES_ID, layoutResID)
                putInt(ARG_BG_COLOR, backgroundColor)
            }
            return CustomIntroFragment().also { it.arguments = args }
        }
    }
}