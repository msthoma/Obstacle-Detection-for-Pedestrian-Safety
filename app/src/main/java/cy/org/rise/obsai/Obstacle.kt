package cy.org.rise.obsai

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "obstacle_table")
data class Obstacle(@PrimaryKey @ColumnInfo(name = "obstacle") val obstacle: String)