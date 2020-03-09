package cy.org.rise.obsai.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cy.org.rise.obsai.BuildConfig
import cy.org.rise.obsai.R
import cy.org.rise.obsai.utils.TAG

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
            if (viewClicks == 5) {
                throw RuntimeException("This crash was intentional!")
            }
            true
        }

        // click listener for libraries activity
        findPreference<Preference>("open_source_libraries")?.setOnPreferenceClickListener {
            startActivity(Intent(activity, AboutActivity::class.java))
            true
        }
    }
}
