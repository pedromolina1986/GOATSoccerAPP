package com.example.goatsoccerapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.goatsoccerapp.ui.theme.GOATSoccerAPPTheme

@Composable
fun TeamDetailScreen(){
    Column(
        Modifier.statusBarsPadding()
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row() {
            //team logo
            //Image()
            Column() {
                Text("Team Name")
                Text("Coach: ")
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween) {
            Column() {
                Text("number")
                Text("Wins")
            }
            Column() {
                Text("number")
                Text("Draws")
            }
            Column() {
                Text("number")
                Text("Loses")
            }
            Column() {
                Text("number")
                Text("Point")
            }
        }
        Row(Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween) {
            Text("Roster")
            Button(
                onClick = {}
            ) { }
        }
        Column() { }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamDetailPreview() {
    GOATSoccerAPPTheme {
        TeamDetailScreen()
    }
}