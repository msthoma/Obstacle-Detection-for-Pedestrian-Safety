package cy.org.rise.obsai.api

import cy.org.rise.obsai.db.Obstacle
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

data class RestObstacle(
    val id: String,

    val timeStamp: TimeStamp,

    var obstacleType: ObstacleType,

    var photoPath: PhotoPath,

    var location: Location,

    val orientation: Orientation

) : Serializable {

    val type: String = "Obstacle" // required by Orion as entity type

    data class TimeStamp(
        val value: Date,
        val type: String = "Date"
    )

    data class PhotoPath(
        val value: String,
        val type: String = "Text"
    )

    data class ObstacleType(
        val value: String,
        val type: String = "Text"
    )

    data class Location(
        // NOTE: Android uses Lat, Long but server Long, Lat, see https://macwright.org/lonlat/
        // Careful with conversions between the two!
        @Transient
        var longitude: Double,
        @Transient
        var latitude: Double
    ) {
        val value: Value = Value(listOf(longitude, latitude))

        val type: String = "geo:json"

        data class Value(
            val coordinates: List<Double>,
            val type: String = "Point"
        )
    }

    data class Orientation(
        @Transient
        var x: Double,
        @Transient
        var y: Double,
        @Transient
        var z: Double
    ) {
        var value: List<Double> = listOf(x, y, z)
        val type: String = "Text" // TODO is there a better type?? list of doubles, or coords
    }

    fun toObstacle(): Obstacle = Obstacle(
        id = id,
        timeStamp = timeStamp.value,
        obstacleType = obstacleType.value,
        photoPath = photoPath.value,
        location = Obstacle.Location(
            // NOTE REVERSAL OF LONG, LAT -> LAT, LONG
            latitude = location.value.coordinates[1],
            longitude =  location.value.coordinates[0]
        ),
        orientation = Obstacle.Orientation(
            orientation.value[0],
            orientation.value[1],
            orientation.value[2]
        )
    )
}