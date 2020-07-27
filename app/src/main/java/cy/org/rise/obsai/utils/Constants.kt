package cy.org.rise.obsai.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * A list of constants used throughout the app.
 */
object Constants {
    // Key for obstacles in Work Manager
    const val KEY_OBSTACLE_JSON = "KEY_OBSTACLE_ENTITY"

    // SharedPreferences file name
    const val PREFERENCE_FILE_KEY = "cy.org.rise.obsai.PREFERENCE_FILE_KEY"

    // Keys for device unique ID (actually per install unique id)
    const val PREF_UNIQUE_ID_KEY = "PREF_UNIQUE_ID_KEY"
    const val PREF_UNIQUE_ID_EMPTY = "EMPTY"

    // Key for pref that determines whether the app intro tutorial has been shown or not
    const val INTRO_SHOWN_KEY = "INTRO_SHOWN_KEY"

    // Map related constants
    val CYPRUS = LatLngBounds(
        // Bounds for map view, for only the general area of Cyprus
        LatLng(34.520142, 32.186723), // Southwest corner
        LatLng(35.738372, 34.644546) // Northeast corner
    )
    const val CITY_ZOOM_LEVEL = 12f
    const val DEFAULT_ZOOM_LEVEL = 16f
    const val MIN_ZOOM_LEVEL = 7.5f
}
