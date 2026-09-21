package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF062B49)
val Green = Color(0xFF2ECC71)
val ScreenBackground = Color(0xFFF3F6F8)

data class Course(
    val code: String,
    val title: String,
    val credits: Int,
    val days: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CourseListScreen()
        }
    }
}

@Composable
fun CourseListScreen(modifier: Modifier = Modifier) {

    val courses = remember {
        listOf(
            Course(
                code = "CSC 402",
                title = "Mobile Application Programming",
                credits = 3,
                days = "Mon / Wed"
            ),
            Course(
                code = "CSC 311",
                title = "Database Systems",
                credits = 4,
                days = "Sun / Tue"
            ),
            Course(
                code = "CSC 340",
                title = "Operating Systems",
                credits = 3,
                days = "Mon / Wed"
            ),
            Course(
                code = "MATH 202",
                title = "Discrete Mathematics",
                credits = 3,
                days = "Sun / Thu"
            ),
            Course(
                code = "ENG 214",
                title = "Technical Writing",
                credits = 2,
                days = "Tue"
            ),
            Course(
                code = "PHYS 105",
                title = "General Physics II",
                credits = 4,
                days = "Sun / Thu"
            ),
            Course(
                code = "ISLM 101",
                title = "Islamic Culture",
                credits = 2,
                days = "Tue"
            )
        )
    }

    Column(
        modifier = modifier
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
                text = "My Courses",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Task 10 - LazyColumn & data class",
                color = Green,
                fontSize = 12.sp
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            items(courses) { course ->
                CourseRow(course)
            }
        }
    }
}

@Composable
fun CourseRow(course: Course) {

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
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(Green)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 14.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    )
            ) {

                Text(
                    text = course.code,
                    color = Green,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = course.title,
                    color = Navy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = course.days,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .padding(end = 14.dp)
                    .background(
                        color = Color(0xFFDDF5E7),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    )
            ) {

                Text(
                    text = "${course.credits} cr",
                    color = Color(0xFF15803D),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CourseListScreenPreview() {
    CourseListScreen()
}
