package cy.org.rise.obsai.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Abstract class for Room database.
 */
@Database(entities = [Obstacle::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ObstacleRoomDatabase : RoomDatabase() {
    abstract fun obstacleDao(): ObstacleDao

    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: ObstacleRoomDatabase? = null

        /**
         * Returns a singleton instance of the app's Room database.
         *
         * @param context application context
         * @return
         */
        fun getInstance(context: Context): ObstacleRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE
                ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ObstacleRoomDatabase::class.java,
                        "obstacle_db"
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}