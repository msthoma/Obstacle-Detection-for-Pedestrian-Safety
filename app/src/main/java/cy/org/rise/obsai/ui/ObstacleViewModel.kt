package cy.org.rise.obsai.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.db.ObstacleRepository
import cy.org.rise.obsai.utils.LocationLiveData
import cy.org.rise.obsai.utils.OrientationLiveData
import cy.org.rise.obsai.utils.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * App view model.
 *
 * @property savedStateHandle
 * @constructor
 *
 * @param rep instance of [ObstacleRepository]
 */
class ObstacleViewModel internal constructor(
    private val rep: ObstacleRepository,
    private val app: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    /**
     * Location tracking as LiveData.
     */
    val locationLiveData by lazy { LocationLiveData(app) }

    /**
     * Orientation tracking as LiveData.
     */
    val orientationLiveData by lazy { OrientationLiveData(app) }

    /**
     * LiveData of obstacles in local db.
     */
    val obstacles: LiveData<List<Obstacle>> = rep.getAllObstaclesLive()

    /**
     * Inserts obstacle in Room database.
     *
     * @param obstacle object to be inserted into db
     */
    fun insertObstacle(obstacle: Obstacle) {
        Log.d(TAG(), "inserting obstacle...")

        // see https://stackoverflow.com/q/58341983 for comments on using GlobalScope
        GlobalScope.launch {
            // add to local db
            rep.insertObstacle(obstacle)

            // push to server
//            try {
//                val res = rep.postToiNicosia(obstacle)
//                Log.d("server push res", res.toString())
//                res?.let { response ->
//                    obstacle.uploadStatus = response.code().toString()
//                    rep.updateObstacle(obstacle)
//                }
//            } catch (e: Exception) {
//                // TODO here catch other exceptions as well, e.g. for inserting obstacle to
//                //  Fiware, not only uploading photo to Minio
//                obstacle.uploadStatus = "Error"
//                rep.updateObstacle(obstacle)
//                Log.e(TAG(), "push to server failed", e)
//            }
            rep.postToiNicosiaWM(obstacle)
        }
    }

    /**
     * Deletes all obstacles from local db.
     */
    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) { rep.deleteAll() }

    /**
     * Inserts obstacle in remote server.
     *
     * @param restObstacle object to be inserted into remote server
     */
    fun insertServerObstacle(restObstacle: RestObstacle) = viewModelScope.launch(Dispatchers.IO) {
        rep.insertServerObstacle(restObstacle)
    }

    /**
     * Runs CNN classification on provided photo, and returns LiveData<Result>.
     *
     * See [https://medium.com/@jcamilorada/arrow-try-is-dead-long-live-kotlin-result-5b086892a71e]
     * for use of Result.
     *
     * @param photoPath full path of the photo to analyze
     */
    fun analyzePhotoWithCNN(photoPath: String) = liveData(Dispatchers.Default) {
        emit(kotlin.runCatching { rep.analyzePhotoWithCNN(photoPath) })
    }

//    val allServerObstacles = liveData(Dispatchers.IO) {
//        val allRestObstacles = rep.getAllServerObstacles("Obstacle")
//        val allObstacles = allRestObstacles.map { it.toObstacle() }
//        emit(allObstacles)
//    }

//    fun getAllServer() = viewModelScope.launch { rep.getAllServerObstacles("Obstacle") }

//    fun ff(path: String) = viewModelScope.launch {
//        findFaces(path)
//    }
//    private suspend fun findFaces(path: String): Array<FaceDetector.Face?> {
//        val bm = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply {
//            inPreferredConfig = Bitmap.Config.RGB_565
//        })
//        val faceArray = arrayOfNulls<FaceDetector.Face>(10)
//        val fd = FaceDetector(bm.width, bm.height, 10)
//        fd.findFaces(bm, faceArray)
//        Log.d(TAG(), faceArray[0]?.confidence().toString())
//        return faceArray
//    }
}
