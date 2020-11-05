package cy.org.rise.obsai.utils

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import android.view.inputmethod.InputMethodManager.SHOW_FORCED
import android.widget.Toast
import androidx.annotation.StringRes
import java.util.*

/**
 * Returns activity name for logging purposes, see discussions
 * [1](https://stackoverflow.com/a/52956934) and  [2](https://stackoverflow.com/a/57123619).
 *
 * @param msg string for displaying next to current class name
 */
@Suppress("unused")
inline fun <reified T> T.TAG(msg: String = "") =
    T::class.java.simpleName + if (msg.isNotEmpty()) " ($msg)" else ""

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
 * Formats a float array to a string, while rounding each float.
 *
 * @return Float array as formatted string
 */
fun FloatArray.formatAsStr(): String = this.joinToString(
    separator = ", ", transform = { fl -> fl.roundTo(3).toString() })

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
            Log.d(TAG(), "Created device unique ID $installUniqueID")
            putString(Constants.PREF_UNIQUE_ID_KEY, installUniqueID)
            apply()
        }
    }

    return installUniqueID
}

/**
 * Provides an indication of whether the app Intro tutorial has been already shown or not, based
 * on a SharedPreference. If the preference does not exist yet, it is created with a default
 * value of false.
 *
 * @param setToShown whether to set the preference to shown or not
 * @return boolean of whether the app intro has been shown already or not
 */
fun Context.introStatus(setToShown: Boolean = false): Boolean {
    val shPref = this.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    // makes sure pref is created if it does not exist yet
    if (!shPref.contains(Constants.INTRO_SHOWN_KEY)) {
        with(shPref.edit()) {
            putBoolean(Constants.INTRO_SHOWN_KEY, false)
            apply()
        }
    }

    if (setToShown) {
        with(shPref.edit()) {
            putBoolean(Constants.INTRO_SHOWN_KEY, true)
            apply()
        }
    }

    return shPref.getBoolean(Constants.INTRO_SHOWN_KEY, false)
}

/**
 * Allows showing a toast from wherever context is available.
 *
 * @param resId resource ID of toast message
 * @param duration one of either Toast.LENGTH_SHORT or Toast.LENGTH_LONG (default), representing
 * the duration the toast will stay on screen
 */
fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_LONG) =
    Toast.makeText(this, resId, duration).show()

/**
 * Extension function that hides soft keyboard.
 *
 * See following links for sources:
 * - https://stackoverflow.com/a/59780666
 * - https://stackoverflow.com/a/58559801
 * - https://stackoverflow.com/a/59758327
 * */
fun View.hideKeyboard() = (context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
    .hideSoftInputFromWindow(windowToken, 0)

/**
 * Extension function that displays soft keyboard.
 *
 * See following links for sources:
 * - https://stackoverflow.com/a/59780666
 * - https://stackoverflow.com/a/58559801
 * - https://stackoverflow.com/a/59758327
 * */
fun View.showKeyboard() = (context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
    .toggleSoftInput(SHOW_FORCED, HIDE_IMPLICIT_ONLY)

/**
 * Toggles visibility of a view between View.VISIBLE and View.GONE.
 */
fun View.toggleVisibility() =
    if (this.visibility == View.VISIBLE) this.visibility = View.GONE else this.visibility = View
        .VISIBLE
