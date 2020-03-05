package cy.org.rise.obsai

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cy.org.rise.obsai.db.Obstacle

@Dao
interface ObstacleDao {
    @Query("SELECT * from obstacle_table ORDER BY timestamp ASC")
    fun getAllObstacles(): LiveData<List<Obstacle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertObstacle(obstacle: Obstacle)

    @Query("DELETE FROM obstacle_table")
    fun deleteAll()
}