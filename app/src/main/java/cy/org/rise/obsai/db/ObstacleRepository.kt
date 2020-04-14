package cy.org.rise.obsai.db

import android.content.Context
import cy.org.rise.obsai.api.FiwareOrionApi
import cy.org.rise.obsai.api.MinIOUploader
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.utils.SessionManager
import retrofit2.Response

/**
 * Repository module for handling data operations, based on
 * [this](https://github.com/android/sunflower/blob/master/app/src/main/java/com/google/samples/apps/sunflower/data/PlantRepository.kt)
 * example.
 */

class ObstacleRepository private constructor(
    private val obstacleDao: ObstacleDao, private val
    context: Context
) {

    /**
     * Returns all obstacles saved in local database as LiveData.
     */
    fun getObstacles() = obstacleDao.getAllObstacles()

    /**
     * Inserts obstacle in local database.
     */
    suspend fun insertObstacle(obstacle: Obstacle) = obstacleDao.insertObstacle(obstacle)

    /**
     * Deletes all obstacles from local database.
     */
    suspend fun deleteAll() = obstacleDao.deleteAll()

    // Network operations
    private val orionService by lazy {
        FiwareOrionApi.create(
            FiwareOrionApi.ORION_BASE_URL,
            SessionManager(context).fetchAuthToken() ?: ""
        )
    }

    private val minIOUploader by lazy {
        MinIOUploader.instance
    }

    /**
     * Uploads entity to server.
     *
     * @param restObstacle entity to be uploaded to the server
     * @return retrofit2 Response
     */
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

    /**
     * Gets all obstacles saved on server.
     *
     * @param type type of entity required, here should be "Obstacle"
     */
    suspend fun getAllServerObstacles(type: String) =
        orionService.getAllServerObstacles(type)

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: ObstacleRepository? = null

        /**
         * Returns singleton of Repository.
         *
         * @param obstacleDao data access object
         * @param context Application context, required to read SharedPreferences for access token.
         */
        fun getInstance(obstacleDao: ObstacleDao, context: Context) =
            instance ?: synchronized(this) {
                instance
                    ?: ObstacleRepository(obstacleDao, context)
                        .also { instance = it }
            }
    }
}