package cy.org.rise.obsai

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable

@Dao
interface ObstacleDao {
    @Query("SELECT * from obstacle_table ORDER BY obstacle ASC")
    fun getAllObstacles(): LiveData<List<Obstacle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertObstacle(obstacle: Obstacle): Completable
}