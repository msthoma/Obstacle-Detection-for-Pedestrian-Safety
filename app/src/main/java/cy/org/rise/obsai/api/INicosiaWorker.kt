package cy.org.rise.obsai.api

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cy.org.rise.obsai.db.ObstacleRoomDatabase
import cy.org.rise.obsai.utils.Constants
import cy.org.rise.obsai.utils.TAG

/** Worker responsible for uploading obstacles to iNicosia. */
class INicosiaWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    // TODO 31/07/20  add retries by returning Result.retry()

    override fun doWork(): Result {
        var obstacleId: String? = null

        return try {
            // Get obstacle from db with its id, and upload it to remote platform
            obstacleId = inputData.getString(Constants.KEY_OBSTACLE_ID)

            obstacleId?.let {
                val dbObstacle = ObstacleRoomDatabase.getInstance(applicationContext).obstacleDao()
                    .getObstacleById(it)

                val api = FiwareOrionApi.create(FiwareOrionApi.iNICOSIA_BASE_URL)

                val response = api?.postToiNicosiaJson(dbObstacle)?.execute()

                return response?.let {
                    if (!response.isSuccessful) {
                        Result.failure()
                    } else {
                        Result.success() // TODO pass on Response from API Result.success(....)
                    }
                } ?: Result.failure()
            } ?: Result.failure()
        } catch (e: Exception) {
            Log.e(TAG(), "Failed to upload entity with ID $obstacleId", e)
            Result.failure() // TODO pass on Response from API Result.failure(....)
        }
    }
}
