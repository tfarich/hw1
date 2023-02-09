package com.example.homework

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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

@Composable
fun RecyclerViewImpl(context: Context) {
    var data: MutableList<Reminder>
    val db: DatabaseHandler = DatabaseHandler(context)
    //var list2 = ArrayList<String>()
    data = db.readData()!!
    //Log.d("V", data.toString())
    if (data.isEmpty()) {
        LazyColumn {
            var list = ArrayList<String>()
            list.add("Press Add Reminder to Add Your First Reminder")
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
    } else {
        LazyColumn {
            items(data) { it ->
                Card(modifier = Modifier.padding(8.dp)) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Row() {
                            Text(text = it.messageToString(),
                                modifier = Modifier.padding(8.dp))
                            Spacer(Modifier.weight(1f))
                            Button(border = null, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = Color.Black), onClick = { editItem(it.id) },) {
                                Icon(imageVector = Icons.Default.Edit, "", tint = black)
                            }
                            Button(colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = Color.Black), onClick = {  }) {
                                Icon(imageVector = Icons.Default.Delete, "", tint = black)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun editItem(id: Int) {
    print(id)
    // trun this int oa view
}