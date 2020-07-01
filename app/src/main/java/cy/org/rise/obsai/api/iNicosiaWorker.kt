package cy.org.rise.obsai.api

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.Constants
import cy.org.rise.obsai.utils.TAG

class iNicosiaWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        var obstacleJson: String? = null
        return try {
            // Get obstacle JSON
            obstacleJson = inputData.getString(Constants.KEY_OBSTACLE_JSON)
            // Convert back to Obstacle entity
            val obstacle = Gson().fromJson<Obstacle>(obstacleJson, Obstacle::class.java)

            val api = FiwareOrionApi.create(FiwareOrionApi.iNICOSIA_BASE_URL)

            val response = api?.postToiNicosiaWM(
                obstacle.id,
                obstacle.obstacleType,
                obstacle.location.latitude,
                obstacle.location.longitude,
                obstacle.obstacleType,
                obstacle.orientation.x,
                obstacle.orientation.y
            )?.execute()

            return response?.let {
                if (!response.isSuccessful) {
                    Result.failure()
                } else {
                    Result.success() // TODO pass on Response from API Result.success(....)
                }
            } ?: Result.failure()

        } catch (e: Exception) {
            Log.e(TAG(), "Failed to upload entity with JSON $obstacleJson", e)
            Result.failure() // TODO pass on Response from API Result.failure(....)
        }
    }
}