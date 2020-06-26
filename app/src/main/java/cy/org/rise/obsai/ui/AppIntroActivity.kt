package cy.org.rise.obsai.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import cy.org.rise.obsai.R

class AppIntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Do NOT call setContentView here

        isWizardMode = true
//        showStatusBar(false)
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
            AppIntroFragment.newInstance(
                title = "Welcome!",
                description = "The app allows pedestrians to report road obstacles to the " +
                        "iNicosia platform.",
                imageDrawable = R.drawable.ic_noun_barrier_2895012,
                backgroundColor = resources.getColor(R.color.introColor1)
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                title = "Permissions required",
                description = "For ease of use, the app requires access to the phone's camera and" +
                        " GPS sensor, please grant them in the popup that will appear.",
                imageDrawable = R.drawable.ic_rise_banner,
                backgroundColor = resources.getColor(R.color.introColor2)
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                title = "Privacy policy",
                description = "Please agree to the policy",
                imageDrawable = R.drawable.ic_rise_banner,
                backgroundColor = resources.getColor(R.color.introColor3)
            )
        )
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
//        findNavController().navigate(R.id.action_global_obstacleListFragment)
        finish()
    }
}