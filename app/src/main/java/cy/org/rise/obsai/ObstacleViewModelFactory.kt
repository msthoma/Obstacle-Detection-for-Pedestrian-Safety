package cy.org.rise.obsai

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

/*
 * Factory for creating an [ObstacleViewModel] with a constructor that takes an [ObstacleRepository]
 * based on https://github.com/android/sunflower/blob/master/app/src/main/java/com/google/samples/apps/sunflower/viewmodels/PlantListViewModelFactory.kt
 */
class ObstacleViewModelFactory(
    private val repository: ObstacleRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return ObstacleViewModel(repository, handle) as T
    }
}