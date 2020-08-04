package cy.org.rise.obsai.api

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cy.org.rise.obsai.db.AppRoomDatabase
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.Constants
import cy.org.rise.obsai.utils.SessionManager
import cy.org.rise.obsai.utils.TAG

/** Worker responsible for uploading obstacles to iNicosia. */
class INicosiaWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    private lateinit var obstacle: Obstacle
    private lateinit var db: AppRoomDatabase

    override fun doWork(): Result {
        Log.d(TAG(), "# of retry: $runAttemptCount")

        // limit number of retries
        if (runAttemptCount > MAX_RETRIES) {
            Log.d(TAG(), "Reached max number of retries ($MAX_RETRIES), returning failure")
            updateUploadStatus(Constants.UPLOAD_FAIL)
            return Result.failure()
        }

        var obstacleId: String? = null

        return try {
            // Get obstacle from db with its id, and upload it to remote platform
            obstacleId = inputData.getString(Constants.KEY_OBSTACLE_ID)

            obstacleId?.let {
                db = AppRoomDatabase.getInstance(applicationContext)

                obstacle = db.obstacleDao().getObstacleById(it)

                val authToken = SessionManager(applicationContext).fetchAuthToken() ?: ""

                val api = INicosiaApi.create(INicosiaApi.iNICOSIA_BASE_URL, authToken)

                val response = api?.postToiNicosiaJson(obstacle)?.execute()

                return response?.let {
                    if (!response.isSuccessful) {
                        Log.d(TAG(), "Failed upload: ${response.errorBody()}, will retry")
                        Result.retry()
                    } else {
                        updateUploadStatus(Constants.UPLOAD_SUCCESS)
                        Result.success() // TODO pass on Response from API Result.success(....)
                    }
                } ?: Result.retry()
            } ?: Result.retry()
        } catch (e: Exception) {
            Log.e(TAG(), "Exception when uploading entity with ID $obstacleId, will retry", e)
            Result.retry() // TODO pass on Response from API Result.failure(....)
        }
    }

    private fun updateUploadStatus(status: String) {
        Log.d(TAG(), "updating Upload Status to $status")
        if (::db.isInitialized && ::obstacle.isInitialized) {
            db.obstacleDao().updateObstacle(obstacle.apply { uploadStatus = status })
        }
    }

    private companion object {
        const val MAX_RETRIES = 4 // will retry 5 times, retry count starts at 0
    }
}
