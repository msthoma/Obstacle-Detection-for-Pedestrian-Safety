package cy.org.rise.obsai

import androidx.lifecycle.LiveData
import io.reactivex.Completable

class ObstacleRepository(private val obstacleDao: ObstacleDao) {
    val allObstacles: LiveData<List<Obstacle>> = obstacleDao.getAllObstacles()

    fun insertObstacle(obstacle: Obstacle): Completable {
        return obstacleDao.insertObstacle(obstacle)
    }
}