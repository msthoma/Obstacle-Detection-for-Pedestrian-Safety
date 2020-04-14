package cy.org.rise.obsai.db

import androidx.room.TypeConverter
import java.util.*

/**
 * Class with Room database
 * [type converters](https://developer.android.com/reference/androidx/room/TypeConverter).
 */
class Converters {
    /**
     * Converts date in millis to Date.
     *
     * @param value date in millis
     * @return date as Date object
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Converts Date to millis.
     *
     * @param date date as Date
     * @return date as millis
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
