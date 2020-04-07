package cy.org.rise.obsai.db

import android.content.Context
import cy.org.rise.obsai.api.FiwareOrionApi
import cy.org.rise.obsai.api.MinIOUploader
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.utils.SessionManager
import retrofit2.Response

/**
 * Repository module for handling data operations.
 * https://github.com/android/sunflower/blob/master/app/src/main/java/com/google/samples/apps/sunflower/data/PlantRepository.kt
 */

class ObstacleRepository private constructor(
    private val obstacleDao: ObstacleDao, private val
    context: Context
) {

    fun getObstacles() = obstacleDao.getAllObstacles()

    suspend fun insertObstacle(obstacle: Obstacle) = obstacleDao.insertObstacle(obstacle)

    suspend fun deleteAll() = obstacleDao.deleteAll()

    // Network operations
    private val orionService by lazy {
        val accessToken = SessionManager(context).fetchAuthToken()
        FiwareOrionApi.create(
            FiwareOrionApi.ORION_BASE_URL,
            SessionManager(context).fetchAuthToken() ?: ""
        )
    }

    private val minIOUploader by lazy {
        MinIOUploader.instance
    }

    suspend fun insertServerObstacle(restObstacle: RestObstacle): Response<Unit> {
//        try {
//            minIOUploader.uploadPhoto(
//                serverPhotoName = "${restObstacle.id}.jpg",
//                photoPath = restObstacle.photoPath.value,
//                bucket = "rise.test"
//            )
//        } catch (connectError: ConnectException) {
//            Log.e(TAG(), "Failed to connect to MinIO: $connectError")
//        }
        return orionService.insertServerObstacle(restObstacle)
    }

    suspend fun getAllServerObstacles(type: String) =
        orionService.getAllServerObstacles(type)

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: ObstacleRepository? = null

        fun getInstance(obstacleDao: ObstacleDao, context: Context) =
            instance ?: synchronized(this) {
                instance
                    ?: ObstacleRepository(obstacleDao, context)
                        .also { instance = it }
            }
    }
}