package cy.org.rise.obsai.api

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cy.org.rise.obsai.utils.Constants
import cy.org.rise.obsai.utils.TAG

class iNicosiaWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        var obstacleToUpload: String? = null
        return try {
            // Get obstacle ID
            obstacleToUpload = inputData.getString(Constants.KEY_OBSTACLE_JSON)

            Result.success() // TODO pass on Response from API Result.success(....)
        } catch (e: Exception) {
            Log.e(TAG(), "Failed to upload entity with ID $obstacleToUpload", e)
            Result.failure() // TODO pass on Response from API Result.failure(....)
        }
    }
}