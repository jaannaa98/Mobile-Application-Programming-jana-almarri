package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF062B49)
val Green = Color(0xFF2ECC71)
val ScreenBackground = Color(0xFFF3F6F8)

data class TeamTask(
    val id: Int,
    val title: String,
    val owner: String,
    val isDone: Boolean = false
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TeamTaskTracker()
        }
    }
}

@Composable
fun TeamTaskTracker() {

    val tasks = remember {
        mutableStateListOf(
            TeamTask(
                id = 1,
                title = "Create GitHub repository",
                owner = "Fatimah",
                isDone = true
            ),
            TeamTask(
                id = 2,
                title = "Write project proposal",
                owner = "Omar",
                isDone = true
            ),
            TeamTask(
                id = 3,
                title = "Design the login screen",
                owner = "Sara"
            ),
            TeamTask(
                id = 4,
                title = "Set up Android Studio",
                owner = "Noura"
            )
        )
    }

    val doneCount = tasks.count { it.isDone }
    val openCount = tasks.count { !it.isDone }
    val totalCount = tasks.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy)
                .padding(16.dp)
        ) {

            Text(
                text = "Team Task Tracker",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Task 11 - Challenge",
                color = Green,
                fontSize = 12.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Navy
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    SummaryItem(
                        value = doneCount.toString(),
                        label = "Done"
                    )

                    SummaryItem(
                        value = openCount.toString(),
                        label = "Open"
                    )

                    SummaryItem(
                        value = totalCount.toString(),
                        label = "Total"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(
                    items = tasks,
                    key = { it.id }
                ) { task ->

                    TaskRow(
                        task = task,
                        onToggle = {

                            val index = tasks.indexOfFirst {
                                it.id == task.id
                            }

                            if (index != -1) {
                                tasks[index] = task.copy(
                                    isDone = !task.isDone
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {

                    val nextId =
                        (tasks.maxOfOrNull { it.id } ?: 0) + 1

                    tasks.add(
                        TeamTask(
                            id = nextId,
                            title = "New task",
                            owner = "Jana Almarri"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = Navy
                ),
                shape = RoundedCornerShape(10.dp)
            ) {

                Text(
                    text = "+  Add task",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    value: String,
    label: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = value,
            color = Green,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
fun TaskRow(
    task: TeamTask,
    onToggle: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = task.isDone,
                onCheckedChange = {
                    onToggle()
                }
            )

            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {

                Text(
                    text = task.title,
                    color = if (task.isDone) {
                        Color.Gray
                    } else {
                        Navy
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textDecoration = if (task.isDone) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )

                Text(
                    text = task.owner,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamTaskTrackerPreview() {
    TeamTaskTracker()
}
