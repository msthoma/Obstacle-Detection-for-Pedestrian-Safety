package cy.org.rise.obsai.db

import cy.org.rise.obsai.api.FiwareOrionApi
import cy.org.rise.obsai.api.MinIOUploader
import cy.org.rise.obsai.api.RestObstacle
import retrofit2.Response

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

    private val minIOUploader by lazy {
        MinIOUploader.instance
    }

    suspend fun insertServerObstacle(restObstacle: RestObstacle): Response<Unit> {
        minIOUploader.uploadPhoto(
            serverPhotoName = "${restObstacle.id}.jpg",
            photoPath = restObstacle.photoPath.value,
            bucket = "rise.test"
        )
        return orionService.insertServerObstacle(restObstacle)
    }

    suspend fun getAllServerObstacles(type: String) =
        orionService.getAllServerObstacles(type)

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