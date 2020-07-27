package cy.org.rise.obsai.db

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import retrofit2.Response
import java.io.File
import java.io.IOException
import kotlin.math.min

/**
 * Repository module for handling data operations, based on [this](https://git.io/JJ0Re) example.
 */

class ObstacleRepository private constructor(
    private val obstacleDao: ObstacleDao,
    private val context: Context
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

    suspend fun analyzePhotoWithCNN(photoPath: String): Map<String, Float> {
        val IMAGE_MEAN = 0.0f
        val IMAGE_STD = 255.0f
        val PROBABILITY_MEAN = 0.0f
        val PROBABILITY_STD = 1.0f

        val tfliteModel = FileUtil.loadMappedFile(context, "cnn128RGB.tflite")
        val tflite = Interpreter(tfliteModel, Interpreter.Options())

        val labels = FileUtil.loadLabels(context, "cnnRGB_labels.txt")

        val imageTensorIndex = 0
        val imageShape = tflite.getInputTensor(imageTensorIndex).shape()
        val imageSizeY = imageShape[1]
        val imageSizeX = imageShape[2]

        val imageDataType = tflite.getInputTensor(imageTensorIndex).dataType()
        val probabilityTensorIndex = 0
        val probabilityShape = tflite.getOutputTensor(probabilityTensorIndex).shape()
        val probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType()

        var inputImageBuffer = TensorImage(imageDataType)

        val outputProbabilityBuffer =
            TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)

        val probabilityProcessor =
            TensorProcessor.Builder().add(NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD))
                .build()

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeFile(photoPath, options)

        val cropSize = min(bitmap.width, bitmap.height)

        inputImageBuffer.load(bitmap)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(
                ResizeOp(
                    imageSizeX, imageSizeY,
                    ResizeOp.ResizeMethod.NEAREST_NEIGHBOR
                )
            )
            .add(Rot90Op(1))
            .add(NormalizeOp(IMAGE_MEAN, IMAGE_STD))
            .build()
        inputImageBuffer = imageProcessor.process(inputImageBuffer)

        // run classification
        tflite.run(
            inputImageBuffer.buffer,
            outputProbabilityBuffer.buffer.rewind()
        )

        // map labels and their predicted probabilities
        return TensorLabel(
            labels,
            probabilityProcessor.process(outputProbabilityBuffer)
        ).mapWithFloatValue
    }

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
