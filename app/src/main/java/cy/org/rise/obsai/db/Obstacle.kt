package cy.org.rise.obsai.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
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
 * @property orientation orientation of phone in space when obstacle was recorded
 */
@Entity(tableName = "obstacle_table")
data class Obstacle(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo
    val timeStamp: Date = Calendar.getInstance().time,

    @ColumnInfo
    var obstacleType: String,

    @ColumnInfo
    var photoPath: String,

    @Embedded(prefix = "location_")
    var location: Location,

    @Embedded(prefix = "orientation_")
    val orientation: Orientation

) : Serializable {

    /**
     * Simple data class to store obstacle location.
     *
     * NOTE: Android uses Lat, Long but server Long, Lat, see [https://macwright.org/lonlat/].
     * Careful with conversions between the two!
     *
     * @property latitude
     * @property longitude
     */
    data class Location(
        //
        var latitude: Double,
        var longitude: Double
    )

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
    )

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
}