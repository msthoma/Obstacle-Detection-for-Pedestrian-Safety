package cy.org.rise.obsai

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "obstacle_table")
data class Obstacle(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    @ColumnInfo var obstacle: String,

    @ColumnInfo var obs_type: String,

    @ColumnInfo var photo: String,

    @ColumnInfo var latitude: Double,

    @ColumnInfo var longitude: Double,

    // Orientation axes (best replaced with class or similar)
    @ColumnInfo var x: Double,
    @ColumnInfo var y: Double,
    @ColumnInfo var z: Double,

    @ColumnInfo var altitude: Double? = null
) : Serializable