package com.example.homework

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
                            RecyclerViewImpl(LocalContext.current)
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
                context.startActivity(Intent(context, AddActivity::class.java))
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

/**@Composable
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
 }**/

@Composable
fun RecyclerViewImpl(context: Context) {
    var data: MutableList<Reminder>
    val db :DatabaseHandler = DatabaseHandler(context)
    var list2 = ArrayList<String>()
    data = db.readData()!!
    Log.d("V", data.toString())
    if (data.isEmpty()) {
        list2.add("Press Add Reminder to Add Your First Reminder")
    } else if (data.size == 1) {
        list2.add(data[0].messageToString())
    } else {
        for (i in 0 until data.size) {
            list2.add(data[i].messageToString())
        }
    }
    LazyColumn {
        items(list2) { it ->
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