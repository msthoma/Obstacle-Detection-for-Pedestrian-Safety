package cy.org.rise.obsai.ui

import android.os.Bundle
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment

class AppIntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Do NOT call setContentView here

        addSlide(
            AppIntroFragment.newInstance(
                title = "Welcome",
                description = "Description"
            )
        )
    }
}