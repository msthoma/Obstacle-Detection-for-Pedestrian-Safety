package cy.org.rise.obsai.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ObstacleDao {
    @Query("SELECT * from obstacle_table ORDER BY timeStamp ASC")
    fun getAllObstacles(): LiveData<List<Obstacle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObstacle(obstacle: Obstacle)

    @Update
    suspend fun updateObstacle(obstacle: Obstacle)

    @Query("DELETE FROM obstacle_table")
    suspend fun deleteAll()
}