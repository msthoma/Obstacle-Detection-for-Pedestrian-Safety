package cy.org.rise.obsai.utils

import android.content.Context
import android.content.SharedPreferences
import cy.org.rise.obsai.R

/**
 * Session manager to save and fetch data from SharedPreferences
 * Based on this https://android.jlelse.eu/token-authorization-with-retrofit-android-oauth-2-0-747995c79720
 */
class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.app_name),
        Context.MODE_PRIVATE
    )

    companion object {
        const val ACCESS_TOKEN = "accessToken"
    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(ACCESS_TOKEN, token)
        editor.apply()
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(ACCESS_TOKEN, null)
    }
}