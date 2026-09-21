package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OrderPuzzleScreen()
        }
    }
}

@Composable
fun ChainA(
    modifier: Modifier = Modifier
) {
    Text(
        text = "content",
        modifier = modifier
            .background(Color(0xFF3DDC84))
            .padding(24.dp)
    )
}

@Composable
fun ChainB(
    modifier: Modifier = Modifier
) {
    Text(
        text = "content",
        modifier = modifier
            .padding(24.dp)
            .background(Color(0xFF3DDC84))
    )
}

@Composable
fun ChainC(
    modifier: Modifier = Modifier
) {
    Text(
        text = "content",
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF3DDC84))
            .padding(24.dp)
    )
}

@Composable
fun OrderPuzzleScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(text = "Chain A - Result 2")

        ChainA()

        Spacer(modifier = Modifier.size(24.dp))

        Text(text = "Chain B - Result 1")

        ChainB()

        Spacer(modifier = Modifier.size(24.dp))

        Text(text = "Chain C - Result 3")

        ChainC()

        Spacer(modifier = Modifier.size(40.dp))

        Text(
            text = "Clickable then padding",
            modifier = Modifier
                .clickable { }
                .padding(16.dp)
                .background(Color(0xFF3DDC84))
        )

        Spacer(modifier = Modifier.size(20.dp))

        Text(
            text = "Padding then clickable",
            modifier = Modifier
                .padding(16.dp)
                .clickable { }
                .background(Color(0xFF3DDC84))
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderPuzzlePreview() {
    OrderPuzzleScreen()
}
