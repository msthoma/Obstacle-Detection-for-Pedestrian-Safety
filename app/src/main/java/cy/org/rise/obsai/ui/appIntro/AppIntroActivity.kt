package cy.org.rise.obsai.ui.appIntro

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroPageTransformerType
import cy.org.rise.obsai.R

class AppIntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Do NOT call setContentView here

        isWizardMode = true
//        showStatusBar(true)
//        setStatusBarColorRes(R.color.introColor1)
//        setImmersiveMode()
        isColorTransitionsEnabled = true

        setTransformer(
            AppIntroPageTransformerType.Parallax(
                titleParallaxFactor = 1.0,
                imageParallaxFactor = -1.0,
                descriptionParallaxFactor = 2.0
            )
        )

        addSlide(
            CustomIntroFragment.newInstance(
                R.layout.fragment_intro_1,
                resources.getColor(R.color.introColor1)
            )
        )
        addSlide(
            CustomIntroFragment.newInstance(
                R.layout.fragment_intro_2,
                resources.getColor(R.color.introColor2)
            )
        )
        addSlide(
            CustomIntroFragment.newInstance(
                R.layout.fragment_intro_3,
                resources.getColor(R.color.introColor3)
            )
        )
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
//        findNavController().navigate(R.id.action_global_obstacleListFragment)
        finish()
    }
}