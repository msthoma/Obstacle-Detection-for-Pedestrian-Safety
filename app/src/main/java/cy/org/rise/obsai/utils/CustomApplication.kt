package cy.org.rise.obsai.utils

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import com.jakewharton.threetenabp.AndroidThreeTen
import cy.org.rise.obsai.BuildConfig
import cy.org.rise.obsai.R
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraDialog
import org.acra.annotation.AcraMailSender
import org.acra.data.StringFormat

/**
 * CustomApplication is used to enable:
 *  - initialization of the ACRA error reporting library
 *  - initialization of ThreeTenABP
 *  - custom implementation of getWorkManagerConfiguration()
 */
@AcraCore(buildConfigClass = BuildConfig::class, reportFormat = StringFormat.JSON)
// fill in the address that should receive crash reports
@AcraMailSender(mailTo = "your.email@example.com")
@AcraDialog(
    resTitle = R.string.acra_crash_dialog_title,
    resPositiveButtonText = R.string.dialog_OK_button,
    resNegativeButtonText = R.string.dialog_cancel_button,
    resText = R.string.acra_crash_message,
    resTheme = R.style.Theme_AppCompat_Dialog,
    resCommentPrompt = R.string.acra_crash_input_prompt
)
class CustomApplication : Application(), Configuration.Provider {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }

    // initialize ThreeTenABP https://github.com/JakeWharton/ThreeTenABP/
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }

    // see https://developer.android.com/topic/libraries/architecture/workmanager/advanced/custom-configuration#implement-configuration-provider
    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO).build()
}
