package cy.org.rise.obsai.db

import cy.org.rise.obsai.api.FiwareOrionApi

/**
 * Repository module for handling data operations.
 * https://github.com/android/sunflower/blob/master/app/src/main/java/com/google/samples/apps/sunflower/data/PlantRepository.kt
 */

class ObstacleRepository private constructor(private val obstacleDao: ObstacleDao) {

    fun getObstacles() = obstacleDao.getAllObstacles()

    suspend fun insertObstacle(obstacle: Obstacle) = obstacleDao.insertObstacle(obstacle)

    suspend fun deleteAll() = obstacleDao.deleteAll()

    // Network operations
    private val orionService by lazy {
        FiwareOrionApi.create()
    }

    suspend fun insertServerObstacle(obstacle: Obstacle) =
        orionService.insertServerObstacle(obstacle)

    suspend fun getAllServerObstacles(type: String, options: String = "keyValues") =
        orionService.getAllServerObstacles(type, options)

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: ObstacleRepository? = null

        fun getInstance(obstacleDao: ObstacleDao) =
            instance ?: synchronized(this) {
                instance
                    ?: ObstacleRepository(obstacleDao)
                        .also { instance = it }
            }
    }
}