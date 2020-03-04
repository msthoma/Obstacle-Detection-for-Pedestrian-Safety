package cy.org.rise.obsai

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class ObstacleViewModel internal constructor(
    obstacleRepository: ObstacleRepository,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    private val rep = obstacleRepository
    val obstacles: LiveData<List<Obstacle>> = obstacleRepository.getObstacles()

    fun insertObstacle(obstacle: Obstacle) {
        Log.d(TAG(),"inserting obstacle...")
        return rep.insertObstacle(obstacle)
    }
}