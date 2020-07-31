package cy.org.rise.obsai.api

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.db.AppRoomDatabase
import cy.org.rise.obsai.utils.Constants
import cy.org.rise.obsai.utils.TAG

/** Worker responsible for uploading obstacles to iNicosia. */
class INicosiaWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    // TODO 31/07/20  add retries by returning Result.retry()

    // TODO 31/07/20 improve use of variables
    private lateinit var obstacle: Obstacle
    private lateinit var db: AppRoomDatabase

    override fun doWork(): Result {
        var obstacleId: String? = null

        return try {
            // Get obstacle from db with its id, and upload it to remote platform
            obstacleId = inputData.getString(Constants.KEY_OBSTACLE_ID)

            obstacleId?.let {
                db = AppRoomDatabase.getInstance(applicationContext)

                obstacle = db.obstacleDao().getObstacleById(it)

                val api = INicosiaApi.create(INicosiaApi.iNICOSIA_BASE_URL)

                val response = api?.postToiNicosiaJson(obstacle)?.execute()

                return response?.let {
                    if (!response.isSuccessful) {
                        updateUploadStatus(Constants.UPLOAD_FAIL)
                        Result.failure()
                    } else {
                        updateUploadStatus(Constants.UPLOAD_SUCCESS)
                        Result.success() // TODO pass on Response from API Result.success(....)
                    }
                } ?: Result.failure()
            } ?: Result.failure()
        } catch (e: Exception) {
            Log.e(TAG(), "Failed to upload entity with ID $obstacleId", e)
            updateUploadStatus(Constants.UPLOAD_FAIL)
            Result.failure() // TODO pass on Response from API Result.failure(....)
        }
    }

    private fun updateUploadStatus(status: String) {
        Log.d(TAG(), "updating Upload Status to $status")
        if (::db.isInitialized && ::obstacle.isInitialized) {
            db.obstacleDao().updateObstacle(obstacle.apply { uploadStatus = status })
        }
    }
}
