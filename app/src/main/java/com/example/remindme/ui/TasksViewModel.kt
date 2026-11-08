package com.example.remindme.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.remindme.data.AppDatabase
import com.example.remindme.data.LatLng
import com.example.remindme.data.ReminderDefaults
import com.example.remindme.data.TaskEntity
import com.example.remindme.data.TaskRepository
import com.example.remindme.data.TaskWithDistance
import com.example.remindme.data.orderActive
import com.example.remindme.data.orderCompleted
import com.example.remindme.data.location.CurrentLocation
import com.example.remindme.data.place.GeocoderPlaceSearch
import com.example.remindme.triggers.Triggers
import com.example.remindme.data.place.PlaceSearch
import com.example.remindme.data.place.SearchOutcome
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TaskRepository,
    private val placeSearch: PlaceSearch,
    private val currentLocation: CurrentLocation,
    private val triggers: Triggers,
    private val defaults: ReminderDefaults,
) : ViewModel() {

    // Null until a fix arrives (or without permission), so tasks sort by due time.
    private val location = MutableStateFlow<LatLng?>(null)

    val activeTasks: StateFlow<List<TaskWithDistance>> =
        combine(repository.observeActive(), location) { tasks, here -> orderActive(tasks, here) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completedTasks: StateFlow<List<TaskEntity>> =
        repository.observeCompleted()
            .map(::orderCompleted)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeTask(id: Int): Flow<TaskEntity?> = repository.observe(id)

    suspend fun searchPlaces(query: String): SearchOutcome = placeSearch.search(query.trim(), location.value)

    fun updateLocation(here: LatLng?) {
        location.value = here
    }

    /** Takes a fresh fix. Without permission the location is cleared; a failed fix keeps the last one. */
    fun refreshLocation() {
        viewModelScope.launch {
            if (!currentLocation.hasPermission()) location.value = null
            else currentLocation.get()?.let { location.value = it }
        }
    }

    fun save(task: TaskEntity) {
        viewModelScope.launch {
            val id = repository.save(task)
            triggers.sync(id)
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            triggers.clear(id, cancelNotification = true)
            repository.delete(id)
        }
    }

    fun setCompleted(id: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.setCompleted(id, completed)
            if (completed) triggers.clear(id, cancelNotification = true) else triggers.sync(id)
        }
    }

    /** The app-wide radius: every place reminder uses it, so they're updated and re-registered too. */
    fun setDefaultRadius(meters: Int) {
        defaults.setRadius(meters)
        viewModelScope.launch {
            repository.setRadiusForAll(meters)
            triggers.syncAll()
        }
    }

    fun syncTriggers() {
        viewModelScope.launch { triggers.syncAll() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                TasksViewModel(
                    TaskRepository(AppDatabase.getInstance(app).taskDao()),
                    GeocoderPlaceSearch(app),
                    CurrentLocation(app),
                    Triggers(app),
                    ReminderDefaults.getInstance(app),
                )
            }
        }
    }
}

/** Activity-scoped: scoping it to a nav destination would cancel a save or delete when that screen closes. */
@Composable
fun tasksViewModel(): TasksViewModel {
    val owner = checkNotNull(LocalActivity.current as? ViewModelStoreOwner) { "Needs a ComponentActivity" }
    return viewModel(viewModelStoreOwner = owner, factory = TasksViewModel.Factory)
}
