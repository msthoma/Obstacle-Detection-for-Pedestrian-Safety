package cy.org.rise.obsai.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.api.RestObstacle.*
import java.io.Serializable
import java.util.*

/**
 * Data class for obstacles, used for Room database entities.
 *
 * Class implements [Serializable], which is required for passing Obstacle objects as safe args
 * by the Navigation component library.
 *
 * It is perhaps possible to utilise only one class for both [Obstacle] and [RestObstacle], see
 * Git history for failed attempt to do so. See discussions
 * [1](https://stackoverflow.com/q/39199426/3755276) &
 * [2](https://medium.com/holisticon-consultants/kotlin-data-class-mapping-aa0f9f750ca1) for
 * strategies of using the same class for both Room and Retrofit.
 * The [Obstacle.toRestObstacle] and [RestObstacle.toObstacle] methods are used as a compromise for
 * converting between the two.
 *
 * @property id obstacle id with UUID value, also primary key
 * @property timeStamp date of object creation in millis
 * @property obstacleType type of the obstacle, e.g crack, no pavement etc.
 * @property photoPath where the photo file is located
 * @property location geo coordinates of the obstacle
 * @property locationFromGPS holds a copy of the GPS-determined location, in case user manually
 * edits location
 * @property orientation orientation of phone in space when obstacle was recorded
 * @property typeProbabilitiesCNN map of obstacle types with their probabilities as predicted by
 * the convolutional neural network
 */
@Entity(tableName = "obstacle_table")
data class Obstacle(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // placeholder for device id, final form TBD soon (will be String for certain)
    @ColumnInfo
    val deviceID: String = "d3869c03-6f52-4bcc-991f-a2775d709b5b",

    @ColumnInfo
    val timeStamp: Date = Calendar.getInstance().time,

    @ColumnInfo
    var obstacleType: String,

    @ColumnInfo
    var photoPath: String,

    @Embedded
    var location: Location,

    @Embedded(prefix = "fused_")
    var orientation: Orientation = Orientation(0.0, 0.0, 0.0),

    @Embedded(prefix = "accelerometer_")
    var accelerometer: Orientation = Orientation(0.0, 0.0, 0.0),

    @Embedded(prefix = "compass_")
    var compass: Orientation = Orientation(0.0, 0.0, 0.0),

    @ColumnInfo
    var altitude: Double = 0.0,

    @ColumnInfo
    var typeProbabilitiesCNN: Map<String, Float>? = null,

    @ColumnInfo
    var uploadStatus: String = "Uploading..."

) : Serializable {

    @Embedded(prefix = "GPS_")
    var locationFromGPS: Location = location.copy()

    /**
     * Simple data class to store obstacle location.
     *
     * NOTE: Android uses [Lat, Long] but server [Long, Lat], see [https://macwright.org/lonlat/].
     * Careful with conversions between the two!
     *
     * @property latitude
     * @property longitude
     */
    data class Location(
        //
        var latitude: Double,
        var longitude: Double
    ) : Serializable

    /**
     * Simple data class to store phone orientation in space.
     *
     * @property x
     * @property y
     * @property z
     */
    data class Orientation(
        var x: Double,
        var y: Double,
        var z: Double
    ) : Serializable

    /**
     * Converts Obstacle entity to RestObstacle entity.
     *
     * @return RestObstacle entity
     */
    fun toRestObstacle(): RestObstacle = RestObstacle(
        id = id,
        timeStamp = TimeStamp(timeStamp),
        obstacleType = ObstacleType(obstacleType),
        photoPath = PhotoPath(photoPath),
        location = RestObstacle.Location(
            // NOTE REVERSAL OF LAT, LONG -> LONG, LAT
            longitude = location.longitude,
            latitude = location.latitude
        ),
        orientation = RestObstacle.Orientation(orientation.x, orientation.y, orientation.z)
    )

    /**
     * Sets obstacle location from LatLng object.
     *
     * @param latLong Google Maps LatLng object
     */
    fun setLocationFromLatLong(latLong: LatLng) {
        location = Location(latitude = latLong.latitude, longitude = latLong.longitude)
    }

    /**
     * Returns obstacle location as LatLng object.
     *
     * @return Obstacle location as Google Maps LatLng
     */
    fun getLocationAsLatLong(): LatLng = LatLng(location.latitude, location.longitude)

    /**
     * Converts obstacle to JSON object using Gson.
     *
     * @return JSON string of current obstacle
     */
    fun toJson(): String = Gson().toJson(this)
}