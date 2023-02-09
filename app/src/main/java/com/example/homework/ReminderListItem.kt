package com.example.homework

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ReminderListItem(reminder: Reminder) {
    Row {
        Column {
            Text(text = reminder.message, style = Typography.h1)
            Text(text = "VIEW MORE", style = Typography.caption)
        }
    }
}