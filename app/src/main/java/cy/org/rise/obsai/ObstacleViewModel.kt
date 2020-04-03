package cy.org.rise.obsai

import android.util.Log
import androidx.lifecycle.*
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.db.ObstacleRepository
import cy.org.rise.obsai.utils.TAG
import io.minio.errors.MinioException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ObstacleViewModel internal constructor(
    obstacleRepository: ObstacleRepository,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    private val rep = obstacleRepository

    val obstacles: LiveData<List<Obstacle>> = obstacleRepository.getObstacles()

    fun insertObstacle(obstacle: Obstacle) {
        Log.d(TAG(), "inserting obstacle...")

        viewModelScope.launch(Dispatchers.IO) {
            // add to local db
            rep.insertObstacle(obstacle)

            // push to server
            try {
                rep.insertServerObstacle(obstacle.toRestObstacle())
            } catch (e: MinioException) {
                // TODO here catch other exceptions as well, e.g. for inserting obstacle to
                //  Fiware, not only uploading photo to Minio
                Log.e(TAG(), "photo upload failed $e")
            }
        }
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) { rep.deleteAll() }

    fun insertServerObstacle(restObstacle: RestObstacle) = viewModelScope.launch(Dispatchers.IO) {
        rep.insertServerObstacle(restObstacle)
    }

    val allServerObstacles = liveData(Dispatchers.IO) {
        val allRestObstacles = rep.getAllServerObstacles("Obstacle")
        val allObstacles = allRestObstacles.map { it.toObstacle() }
        emit(allObstacles)
    }

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