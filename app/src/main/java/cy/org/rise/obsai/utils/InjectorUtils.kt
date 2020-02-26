package cy.org.rise.obsai.utils

import android.content.Context
import androidx.fragment.app.Fragment
import cy.org.rise.obsai.ObstacleRepository
import cy.org.rise.obsai.ObstacleRoomDatabase
import cy.org.rise.obsai.ObstacleViewModelFactory

/**
 * Static methods used to inject classes needed for Activities and Fragments.
 * https://github.com/android/sunflower/blob/master/app/src/main/java/com/google/samples/apps/sunflower/utilities/InjectorUtils.kt
 */

object InjectorUtils {

    private fun getObstacleRepository(context: Context): ObstacleRepository {
        return ObstacleRepository.getInstance(
            ObstacleRoomDatabase.getInstance(
                context
                    .applicationContext
            ).obstacleDao()
        )
    }

    fun provideObstacleViewModelFactory(fragment: Fragment): ObstacleViewModelFactory {
        val repository = getObstacleRepository(fragment.requireContext())
        return ObstacleViewModelFactory(repository, fragment)
    }
}