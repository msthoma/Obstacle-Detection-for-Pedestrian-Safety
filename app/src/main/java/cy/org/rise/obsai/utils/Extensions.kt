package cy.org.rise.obsai.utils

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import android.view.inputmethod.InputMethodManager.SHOW_FORCED
import java.util.*

/**
 * Returns activity name for logging purposes, see discussions
 * [1](https://stackoverflow.com/a/52956934) and  [2](https://stackoverflow.com/a/57123619).
 */
inline fun <reified T> T.TAG(): String = T::class.java.simpleName

/**
 * Rounds double to specified decimal points, see [discussion](https://stackoverflow.com/a/59513133).
 *
 * @param n Number of decimal points to round to
 * @return Rounded double
 */
fun Double.roundTo(n: Int = 3): Double = "%.${n}f".format(Locale.ENGLISH, this).toDouble()

/**
 * Rounds float to specified decimal points, see [discussion](https://stackoverflow.com/a/59513133).
 *
 * @param n Number of decimal points to round to
 * @return Rounded float
 */
fun Float.roundTo(n: Int = 3): Float = "%.${n}f".format(Locale.ENGLISH, this).toFloat()

/**
 * Returns a unique ID for current app install - if such as ID does not exist yet, it is
 * automatically created, using a one-time generated random UUID.
 *
 * The unique app install ID will persist while the app is installed, but will be reset if app is
 * reinstalled.
 *
 * @return unique ID for current app install
 */
fun Context.getUniqueAppInstallID(): String {
    val shPref = this.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    var installUniqueID = shPref.getString(
        Constants.PREF_UNIQUE_ID_KEY, Constants.PREF_UNIQUE_ID_EMPTY
    ) ?: Constants.PREF_UNIQUE_ID_EMPTY

    // In case ID is empty, create a new one
    if (installUniqueID == Constants.PREF_UNIQUE_ID_EMPTY) {
        with(shPref.edit()) {
            installUniqueID = UUID.randomUUID().toString()
            android.util.Log.d(TAG(), "Created device unique ID $installUniqueID")
            putString(Constants.PREF_UNIQUE_ID_KEY, installUniqueID)
            apply()
        }
    }

    return installUniqueID
}

fun View.hideKeyboard() = (context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
    .hideSoftInputFromWindow(windowToken, 0)

fun View.showKeyboard() {
    val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(SHOW_FORCED, HIDE_IMPLICIT_ONLY)
}