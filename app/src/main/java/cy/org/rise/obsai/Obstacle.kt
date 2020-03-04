package cy.org.rise.obsai

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "obstacle_table")
data class Obstacle(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "obstacle") val obstacle: String,

    @ColumnInfo(name = "latitude") val latitude: Double,

    @ColumnInfo(name = "longitude") val longitude: Double
)