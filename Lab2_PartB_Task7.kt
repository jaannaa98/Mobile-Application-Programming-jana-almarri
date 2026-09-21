package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF062B49)
val Green = Color(0xFF2ECC71)
val ScreenBackground = Color(0xFFF3F6F8)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StudentIdScreen()
        }
    }
}

@Composable
fun StudentIdScreen(modifier: Modifier = Modifier) {

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
                text = "Student ID",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Task 7 - Row & Column",
                color = Green,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Navy),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "JA",
                        color = Green,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                Column {

                    Text(
                        text = "Jana Almarri",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )

                    Text(
                        text = "Computer Science",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "YOUR STUDENT ID",
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFDDF5E7))
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 5.dp
                                )
                        ) {

                            Text(
                                text = "Level 4",
                                color = Color(0xFF15803D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.size(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE5E7EB))
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 5.dp
                                )
                        ) {

                            Text(
                                text = "CSHJ",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                StatItem(
                    value = "4.42",
                    label = "GPA"
                )

                StatItem(
                    value = "78",
                    label = "Credits"
                )

                StatItem(
                    value = "Active",
                    label = "Status"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = Navy,
            fontSize = 16.sp
        )

        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudentIdScreenPreview() {
    StudentIdScreen()
}
