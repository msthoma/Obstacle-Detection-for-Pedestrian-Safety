package cy.org.rise.obsai.utils

import android.content.Context
import androidx.fragment.app.Fragment
import cy.org.rise.obsai.db.AppRepository
import cy.org.rise.obsai.db.AppRoomDatabase
import cy.org.rise.obsai.ui.ObstacleViewModelFactory

/**
 * Static methods used to inject classes needed for Activities and Fragments.
 *
 * Based on this [example](https://git.io/JJaYb).
 */

object InjectorUtils {

    private fun getAppRepository(context: Context): AppRepository {
        return AppRepository.getInstance(
            AppRoomDatabase.getInstance(context.applicationContext).obstacleDao(),
            context.applicationContext
        )
    }

    fun provideObstacleViewModelFactory(fragment: Fragment): ObstacleViewModelFactory {
        val repository = getAppRepository(fragment.requireContext())
        return ObstacleViewModelFactory(
            repository,
            fragment.requireActivity().application,
            fragment
        )
    }
}
