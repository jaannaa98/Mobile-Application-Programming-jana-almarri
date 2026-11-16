
package sa.edu.iau.cshj.studyplanner

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TodayViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = TaskRepository(
        StudyDatabase.getDatabase(application).studyTaskDao()
    )

    private val preferences = application.getSharedPreferences(
        "study_planner",
        Context.MODE_PRIVATE
    )

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> =
        _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (!preferences.getBoolean("sample_initialized", false)) {
                repository.seedInitialTasksIfEmpty()
                preferences.edit()
                    .putBoolean("sample_initialized", true)
                    .apply()
            }

            repository.allTasks.collect { tasks ->
                _uiState.update {
                    it.copy(isLoading = false, tasks = tasks)
                }
            }
        }
    }

    fun onAddTask(title: String) {
        if (title.isBlank()) return

        viewModelScope.launch {
            repository.add(title.trim(), "Mobile Apps")
            _uiState.update {
                it.copy(message = "Task added")
            }
        }
    }

    fun onTaskCheckedChange(id: Int, done: Boolean) {
        val task = _uiState.value.tasks.find { it.id == id }
            ?: return

        viewModelScope.launch {
            repository.setDone(task, done)
            if (done) {
                _uiState.update {
                    it.copy(message = "Marked as done")
                }
            }
        }
    }

    fun onRemindMe() {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(getApplication())
            .enqueue(request)

        _uiState.update {
            it.copy(
                message = "Reminder in 10 seconds. Close the app!"
            )
        }
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(message = null)
        }
    }
}
