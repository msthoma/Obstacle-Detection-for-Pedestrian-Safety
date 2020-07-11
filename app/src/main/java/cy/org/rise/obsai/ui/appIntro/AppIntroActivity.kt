package cy.org.rise.obsai.ui.appIntro

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroPageTransformerType
import cy.org.rise.obsai.R
import cy.org.rise.obsai.utils.TAG
import cy.org.rise.obsai.utils.introStatus

/**
 * Activity for showing an intro tutorial when the app is launched for the first time.
 *
 * Based on the [AppIntro library](https://github.com/AppIntro/AppIntro).
 */
class AppIntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Do NOT call setContentView here

        // TODO figure out back button
        isSystemBackButtonLocked = true

        isWizardMode = true
//        showStatusBar(true)
//        setStatusBarColorRes(R.color.introColor1)
//        setImmersiveMode()
        isColorTransitionsEnabled = true

        setTransformer(
            AppIntroPageTransformerType.Parallax(
                titleParallaxFactor = 2.0,
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

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        Log.d("onSlideChanged", newFragment?.tag.toString())

    }

    override fun onNextPressed(currentFragment: Fragment?) {
        super.onNextPressed(currentFragment)
        Log.d("onNextPressed", "${currentFragment?.tag}")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG(), "back pressed")
        // TODO: 11/07/20 better way to handle back presses
        if (applicationContext.introStatus()) finish() else this.finishAffinity()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        applicationContext.introStatus(setToShown = true)
        // TODO figure out navigation
//        findNavController().navigate(R.id.action_global_obstacleListFragment)
        finish()
    }
}
