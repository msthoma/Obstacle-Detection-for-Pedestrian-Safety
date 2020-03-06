package cy.org.rise.obsai.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ObstacleDao {
    @Query("SELECT * from obstacle_table ORDER BY timestamp ASC")
    fun getAllObstacles(): LiveData<List<Obstacle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertObstacle(obstacle: Obstacle)

    @Update
    fun updateObstacle(obstacle: Obstacle)

    @Query("DELETE FROM obstacle_table")
    fun deleteAll()
}