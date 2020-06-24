package cy.org.rise.obsai.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.db.ObstacleRepository
import cy.org.rise.obsai.utils.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * App view model.
 *
 * @property savedStateHandle
 * @constructor
 *
 * @param obstacleRepository instance of [ObstacleRepository]
 */
class ObstacleViewModel internal constructor(
    obstacleRepository: ObstacleRepository,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    private val rep = obstacleRepository

    /**
     * Live data of obstacles in local db.
     */
    val obstacles: LiveData<List<Obstacle>> = obstacleRepository.getAllObstaclesLive()

    /**
     * Inserts obstacle in Room database.
     *
     * @param obstacle object to be inserted into db
     */
    fun insertObstacle(obstacle: Obstacle) {
        Log.d(TAG(), "inserting obstacle...")

        viewModelScope.launch(Dispatchers.IO) {
            // add to local db
            rep.insertObstacle(obstacle)

            // push to server
            try {
                rep.postToiNicosiaJson(obstacle)
            } catch (e: Exception) {
                // TODO here catch other exceptions as well, e.g. for inserting obstacle to
                //  Fiware, not only uploading photo to Minio
                Log.e(TAG(), "push to server failed", e)
            }
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