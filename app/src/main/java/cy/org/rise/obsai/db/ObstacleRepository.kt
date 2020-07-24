package cy.org.rise.obsai.db

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.*
import cy.org.rise.obsai.api.FiwareOrionApi
import cy.org.rise.obsai.api.MinIOUploader
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.api.iNicosiaWorker
import cy.org.rise.obsai.utils.Constants
import cy.org.rise.obsai.utils.SessionManager
import cy.org.rise.obsai.utils.TAG
import retrofit2.Response
import java.io.File
import java.io.IOException

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
    fun getAllObstaclesLive() = obstacleDao.getAllObstaclesLive()

    /**
     * Inserts obstacle in local database.
     */
    suspend fun insertObstacle(obstacle: Obstacle) = obstacleDao.insertObstacle(obstacle)

    /**
     * Updates obstacle already in local database.
     */
    suspend fun updateObstacle(obstacle: Obstacle) = obstacleDao.updateObstacle(obstacle)

    /**
     * Deletes all obstacles from local database, and their accompanying photo files in loca
     * storage.
     */
    suspend fun deleteAll() {
        val allObstacles = obstacleDao.getAllObstacles()
        // delete photo files first
        allObstacles.forEach { obstacle ->
            try {
                File(obstacle.photoPath).delete()
                Log.d(TAG(), "Deleted photo ${obstacle.photoPath}")
            } catch (ex: IOException) {
                Log.e(TAG(), "Error deleting photo at ${obstacle.photoPath}", ex)
            }
        }
        // delete entries in database
        obstacleDao.deleteAll()
    }

    // Network operations
    private val orionService by lazy {
        FiwareOrionApi.create(
            FiwareOrionApi.iNICOSIA_BASE_URL,
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
    suspend fun insertServerObstacle(restObstacle: RestObstacle): Response<Unit>? {
//        try {
//            minIOUploader.uploadPhoto(
//                serverPhotoName = "${restObstacle.id}.jpg",
//                photoPath = restObstacle.photoPath.value,
//                bucket = "rise.test"
//            )
//        } catch (connectError: ConnectException) {
//            Log.e(TAG(), "Failed to connect to MinIO: $connectError")
//        }
        return orionService?.insertServerObstacle(restObstacle)
    }

    fun postToiNicosiaWM(obstacle: Obstacle) {
        // Check if mobile data is allowed by the user
        val mobileDataAllowed = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("allow_mobile_data", false)
        Log.d(TAG(), "Mobile data allowed: $mobileDataAllowed")

        val workManager = WorkManager.getInstance(context)

        val uploadConstraints = Constraints.Builder()
            .setRequiredNetworkType(
                // TODO recheck network logic here, as it does it produce the intended behaviour?
                if (mobileDataAllowed) {
                    NetworkType.CONNECTED
                } else {
                    NetworkType.UNMETERED
                }
            )
            .build()

        val upload = OneTimeWorkRequestBuilder<iNicosiaWorker>()
            .setInputData(
                Data.Builder().putString(Constants.KEY_OBSTACLE_JSON, obstacle.toJson()).build()
            )
            .setConstraints(uploadConstraints)
            .build()

        workManager.enqueue(upload)
    }

    /**
     * Gets all obstacles saved on server.
     *
     * @param type type of entity required, here should be "Obstacle"
     */
    suspend fun getAllServerObstacles(type: String) =
        orionService?.getAllServerObstacles(type)

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
