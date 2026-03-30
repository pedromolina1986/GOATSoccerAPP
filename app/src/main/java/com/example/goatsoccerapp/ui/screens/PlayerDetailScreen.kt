package com.example.goatsoccerapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goatsoccerapp.ui.theme.GOATSoccerAPPTheme
import kotlinx.serialization.EncodeDefault

//player object needed
@Composable
fun PlayerDetailScreen(){
    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
            .padding(10.dp)
    ) {
        Row() {
            Text("Player Detail",
                fontSize = 32.sp,)
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.End
            ) {

                //edit button
                Button(
                    onClick = {}
                ) {
                    Text("Edit")
                }
                //delete button
                Button(
                    onClick = {}
                ) {
                    Text("Delete")
                }
            }
        }
        Row(
            Modifier.fillMaxWidth()
                .padding(0.dp, 20.dp),) {
            /*player image
            Image(
                painter = {},
                contentDescription = "Player Image"
            )
             */
            //Other player detail
            Column() {
                Text("PlaceHolder Player Name",)
                Text("Player position")
                //extra tags
                Row() { }
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween) {
            Column() {
                Text("number")
                Text("Goal")
            }
            Column() {
                Text("number")
                Text("Assist")
            }
            Column() {
                Text("number")
                Text("Matches")
            }
            Column() {
                Text("number")
                Text("Rating")
            }
        }
        Box(){
            Text("Overall Performance")
            //graph shown here
        }
        Box(){
            Text("Recent Matches")
            LazyColumn() {
                //Card() { }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerDetailPreview() {
    GOATSoccerAPPTheme {
        PlayerDetailScreen()
    }
}