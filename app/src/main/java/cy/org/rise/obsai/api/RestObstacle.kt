package cy.org.rise.obsai.api

import cy.org.rise.obsai.db.Obstacle
import java.io.Serializable
import java.util.*

/**
 * Data class that allows for proper deserialization of [Obstacle] objects to JSON by the Gson
 * library. See Obstacle documentation for further information.
 *
 * The class has essentially the same fields as Obstacle, but each field here is a data class
 * that includes the value as well as the type of the field (e.g. type = "Text") for proper
 * deserialization to JSON, as shown in the example below:
 * ```
 * "obstacleType": {
 *     "type": "Text",
 *     "value": "Cracked pavement"
 * }
 * ```
 *
 * Fields annotated with @Transient are ignored during deserialization by Gson.
 *
 * @property id
 * @property timeStamp
 * @property obstacleType
 * @property photoPath
 * @property location
 * @property orientation
 */
data class RestObstacle(
    val id: String,

    val timeStamp: TimeStamp,

    var obstacleType: ObstacleType,

    var photoPath: PhotoPath,

    var location: Location,

    val orientation: Orientation

) : Serializable {

    /**
     * The type of the entity, here always "Obstacle". Required field by Orion as entity type
     */
    val type: String = "Obstacle"

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

    /**
     * Converts RestObstacle entity to Obstacle entity.
     *
     * @return Obstacle entity
     */
    fun toObstacle(): Obstacle = Obstacle(
        id = id,
        timeStamp = timeStamp.value,
        obstacleType = obstacleType.value,
        photoPath = photoPath.value,
        location = Obstacle.Location(
            // NOTE REVERSAL OF LONG, LAT -> LAT, LONG
            latitude = location.value.coordinates[1],
            longitude = location.value.coordinates[0]
        ),
        orientation = Obstacle.Orientation(
            orientation.value[0],
            orientation.value[1],
            orientation.value[2]
        )
    )
}