package cy.org.rise.obsai

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cy.org.rise.obsai.db.Converters
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.db.ObstacleDao

@Database(entities = [Obstacle::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ObstacleRoomDatabase : RoomDatabase() {
    abstract fun obstacleDao(): ObstacleDao

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: ObstacleRoomDatabase? = null

        fun getInstance(context: Context): ObstacleRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ObstacleRoomDatabase::class.java,
                        "obstacle_db"
                    )
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}