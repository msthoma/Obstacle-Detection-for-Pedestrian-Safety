package cy.org.rise.obsai.db

/**
 * Repository module for handling data operations.
 * https://github.com/android/sunflower/blob/master/app/src/main/java/com/google/samples/apps/sunflower/data/PlantRepository.kt
 */

class ObstacleRepository private constructor(private val obstacleDao: ObstacleDao) {

    fun getObstacles() = obstacleDao.getAllObstacles()

    suspend fun insertObstacle(obstacle: Obstacle) = obstacleDao.insertObstacle(obstacle)

    suspend fun deleteAll() = obstacleDao.deleteAll()

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