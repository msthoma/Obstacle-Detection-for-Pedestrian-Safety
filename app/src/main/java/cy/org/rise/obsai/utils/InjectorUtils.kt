package cy.org.rise.obsai.utils

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import cy.org.rise.obsai.db.ObstacleRepository
import cy.org.rise.obsai.db.ObstacleRoomDatabase
import cy.org.rise.obsai.ui.ObstacleViewModelFactory

/**
 * Static methods used to inject classes needed for Activities and Fragments.
 *
 * Based on this [example](https://github.com/android/sunflower/blob/master/app/src/main/java/com/google/samples/apps/sunflower/utilities/InjectorUtils.kt).
 */

object InjectorUtils {

    private fun getObstacleRepository(context: Context): ObstacleRepository {
        return ObstacleRepository.getInstance(
            ObstacleRoomDatabase.getInstance(
                context
                    .applicationContext
            ).obstacleDao(),
            context.applicationContext
        )
    }

    fun provideObstacleViewModelFactory(
        fragment: Fragment,
        application: Application
    ): ObstacleViewModelFactory {
        val repository = getObstacleRepository(fragment.requireContext())
        return ObstacleViewModelFactory(repository, application, fragment)
    }
}
