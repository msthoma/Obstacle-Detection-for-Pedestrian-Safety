package cy.org.rise.obsai.db

import androidx.room.*
import com.google.gson.annotations.Expose
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
    @Expose
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo val time_stamp: Date = Calendar.getInstance().time,

    // not the best name, but obstacleType is taken below, exposed for json serialization
    @ColumnInfo var obs_type: String,

    @ColumnInfo var photo_path: String,

    @Expose
    @Embedded(prefix = "location_")
    var location: Location,

    @Expose
    @Embedded(prefix = "orientation_")
    val orientation: Orientation,

    @ColumnInfo var altitude: Double? = null

) : Serializable {

    @Expose
    @Ignore
    val timeStamp = TimeStamp(time_stamp)

    data class TimeStamp(
        @Expose
        val value: Date,
        @Expose
        val type: String = "Date"
    )

    @Expose
    @Ignore
    val photoPath = PhotoPath(photo_path)

    data class PhotoPath(
        @Expose
        val value: String,
        @Expose
        val type: String = "Text"
    )

    @Expose
    @Ignore
    val type: String = "Obstacle" // required by Orion as entity type

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
        @Ignore
        val value: Value = Value(listOf(latitude, longitude))

        @Expose
        @Ignore
        val type: String = "geo:json"

        data class Value(
            @Expose
            val coordinates: List<Double>,
            @Expose
            val type: String = "Point"
        )
    }

    data class Orientation(
        var x: Double,
        var y: Double,
        var z: Double
    ) {

        @Expose
        @Ignore
        var value: List<Double> = listOf(x, y, z)

        @Expose
        @Ignore
        val type: String = "Text" // TODO is there a better type?? list of doubles
    }
}