package cy.org.rise.obsai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class ObstacleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ObstacleRepository
    val allObstacles: LiveData<List<Obstacle>>

    init {
        val obstacleDao = ObstacleRoomDatabase.getDatabase(application).obstacleDao()
        repository = ObstacleRepository(obstacleDao)
        allObstacles = repository.allObstacles
    }

    fun insertObstacle(obstacle: Obstacle) {
        repository.insertObstacle(obstacle)
    }
}