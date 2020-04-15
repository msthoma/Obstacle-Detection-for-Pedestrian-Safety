package cy.org.rise.obsai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment
import cy.org.rise.obsai.R
import kotlinx.android.synthetic.main.activity_about.*

/**
 * Used to display a list of the Open source libraries used in the app
 */
class AboutActivity : AppCompatActivity() {

    // back button correctly goes back to settings fragment, but up goes to list fragment instead

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fragment: LibsSupportFragment = LibsBuilder().supportFragment()

        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }
}
