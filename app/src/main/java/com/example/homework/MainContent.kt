package com.example.homework

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

@Composable
fun MainContent() {
    //val reminders = remember { this is where sql goes }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        //items(
            //items = reminders,
            //itemContent = {
                //ReminderListItem(reminder = it)
           // }
        //)
    }
}