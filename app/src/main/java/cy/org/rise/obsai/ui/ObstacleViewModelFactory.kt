package cy.org.rise.obsai.ui

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import cy.org.rise.obsai.db.AppRepository

/**
 * Factory for creating an [ObstacleViewModel] with a constructor that takes an
 * [AppRepository], based on [this example](https://git.io/JJ0D5).
 */
class ObstacleViewModelFactory(
    private val repository: AppRepository,
    private val application: Application,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return ObstacleViewModel(repository, application, handle) as T
    }
}
