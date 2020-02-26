package cy.org.rise.obsai

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class ObstacleViewModel internal constructor(
    obstacleRepository: ObstacleRepository,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    val obstacles: LiveData<List<Obstacle>> = obstacleRepository.getObstacles()

//    fun insertObstacle(obstacle: Obstacle) {
//        obstacleRepository.insertObstacle(obstacle)
//    }
}