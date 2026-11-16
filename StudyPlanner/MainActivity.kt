
package sa.edu.iau.cshj.studyplanner

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.*
import androidx.work.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val course: String,
    @ColumnInfo(name = "is_done")
    val isDone: Boolean = false
)

data class PlannerUiState(
    val greeting: String = "Welcome to StudyPlanner!",
    val tasks: List<StudyTask> = emptyList()
)

data class TodayUiState(
    val isLoading: Boolean = true,
    val tasks: List<StudyTask> = emptyList(),
    val message: String? = null
)

fun sampleStudyTasks(): List<StudyTask> = listOf(
    StudyTask(1, "Complete Kotlin Lab", "CSC402", true),
    StudyTask(2, "Study Power BI", "CSC403"),
    StudyTask(3, "Review Image Processing", "ARTI407", true),
    StudyTask(4, "Prepare Presentation", "CSC401"),
    StudyTask(5, "Finish Assignment", "CSC402")
)

@Dao
interface StudyTaskDao {

    @Query("SELECT * FROM study_tasks ORDER BY id")
    fun getAllTasks(): Flow<List<StudyTask>>

    @Insert
    suspend fun insert(task: StudyTask)

    @Update
    suspend fun update(task: StudyTask)

    @Delete
    suspend fun delete(task: StudyTask)

    @Query("SELECT COUNT(*) FROM study_tasks")
    suspend fun countTasks(): Int

    @Query("SELECT COUNT(*) FROM study_tasks WHERE is_done = 0")
    suspend fun countUnfinished(): Int
}

@Database(
    entities = [StudyTask::class],
    version = 1,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {

    abstract fun studyTaskDao(): StudyTaskDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getDatabase(context: Context): StudyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_database"
                ).build().also {
                    INSTANCE = it
                }
            }
    }
}

class TaskRepository(
    private val dao: StudyTaskDao
) {
    val allTasks: Flow<List<StudyTask>> = dao.getAllTasks()

    suspend fun seedInitialTasksIfEmpty() {
        if (dao.countTasks() == 0) {
            sampleStudyTasks().forEach {
                dao.insert(it.copy(id = 0))
            }
        }
    }

    suspend fun add(title: String, course: String) {
        dao.insert(
            StudyTask(title = title, course = course)
        )
    }

    suspend fun setDone(task: StudyTask, done: Boolean) {
        dao.update(task.copy(isDone = done))
    }

    suspend fun delete(task: StudyTask) {
        dao.delete(task)
    }
}

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
            delay(1500L)

            if (!preferences.getBoolean("sample_initialized", false)) {
                repository.seedInitialTasksIfEmpty()
                preferences.edit()
                    .putBoolean("sample_initialized", true)
                    .apply()
            }

            repository.allTasks.collect { tasks ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tasks = tasks
                    )
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
        val task = _uiState.value.tasks.find {
            it.id == id
        } ?: return

        viewModelScope.launch {
            repository.setDone(task, done)
            _uiState.update {
                it.copy(
                    message = if (done) {
                        "Marked as done"
                    } else {
                        "Marked as not done"
                    }
                )
            }
        }
    }

    fun onDeleteTask(id: Int) {
        val task = _uiState.value.tasks.find {
            it.id == id
        } ?: return

        viewModelScope.launch {
            repository.delete(task)
            _uiState.update {
                it.copy(message = "Task deleted")
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

class AirplaneModeReceiver(
    private val onChange: (Boolean) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        onChange(intent.getBooleanExtra("state", false))
    }
}

fun isAirplaneModeOn(context: Context): Boolean =
    Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.AIRPLANE_MODE_ON,
        0
    ) != 0

@Composable
fun rememberAirplaneModeOn(): Boolean {
    val context = LocalContext.current
    var isOn by remember {
        mutableStateOf(isAirplaneModeOn(context))
    }

    DisposableEffect(context) {
        val receiver = AirplaneModeReceiver {
            isOn = it
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    return isOn
}

const val REMINDER_CHANNEL_ID = "study_reminders"

fun createReminderChannel(context: Context) {
    val channel = NotificationChannel(
        REMINDER_CHANNEL_ID,
        "Study reminders",
        NotificationManager.IMPORTANCE_DEFAULT
    )

    channel.description = "Daily reminders of unfinished tasks"

    context.getSystemService(
        NotificationManager::class.java
    ).createNotificationChannel(channel)
}

fun showReminder(context: Context, unfinished: Int) {
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val notification = NotificationCompat.Builder(
        context,
        REMINDER_CHANNEL_ID
    )
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Study Planner")
        .setContentText(
            "You have $unfinished unfinished tasks."
        )
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context)
        .notify(1, notification)
}

@Composable
fun AskNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = StudyDatabase.getDatabase(
            applicationContext
        )

        val unfinished = database.studyTaskDao()
            .countUnfinished()

        if (unfinished > 0) {
            showReminder(applicationContext, unfinished)
        }

        return Result.success()
    }
}

fun scheduleDailyReminder(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()

    val request =
        PeriodicWorkRequestBuilder<ReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
}

@Serializable
object PlannerHome

@Serializable
data class TaskDetail(val title: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createReminderChannel(this)
        scheduleDailyReminder(this)

        setContent {
            MaterialTheme {
                StudyPlannerApp()
            }
        }
    }
}

@Composable
fun StudyPlannerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PlannerHome
    ) {
        composable<PlannerHome> {
            PlannerHomeRoute(
                onTaskClick = { title ->
                    navController.navigate(TaskDetail(title))
                }
            )
        }

        composable<TaskDetail> { entry ->
            val route = entry.toRoute<TaskDetail>()

            TaskDetailRoute(
                title = route.title,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun PlannerHomeRoute(
    onTaskClick: (String) -> Unit,
    viewModel: TodayViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFocusMode = rememberAirplaneModeOn()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    AskNotificationPermission()

    LaunchedEffect(uiState.message) {
        val message = uiState.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        TodayScreen(
            uiState = uiState,
            isFocusMode = isFocusMode,
            onAddTask = viewModel::onAddTask,
            onDoneChange = viewModel::onTaskCheckedChange,
            onDeleteTask = viewModel::onDeleteTask,
            onTaskClick = { id ->
                uiState.tasks.find { it.id == id }
                    ?.let { onTaskClick(it.title) }
            },
            onRemindMe = viewModel::onRemindMe,
            onHelpClick = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Tick a task when you finish it"
                    )
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    isFocusMode: Boolean,
    onAddTask: (String) -> Unit,
    onTaskClick: (Int) -> Unit,
    onDoneChange: (Int, Boolean) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onRemindMe: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlannerHomeScreen(
        uiState = PlannerUiState(tasks = uiState.tasks),
        isLoading = uiState.isLoading,
        isFocusMode = isFocusMode,
        onAddTask = onAddTask,
        onTaskClick = onTaskClick,
        onDoneChange = onDoneChange,
        onDeleteTask = onDeleteTask,
        onRemindMe = onRemindMe,
        onHelpClick = onHelpClick,
        modifier = modifier
    )
}

@Composable
fun PlannerHomeScreen(
    uiState: PlannerUiState,
    onAddTask: (String) -> Unit,
    onTaskClick: (Int) -> Unit,
    onDoneChange: (Int, Boolean) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onRemindMe: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isFocusMode: Boolean = false
) {
    val totalTasks = uiState.tasks.size
    val doneTasks = uiState.tasks.count { it.isDone }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                uiState.greeting,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "$doneTasks of $totalTasks done",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            Row {
                TextButton(onClick = onRemindMe) {
                    Text("Remind me")
                }

                TextButton(onClick = onHelpClick) {
                    Text("Help")
                }
            }

            if (isFocusMode) {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        "Focus mode: airplane mode is on. No distractions!",
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            AddTaskRow(onAddTask)

            Spacer(Modifier.height(20.dp))

            Text(
                "My Study Tasks",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (isLoading) {
            item {
                CircularProgressIndicator()
            }
        } else if (uiState.tasks.isEmpty()) {
            item {
                Text("No tasks yet. Add your first one above.")
            }
        } else {
            items(
                items = uiState.tasks,
                key = { it.id }
            ) { task ->
                TaskRow(
                    task = task,
                    onTaskClick = onTaskClick,
                    onDoneChange = onDoneChange,
                    onDeleteTask = onDeleteTask
                )
            }
        }
    }
}

@Composable
fun AddTaskRow(onAddTask: (String) -> Unit) {
    var newTitle by rememberSaveable {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newTitle,
            onValueChange = { newTitle = it },
            label = { Text("New task") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Button(
            onClick = {
                if (newTitle.isNotBlank()) {
                    onAddTask(newTitle)
                    newTitle = ""
                }
            }
        ) {
            Text("Add")
        }
    }
}

@Composable
fun TaskRow(
    task: StudyTask,
    onTaskClick: (Int) -> Unit,
    onDoneChange: (Int, Boolean) -> Unit,
    onDeleteTask: (Int) -> Unit
) {
    TaskItem(
        task = task,
        onTaskClick = onTaskClick,
        onDoneChange = onDoneChange,
        onDeleteTask = onDeleteTask
    )
}

@Composable
fun TaskItem(
    task: StudyTask,
    onTaskClick: (Int) -> Unit,
    onDoneChange: (Int, Boolean) -> Unit,
    onDeleteTask: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { checked ->
                    onDoneChange(task.id, checked)
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTaskClick(task.id) }
                    .padding(start = 8.dp)
            ) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isDone) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )

                Text(
                    task.course,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TextButton(onClick = { onDeleteTask(task.id) }) {
                Text("Delete")
            }
        }
    }
}

data class TaskDetailUiState(
    val title: String = ""
)

@Composable
fun TaskDetailRoute(
    title: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    TaskDetailScreen(
        uiState = TaskDetailUiState(title),
        onShareClick = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Today I'm studying: $title"
                )
            }

            context.startActivity(
                Intent.createChooser(
                    intent,
                    "Share Study Task"
                )
            )
        },
        onBackClick = onBackClick
    )
}

@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "Task Details",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(20.dp))

        Text(
            uiState.title,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onShareClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
fun PreviewHome(tasks: List<StudyTask>) {
    MaterialTheme {
        PlannerHomeScreen(
            uiState = PlannerUiState(tasks = tasks),
            onAddTask = {},
            onTaskClick = {},
            onDoneChange = { _, _ -> },
            onDeleteTask = {},
            onRemindMe = {},
            onHelpClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Nothing Done")
@Composable
fun HomeNothingDonePreview() {
    PreviewHome(
        sampleStudyTasks().map {
            it.copy(isDone = false)
        }
    )
}

@Preview(showBackground = true, name = "All Done")
@Composable
fun HomeAllDonePreview() {
    PreviewHome(
        sampleStudyTasks().map {
            it.copy(isDone = true)
        }
    )
}

@Preview(showBackground = true, name = "After Tap")
@Composable
fun HomeAfterTapPreview() {
    PreviewHome(sampleStudyTasks())
}

@Preview(showBackground = true, name = "Empty Tasks")
@Composable
fun HomeEmptyPreview() {
    PreviewHome(emptyList())
}

@Preview(showBackground = true, name = "Task Detail")
@Composable
fun TaskDetailPreview() {
    MaterialTheme {
        TaskDetailScreen(
            uiState = TaskDetailUiState(
                "Complete Kotlin Lab"
            ),
            onShareClick = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Task Not Done")
@Composable
fun TaskNotDonePreview() {
    MaterialTheme {
        TaskItem(
            task = StudyTask(
                1, "Study Power BI", "CSC403"
            ),
            onTaskClick = {},
            onDoneChange = { _, _ -> },
            onDeleteTask = {}
        )
    }
}

@Preview(showBackground = true, name = "Task Done")
@Composable
fun TaskDonePreview() {
    MaterialTheme {
        TaskItem(
            task = StudyTask(
                1, "Complete Kotlin Lab", "CSC402", true
            ),
            onTaskClick = {},
            onDoneChange = { _, _ -> },
            onDeleteTask = {}
        )
    }
}

