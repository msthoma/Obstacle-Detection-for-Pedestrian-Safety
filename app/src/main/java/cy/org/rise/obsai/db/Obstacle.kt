package cy.org.rise.obsai.db

import androidx.room.*
import com.google.gson.annotations.Expose
import java.io.Serializable
import java.util.*

/*
* parameters with @Ignore are ignored by Room
* parameters with @Expose are used by Gson, to convert class to json
* */

@Entity(tableName = "obstacle_table")
data class Obstacle(
    @PrimaryKey
    @Expose
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo val timestamp: Date = Calendar.getInstance().time,

    @ColumnInfo var obs_type: String,

    @ColumnInfo var photo: String,

    @ColumnInfo var latitude: Double,

    @ColumnInfo var longitude: Double,

    @Expose
    @Embedded(prefix = "orientation_")
    val orientation: Orientation,

    @ColumnInfo var altitude: Double? = null

) : Serializable {

    @Expose
    @Ignore
    val type: String = "Obstacle"

    @Expose
    @Ignore
    var location: Location = Location(latitude, longitude)

    @Expose
    @Ignore
    var obstacleType = ObstacleType(obs_type)

    data class ObstacleType(
        @Expose
        val value: String,
        @Expose
        val type: String = "Text"
    )

    data class Location(
        var latitude: Double,
        var longitude: Double
    ) {
        @Expose
        val value: Value = Value(listOf(latitude, longitude))

        @Expose
        val type: String = "geo:json"

        data class Value(
            @Expose
            val coordinates: List<Double>,
            @Expose
            val type: String = "Point"
        )
    }

    data class Orientation(
        val x: Double,
        var y: Double,
        var z: Double
    ) {

        @Expose
        @Ignore
        var value: String = "x:$x,y:$y,z:$z"

        @Expose
        @Ignore
        val type: String = "Text"
    }
}