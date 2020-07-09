package cy.org.rise.obsai.db

import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.api.RestObstacle.*
import cy.org.rise.obsai.utils.Constants
import java.io.Serializable
import java.util.*

/**
 * Data class for obstacles, used for Room database entities.
 *
 * Class (and its subclasses) implement [Serializable], which is required for passing Obstacle
 * objects as safe args by the Navigation component library.
 *
 * It is perhaps possible to utilise only one class for both [Obstacle] and [RestObstacle], see
 * Git history for failed attempt to do so. See discussions
 * [1](https://stackoverflow.com/q/39199426/3755276) &
 * [2](https://medium.com/holisticon-consultants/kotlin-data-class-mapping-aa0f9f750ca1) for
 * strategies of using the same class for both Room and Retrofit.
 * The [Obstacle.toRestObstacle] and [RestObstacle.toObstacle] methods are used as a compromise for
 * converting between the two.
 *
 * ## Location information
 * For details on location data, see docs for
 * [Location](https://developer.android.com/reference/android/location/Location) and
 * [LocationManager](https://developer.android.com/reference/android/location/LocationManager).
 *
 * ## Orientation information
 * For details on accelerometer, compass and orientation data, see docs for
 * [Position sensors](https://developer.android.com/guide/topics/sensors/sensors_position).
 *
 * ## Notes on Unique identifiers
 * Using unique identifiers in an app is a complicated subject, the article
 * [Best practices for unique identifiers](https://developer.android.com/training/articles/user-data-ids)
 * provides a good summary. Also this [comment](https://stackoverflow.com/a/59093659) (and that
 * discussion in general) is a good source of information. The linked comment points out that any
 * use of an identifier that can be linked to an individual has to comply with GDPR
 * ([link](https://gdpr.eu/eu-gdpr-personal-data/)). In this app a unique identifier is generated
 * for each app install, the first time [cy.org.rise.obsai.utils.getUniqueAppInstallID] is called,
 * and is used for as long as the app remains installed (uninstalling and reinstalling the app
 * will of course generate a new identifier).
 *
 * @property accelerometer accelerometer reading at the time the photo was captured
 * @property altitude altitude in meters, at the location of the obstacle
 * @property compass compass reading at the time the photo was captured
 * @property appInstallID an id unique to the current app install
 * @property id obstacle id with UUID value, also primary key
 * @property locationAccuracy horizontal radial accuracy of the location reading, in meters
 * @property location coordinates of the obstacle (may be modified by user, original saved below)
 * @property locationFromGPS holds a copy of the GPS-determined location
 * @property obstacleType type of the obstacle, as reported by user
 * @property orientation orientation of phone when photo was taken (calculated by fusing
 * accelerometer and compass readings)
 * @property photoPath path to the photo file
 * @property timeStamp date of object creation
 * @property typeProbabilitiesCNN map of obstacle types and their probabilities as predicted by
 * the convolutional neural network
 */
@Entity(tableName = "obstacle_table")
data class Obstacle(
    @PrimaryKey @SerializedName("_id") @Expose
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo @Expose
    var appInstallID: String = Constants.PREF_UNIQUE_ID_EMPTY,

    @ColumnInfo @Expose
    val timeStamp: Date = Calendar.getInstance().time,

    @ColumnInfo @Expose
    var obstacleType: String,

    @ColumnInfo @Expose
    var photoPath: String,

    @Embedded @Expose
    var location: Location,

    @ColumnInfo @Expose
    var locationAccuracy: Float = 0.0f,

    @Embedded(prefix = "fused_") @Expose
    var orientation: Orientation = Orientation(0.0, 0.0, 0.0),

    @Embedded(prefix = "accelerometer_") @Expose
    var accelerometer: Orientation = Orientation(0.0, 0.0, 0.0),

    @Embedded(prefix = "compass_") @Expose
    var compass: Orientation = Orientation(0.0, 0.0, 0.0),

    @ColumnInfo @Expose
    var altitude: Double = 0.0,

    @ColumnInfo @Expose
    var typeProbabilitiesCNN: Map<String, Float>? = null,

    @ColumnInfo
    var uploadStatus: String = "Uploading..."

) : Serializable {

    @Embedded(prefix = "GPS_")
    @Expose
    var locationFromGPS: Location = location.copy()

    /**
     * Simple data class to store obstacle location.
     *
     * @property latitude
     * @property longitude
     */
    data class Location(
        var latitude: Double,
        var longitude: Double
    ) : Serializable {
        @Expose
        @Ignore
        var coordinates = listOf(latitude, longitude)
    }

    /**
     * Simple data class to store phone orientation in space.
     *
     * @property x
     * @property y
     * @property z
     */
    data class Orientation(
        @Expose var x: Double,
        @Expose var y: Double,
        @Expose var z: Double
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
