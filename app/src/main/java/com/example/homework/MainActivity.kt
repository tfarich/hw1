package com.example.homework

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scaffoldState = rememberScaffoldState(
                rememberDrawerState(DrawerValue.Closed)
            )
            Scaffold(
                content = {
                    ComposePlaygroundTheme {
                        Surface(color = LightBlue) {
                            RecyclerViewImpl()
                        }
                    }
                },
                bottomBar = { BottomBar() }
            )
        }
    }
}

@Composable
fun BottomBar() {
    val selectedIndex = remember { mutableStateOf(0) }
    val context = LocalContext.current
    BottomNavigation(backgroundColor = androidx.compose.ui.graphics.Color.White, elevation = 10.dp) {

        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Default.ArrowBack, "")
        },
            label = { Text(text = "Log Out") },
            selected = (selectedIndex.value == 0),
            onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
            })
        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Default.Add, "")
        },
            label = { Text(text = "Add Reminder") },
            selected = (selectedIndex.value == 1),
            onClick = {
                selectedIndex.value = 0
            })
        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Default.Person, "")
        },
            label = { Text(text = "Profile") },
            selected = (selectedIndex.value == 2),
            onClick = {
                context.startActivity(Intent(context, ProfileActivity::class.java))
            })
    }
}

@Composable
fun RecyclerViewImpl() {
    val list = mutableListOf<String>()
    for (i in 1..5) {
        list.add("Placeholder $i")
    }
    LazyColumn {
        items(list) { it ->
            Card(modifier = Modifier.padding(8.dp)) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = it, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
