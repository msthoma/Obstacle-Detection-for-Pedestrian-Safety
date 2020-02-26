package cy.org.rise.obsai

import io.reactivex.Completable

/**
 * Repository module for handling data operations.
 * https://github.com/android/sunflower/blob/master/app/src/main/java/com/google/samples/apps/sunflower/data/PlantRepository.kt
 */

class ObstacleRepository private constructor(private val obstacleDao: ObstacleDao) {

    fun getObstacles() = obstacleDao.getAllObstacles()

    fun insertObstacle(obstacle: Obstacle): Completable = obstacleDao.insertObstacle(obstacle)

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: ObstacleRepository? = null

        fun getInstance(obstacleDao: ObstacleDao) =
            instance ?: synchronized(this) {
                instance ?: ObstacleRepository(obstacleDao).also { instance = it }
            }
    }
}