
package sa.edu.iau.cshj.studyplanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun PlannerHomeRoute(
    onTaskClick: (String) -> Unit
) {
    TodayRoute(onTaskClick = onTaskClick)
}

@Composable
fun TodayRoute(
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
            onTaskClick = { id ->
                viewModel.uiState.value.tasks
                    .find { it.id == id }
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
    onRemindMe: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlannerHomeScreen(
        uiState = PlannerUiState(
            tasks = uiState.tasks
        ),
        isLoading = uiState.isLoading,
        isFocusMode = isFocusMode,
        onAddTask = onAddTask,
        onTaskClick = onTaskClick,
        onDoneChange = onDoneChange,
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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            }

            Spacer(Modifier.height(8.dp))

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
                    onDoneChange = onDoneChange
                )
            }
        }
    }
}

@Composable
fun AddTaskRow(
    onAddTask: (String) -> Unit
) {
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
    onDoneChange: (Int, Boolean) -> Unit
) {
    TaskItem(
        task = task,
        onTaskClick = onTaskClick,
        onDoneChange = onDoneChange
    )
}

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
                .clickable { onTaskClick(task.id) }
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
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text("›")
        }
    }
}
