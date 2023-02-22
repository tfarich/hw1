package com.example.homework

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.startActivity
import coil.compose.rememberImagePainter
import coil.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        //this.deleteDatabase(DATABASE_NAME)
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
    val context = LocalContext.current
    var dialogOpen by remember { mutableStateOf(false) }
    var id by remember { mutableStateOf(0) }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = {
                dialogOpen = false
            },
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Black
                    ),
                    onClick = {
                        db.deleteData(id)
                        Log.v("ID: ", id.toString())
                        val switchActivityIntent = Intent(context, MainActivity::class.java)
                        context.startActivity(switchActivityIntent)
                }) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Black
                    ),
                    onClick = {
                    dialogOpen = false
                }) {
                    Text(text = "Do not Delete")
                }
            },
            title = {
                Text(text = "Do You Wish to Delete this Reminder?")
            },
            text = {
                Text(text = "This action cannot be undone")
            },
            modifier = Modifier // Set the width and padding
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(5.dp),
            backgroundColor = Color.White,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }

    data = db.readData()!!
    var reminderData: MutableList<Reminder> = mutableListOf()

    for (d in data) {
        val currentTime = Calendar.getInstance().timeInMillis
        val reminderTime = d.reminder_time.toLong()
        val id = d.id
        Log.v("id", id.toString())
        val timeDiff = (reminderTime/1000L)-(currentTime/1000L)
        Log.v("reminder diff", timeDiff.toString())
        Log.v("reminder_time", reminderTime.toString())
        Log.v("creation time", currentTime.toString())
        if (timeDiff < 0) {
            reminderData.add(d)
        }
    }
    if (reminderData.isEmpty()) {
        LazyColumn {
            var list = ArrayList<String>()
            list.add("No Current Reminders")
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
            items(reminderData) { it ->
                Card(modifier = Modifier.padding(8.dp)) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Row() {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it.reminder_icon)
                                .build(),
                            contentDescription = "icon",
                            contentScale = ContentScale.Inside,
                            modifier = Modifier.size(30.dp)
                            )
                            /*val painter = rememberImagePainter(data = File(it.reminder_icon))
                            Image(
                                modifier = Modifier
                                    .padding(5.dp),
                                contentScale = ContentScale.FillHeight,
                                painter = painter,
                                contentDescription = "",
                            )*/
                            Text(text = it.messageToString(),
                                modifier = Modifier.padding(8.dp))
                            Spacer(Modifier.weight(1f))
                            Button(
                                border = null,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(width = 40.dp, height = 35.dp),
                                onClick = {
                                    val switchActivityIntent = Intent(context, EditActivity::class.java)
                                    switchActivityIntent.putExtra("ID", it.id)
                                    switchActivityIntent.putExtra("message", it.message)
                                    switchActivityIntent.putExtra("location_x", it.location_x)
                                    switchActivityIntent.putExtra("location_y", it.location_y)
                                    switchActivityIntent.putExtra("reminder_time", it.reminder_time)
                                    switchActivityIntent.putExtra("creation_time", it.creation_time)
                                    switchActivityIntent.putExtra("creator_id", it.creator_id)
                                    switchActivityIntent.putExtra("reminder_seen", it.reminder_seen)
                                    switchActivityIntent.putExtra("reminder_icon", it.reminder_icon)
                                    context.startActivity(switchActivityIntent)
                                },
                            ) {
                                Icon(imageVector = Icons.Default.Edit, "")
                            }
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(width = 40.dp,height = 35.dp),
                                onClick = {
                                    dialogOpen = true
                                    id = it.id
                                }) {
                                Icon(imageVector = Icons.Default.Delete, "")
                            }
                        }
                    }
                }
            }
        }
    }
}