package cy.org.rise.obsai.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.data.CnnClassifier
import cy.org.rise.obsai.data.LocationLiveData
import cy.org.rise.obsai.data.OrientationLiveData
import cy.org.rise.obsai.db.AppRepository
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.utils.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.common.FileUtil

/**
 * App view model.
 *
 * @property savedStateHandle
 * @constructor Creates a new view model.
 * @param app application context
 * @param rep instance of [AppRepository]
 */
class ObstacleViewModel internal constructor(
    private val rep: AppRepository,
    private val app: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    /** Location tracking as LiveData. */
    val locationLiveData by lazy { LocationLiveData(app) }

    /** Orientation tracking as LiveData. */
    val orientationLiveData by lazy { OrientationLiveData(app) }

    /** Lazy initialization of the CNN classifier - by keeping a reference to the Lazy object
     * itself, we can check if the classifier was actually initialized in onCleared() below.
     */
    private val lazyCnnClassifier = lazy {
        CnnClassifier(
            tfLiteModel = FileUtil.loadMappedFile(app, "cnn128RGB.tflite"),
            cnnLabels = FileUtil.loadLabels(app, "cnnRGB_labels.txt")
        )
    }

    /** CNN classifier. */
    private val cnnClassifier by lazyCnnClassifier

    /**
     * Runs CNN classification on provided photo, and returns LiveData<Result>.
     *
     * See [https://medium.com/@jcamilorada/arrow-try-is-dead-long-live-kotlin-result-5b086892a71e]
     * for use of Result.
     *
     * @param photoPath full path of the photo to analyze
     */
    fun classifyPhotoWithCNN(photoPath: String) = liveData(Dispatchers.Default) {
        emit(kotlin.runCatching { cnnClassifier.classifyPhoto(photoPath) })
    }

    /** LiveData of obstacles in local db. */
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
            rep.postToiNicosiaWM(obstacle)

//            try {
//                val res = rep.postToiNicosia(obstacle)
//                Log.d("server push res", res.toString())
//                res?.let { response ->
//                    obstacle.uploadStatus = response.code().toString()
//                    rep.updateObstacle(obstacle)
//                }
//            } catch (e: Exception) {
//                //  Fiware, not only uploading photo to Minio
//                obstacle.uploadStatus = "Error"
//                rep.updateObstacle(obstacle)
//                Log.e(TAG(), "push to server failed", e)
//            }
        }
    }

    /** Deletes all obstacles from local db. */
    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) { rep.deleteAll() }

    /**
     * Inserts obstacle in remote server.
     *
     * @param restObstacle object to be inserted into remote server
     */
    fun insertServerObstacle(restObstacle: RestObstacle) = viewModelScope.launch(Dispatchers.IO) {
        rep.insertServerObstacle(restObstacle)
    }

    val allServerObstacles = liveData(Dispatchers.IO) {
        emit(rep.getAllServerObstacles())
    }

//    val allServerObs = viewModelScope.launch(Dispatchers.IO) { rep.getAllServerObstacles() }

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

    /** Override to make sure the cnnClassifier is closed when the VM is cleared. */
    override fun onCleared() {
        super.onCleared()
        if (lazyCnnClassifier.isInitialized()) cnnClassifier.close()
    }
}
