package cy.org.rise.obsai.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.michaelflisar.changelog.ChangelogBuilder
import cy.org.rise.obsai.BuildConfig
import cy.org.rise.obsai.R
import cy.org.rise.obsai.utils.TAG

/**
 * Fragment that displays the app's settings.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // set app version
        findPreference<Preference>("version")?.summary =
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        // intentionally crash app to test crash reporting, triggered by repeated count view clicks
        var viewClicks = 0
        findPreference<Preference>("version")?.setOnPreferenceClickListener {
            viewClicks += 1
            Log.d(TAG(), "$viewClicks view clicks...")
            if (viewClicks == 20) {
                throw RuntimeException("This crash was intentional!")
            }
            true
        }

        // navigate to intro
        findPreference<Preference>("appIntro")?.setOnPreferenceClickListener {
            // TODO: 11/07/20 figure out intro navigation
            findNavController().navigate(R.id.action_settingsFragment_to_appIntroActivity)
            true
        }

        // show changelog
        findPreference<Preference>("changelog")?.setOnPreferenceClickListener {
            ChangelogBuilder()
                .withUseBulletList(true)
                .withTitle("Changelog")
                .buildAndShowDialog(activity as AppCompatActivity?, false)
            true
        }

        // click listener for libraries activity
        findPreference<Preference>("open_source_libraries")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutActivity)
            true
        }
    }
}
