package cy.org.rise.obsai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import cy.org.rise.obsai.R
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Activity that serves as the home of the nav_host_fragment, which is used by the Navigation
 * Component library for most of the app's workflow.
 *
 * For setting up the toolbar to work with the Navigation component, see the following links,
 * especially the first:
 * - [https://stackoverflow.com/a/55930024]
 * - [https://stackoverflow.com/q/30721664]
 * - [https://stackoverflow.com/a/55380395]
 * - [https://developer.android.com/guide/navigation/navigation-ui#action_bar]
 */
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            appBarConfiguration
        )
    }

    /**
     * By overriding this method, custom behaviour is added when navigating up in specific
     * fragments.
     *
     * @return
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)

        return when (navController.currentDestination?.id) {
            // when Up is pressed in obstacleEditFragment, call onBackPressed instead of
            // navigateUp, so that an OnBackPressedCallback is received in the fragment, to
            // display a discard dialog, which should happen for both Up + Back
            // see https://stackoverflow.com/a/55930024
            R.id.obstacleEditFragment -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> navController.navigateUp()
        }
    }
}
