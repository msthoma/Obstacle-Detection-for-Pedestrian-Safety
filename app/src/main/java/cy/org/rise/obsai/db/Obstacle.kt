package cy.org.rise.obsai.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

@Entity(tableName = "obstacle_table")
data class Obstacle(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    @ColumnInfo val timestamp: Date = Calendar.getInstance().time,

    @ColumnInfo var obs_type: String,

    @ColumnInfo var photo: String,

    @ColumnInfo var latitude: Double,

    @ColumnInfo var longitude: Double,

    @field:SerializedName("orientation")
    @field:Embedded(prefix = "orientation_")
    val orientation: Orientation,

    @ColumnInfo var altitude: Double? = null
) : Serializable {

    data class Orientation(
        @field:SerializedName("x") val x: Double,
        @field:SerializedName("y") var y: Double,
        @field:SerializedName("z") var z: Double
    )
}