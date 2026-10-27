
package sa.edu.iau.cshj.studyplanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

// Main Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                StudyPlannerApp()
            }
        }
    }
}

// Part 4 - Task Data
data class StudyTask(
    val id: Int,
    val title: String,
    val course: String,
    val isDone: Boolean = false
)

// Part 1 and Part 4 - UI State
data class PlannerUiState(
    val greeting: String = "Welcome to StudyPlanner!",
    val tasks: List<StudyTask> = emptyList()
)

// Sample Tasks
fun sampleStudyTasks(): List<StudyTask> = listOf(
    StudyTask(1, "Complete Kotlin Lab", "CSC402", true),
    StudyTask(2, "Study Power BI", "CSC403", false),
    StudyTask(3, "Review Image Processing", "ARTI407", true),
    StudyTask(4, "Prepare Presentation", "CSC401", false),
    StudyTask(5, "Finish Assignment", "CSC402", false)
)

// Part 2 - Navigation Routes
@Serializable
object PlannerHome

@Serializable
data class TaskDetail(val title: String)

// Part 2 - Main App Navigation
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
                    navController.navigate(
                        TaskDetail(title)
                    )
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

// Save task state when the screen is recreated
private val plannerStateSaver =
    Saver<PlannerUiState, ArrayList<String>>(
        save = { state ->
            arrayListOf(state.greeting).apply {
                state.tasks.forEach { task ->
                    add(task.id.toString())
                    add(task.title)
                    add(task.course)
                    add(task.isDone.toString())
                }
            }
        },
        restore = { saved ->
            val tasks = saved.drop(1)
                .chunked(4)
                .mapNotNull { values ->
                    if (values.size != 4) {
                        null
                    } else {
                        val id = values[0].toIntOrNull()
                        if (id == null) null
                        else StudyTask(
                            id = id,
                            title = values[1],
                            course = values[2],
                            isDone = values[3].toBoolean()
                        )
                    }
                }

            PlannerUiState(
                greeting = saved.firstOrNull()
                    ?: "Welcome to StudyPlanner!",
                tasks = tasks
            )
        }
    )

// Part 1 and Part 4 - State Holder
@Composable
fun PlannerHomeRoute(
    onTaskClick: (String) -> Unit
) {
    var uiState by rememberSaveable(
        stateSaver = plannerStateSaver
    ) {
        mutableStateOf(
            PlannerUiState(
                tasks = sampleStudyTasks()
            )
        )
    }

    PlannerHomeScreen(
        uiState = uiState,

        onAddTask = {
            val nextId =
                (uiState.tasks.maxOfOrNull { it.id } ?: 0) + 1

            val newTask = StudyTask(
                id = nextId,
                title = "New Study Task $nextId",
                course = "General"
            )

            uiState = uiState.copy(
                tasks = uiState.tasks + newTask
            )
        },

        onTaskClick = { id ->
            val task = uiState.tasks.find {
                it.id == id
            }

            if (task != null) {
                onTaskClick(task.title)
            }
        },

        onDoneChange = { id, checked ->
            uiState = uiState.copy(
                tasks = uiState.tasks.map { task ->
                    if (task.id == id) {
                        task.copy(isDone = checked)
                    } else {
                        task
                    }
                }
            )
        }
    )
}

// Part 1 and Part 4 - Stateless Home Screen
@Composable
fun PlannerHomeScreen(
    uiState: PlannerUiState,
    onAddTask: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onDoneChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
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
                text = uiState.greeting,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$doneTasks of $totalTasks done",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onAddTask) {
                Text("Add Task")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "My Study Tasks",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (uiState.tasks.isEmpty()) {
            item {
                Text("No tasks yet")
            }
        }

        items(
            items = uiState.tasks,
            key = { it.id }
        ) { task ->
            TaskItem(
                task = task,
                onTaskClick = onTaskClick,
                onDoneChange = onDoneChange
            )
        }
    }
}

// Part 4 - Reusable Task Item
@Composable
fun TaskItem(
    task: StudyTask,
    onTaskClick: (Int) -> Unit,
    onDoneChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onTaskClick(task.id)
                }
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
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isDone)
                        TextDecoration.LineThrough
                    else
                        TextDecoration.None
                )

                Text(
                    text = task.course,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(">")
        }
    }
}

// Part 2 - Task Detail State
data class TaskDetailUiState(
    val title: String = ""
)

// Part 2 - Task Detail Route
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

// Part 2 - Stateless Detail Screen
@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Task Details",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = uiState.title,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onShareClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

// Part 1 - Home Preview 1
@Preview(showBackground = true, name = "Empty Home")
@Composable
fun HomeEmptyPreview() {
    MaterialTheme {
        PlannerHomeScreen(
            uiState = PlannerUiState(),
            onAddTask = {},
            onTaskClick = {},
            onDoneChange = { _, _ -> }
        )
    }
}

// Part 1 and Part 4 - Home Preview 2
@Preview(showBackground = true, name = "Home With Five Tasks")
@Composable
fun HomeTasksPreview() {
    MaterialTheme {
        PlannerHomeScreen(
            uiState = PlannerUiState(
                tasks = sampleStudyTasks()
            ),
            onAddTask = {},
            onTaskClick = {},
            onDoneChange = { _, _ -> }
        )
    }
}

// Part 2 - Detail Preview 1
@Preview(showBackground = true, name = "Kotlin Task")
@Composable
fun TaskDetailPreview1() {
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

// Part 2 - Detail Preview 2
@Preview(showBackground = true, name = "Power BI Task")
@Composable
fun TaskDetailPreview2() {
    MaterialTheme {
        TaskDetailScreen(
            uiState = TaskDetailUiState(
                "Study Power BI"
            ),
            onShareClick = {},
            onBackClick = {}
        )
    }
}

// Part 4 - Task Not Done Preview
@Preview(showBackground = true, name = "Task Not Done")
@Composable
fun TaskNotDonePreview() {
    MaterialTheme {
        TaskItem(
            task = StudyTask(
                1, "Study Power BI", "CSC403"
            ),
            onTaskClick = {},
            onDoneChange = { _, _ -> }
        )
    }
}

// Part 4 - Task Done Preview
@Preview(showBackground = true, name = "Task Done")
@Composable
fun TaskDonePreview() {
    MaterialTheme {
        TaskItem(
            task = StudyTask(
                1, "Complete Kotlin Lab", "CSC402", true
            ),
            onTaskClick = {},
            onDoneChange = { _, _ -> }
        )
    }
}
