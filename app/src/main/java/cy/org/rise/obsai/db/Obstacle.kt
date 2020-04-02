package cy.org.rise.obsai.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.api.RestObstacle.*
import java.io.Serializable
import java.util.*

/*
* parameters with @Ignore are ignored by Room
* parameters with @Expose are used by Gson, for converting entities to json
*
* see these discussions for other strategies of using the same class for both Room and Retrofit:
* https://stackoverflow.com/q/39199426/3755276
* https://medium.com/holisticon-consultants/kotlin-data-class-mapping-aa0f9f750ca1
*
* Class implements Serializable to be able to be passed between fragments as SafeArg with
* Navigation components library
* */

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

    data class Location(
        var latitude: Double,
        var longitude: Double
    )

    data class Orientation(
        var x: Double,
        var y: Double,
        var z: Double
    )

    fun toRestObstacle(): RestObstacle = RestObstacle(
        id = id,
        timeStamp = TimeStamp(timeStamp),
        obstacleType = ObstacleType(obstacleType),
        photoPath = PhotoPath(photoPath),
        location = RestObstacle.Location(location.latitude, location.longitude),
        orientation = RestObstacle.Orientation(orientation.x, orientation.y, orientation.z)
    )
}