package cy.org.rise.obsai.ui

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.ui.LibsFragment
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment
import cy.org.rise.obsai.R

import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fragment: LibsSupportFragment = LibsBuilder().supportFragment()

        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

}
