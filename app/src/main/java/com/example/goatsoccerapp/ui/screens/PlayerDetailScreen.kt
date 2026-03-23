package com.example.goatsoccerapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

//player object needed
@Composable
fun PlayerDetailScreen(){
    Column(
        Modifier.statusBarsPadding()
    ) {
        Row() {
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
        Row() {
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
        Row() {
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

