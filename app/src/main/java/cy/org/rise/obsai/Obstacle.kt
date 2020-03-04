package cy.org.rise.obsai

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "obstacle_table")
data class Obstacle(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    @ColumnInfo val obstacle: String,

    @ColumnInfo val latitude: Double,

    @ColumnInfo val longitude: Double
)