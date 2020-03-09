package cy.org.rise.obsai

import android.app.Application
import android.content.Context
import android.util.Log
import cy.org.rise.obsai.utils.TAG
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraDialog
import org.acra.annotation.AcraMailSender
import org.acra.data.StringFormat


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
class CustomApplication : Application() {
    override fun attachBaseContext(base: Context) {
        Log.d(TAG(), "Override attachBaseContext, to initialize ACRA")
        super.attachBaseContext(base)
        ACRA.init(this)
    }
}