package cy.org.rise.obsai.db

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Interface for data access object, used for defining SQLite commands of Room database.
 */
@Dao
interface ObstacleDao {
    @Query("SELECT * FROM obstacle_table ORDER BY timeStamp ASC")
    fun getAllObstaclesLive(): LiveData<List<Obstacle>>

    @Query("SELECT * FROM obstacle_table")
    suspend fun getAllObstacles(): List<Obstacle>

    @Query("SELECT * FROM obstacle_table WHERE id = :obstacleId")
    fun getObstacleById(obstacleId: String): Obstacle

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObstacle(obstacle: Obstacle)

    @Update
    suspend fun updateObstacle(obstacle: Obstacle)

    @Query("DELETE FROM obstacle_table")
    suspend fun deleteAll()
}
