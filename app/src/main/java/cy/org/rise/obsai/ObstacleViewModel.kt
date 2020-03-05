package cy.org.rise.obsai

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cy.org.rise.obsai.db.Obstacle
import cy.org.rise.obsai.db.ObstacleRepository
import cy.org.rise.obsai.utils.TAG

class ObstacleViewModel internal constructor(
    obstacleRepository: ObstacleRepository,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    private val rep = obstacleRepository

    val obstacles: LiveData<List<Obstacle>> = obstacleRepository.getObstacles()

    fun insertObstacle(obstacle: Obstacle) {
        Log.d(TAG(), "inserting obstacle...")
        return rep.insertObstacle(obstacle)
    }

    fun deleteAll() = rep.deleteAll()
}