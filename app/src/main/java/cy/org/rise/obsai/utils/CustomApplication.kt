package cy.org.rise.obsai.utils

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import cy.org.rise.obsai.BuildConfig
import cy.org.rise.obsai.R
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraDialog
import org.acra.annotation.AcraMailSender
import org.acra.data.StringFormat
import java.util.*

/**
 * CustomApplication is used to enable:
 *  - initialization of the ACRA error reporting library
 *  - custom implementation of getWorkManagerConfiguration()
 *  - create a device unique ID at first app launch
 */
@AcraCore(buildConfigClass = BuildConfig::class, reportFormat = StringFormat.JSON)
@AcraMailSender(mailTo = "msthoma@outlook.com")
@AcraDialog(
    resTitle = R.string.acra_crash_dialog_title,
    resPositiveButtonText = R.string.acra_crash_positive_button,
    resNegativeButtonText = R.string.acra_crash_negative_button,
    resText = R.string.acra_crash_message,
    resTheme = R.style.Theme_AppCompat_Dialog,
    resCommentPrompt = R.string.acra_crash_input_prompt
)
class CustomApplication : Application(), Configuration.Provider {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Initiate ACRA
        ACRA.init(this)

        // Check if device unique ID exists, and if not create one
        // The unique ID will persist while the app is installed, but will be reset if app is
        // reinstalled
        val shPref = base.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

        if (shPref.getString(
                Constants.PREF_UNIQUE_ID_KEY, Constants.PREF_UNIQUE_ID_DEFAULT
            ) == Constants.PREF_UNIQUE_ID_DEFAULT
        ) {
            with(shPref.edit()) {
                val newUniqueDeviceID = UUID.randomUUID().toString()
                Log.d(TAG(), "Created device unique ID $newUniqueDeviceID")
                putString(Constants.PREF_UNIQUE_ID_KEY, newUniqueDeviceID)
                apply()
            }
        }
    }

    // see https://developer.android.com/topic/libraries/architecture/workmanager/advanced/custom-configuration#implement-configuration-provider
    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO).build()
}
