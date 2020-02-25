package cy.org.rise.obsai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment
import cy.org.rise.obsai.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fragment: LibsSupportFragment = LibsBuilder()
            .withAboutAppName(resources.getString(R.string.app_name))
            .withAboutDescription("App allows reporting obstacles in a city.")
            .supportFragment()

        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }
}
