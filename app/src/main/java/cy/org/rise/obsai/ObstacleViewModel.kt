package cy.org.rise.obsai

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cy.org.rise.obsai.api.OrionService
import cy.org.rise.obsai.api.RestObstacle
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.db.ObstacleRepository
import cy.org.rise.obsai.utils.TAG
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ObstacleViewModel internal constructor(
    obstacleRepository: ObstacleRepository,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    private val rep = obstacleRepository

    val obstacles: LiveData<List<Obstacle>> = obstacleRepository.getObstacles()

    fun insertObstacle(obstacle: Obstacle) {
        Log.d(TAG(), "inserting obstacle...")
        viewModelScope.launch { rep.insertObstacle(obstacle) }
    }

    fun deleteAll() = viewModelScope.launch { rep.deleteAll() }

    fun insertRestObstacle(restObstacle: RestObstacle) = viewModelScope.launch {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.10.10:1026/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(OrionService::class.java)

        val call = service.insertObstacle(restObstacle)
    }

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