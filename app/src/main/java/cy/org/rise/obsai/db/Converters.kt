package cy.org.rise.obsai.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Class with Room database
 * [type converters](https://developer.android.com/reference/androidx/room/TypeConverter), used
 * for saving complex data in Room DB.
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

    /**
     * Converts map of strings and floats to string, based on
     * [this](https://stackoverflow.com/a/57246287).
     *
     * @param value string to convert back to map
     * @return map of obstacle types and probabilities
     */
    @TypeConverter
    fun stringToMapOfStringFloat(value: String): Map<String, Float>? {
        return if (value == "") null else Gson().fromJson(value, object : TypeToken<Map<String,
                Float>>() {}.type)
    }

    /**
     * Converts string to map of strings and floats, based on
     * [this](https://stackoverflow.com/a/57246287).
     *
     * @param value map of obstacle types and probabilities
     * @return map converted to string
     */
    @TypeConverter
    fun mapStingFloatToString(value: Map<String, Float>?): String {
        return value?.let { Gson().toJson(it) } ?: ""
    }
}
