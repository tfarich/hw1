package com.example.homework

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import coil.compose.rememberImagePainter
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.material.button.MaterialButton
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    //private val CAMERA_REQUEST_CODE = 1

    //@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    private lateinit var photoFile: File
    lateinit var currentPhotoPath: String
    private val PICTURE_FROM_CAMERA: Int = 1
    private var picture : ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val backButton = findViewById<MaterialButton>(R.id.backbtn)
        backButton.setOnClickListener {
            switchActivities()
        }
        initViews()
        registerListener()
    }

    private fun initViews() {
        picture = findViewById<ImageView>(R.id.preview)
    }

    private fun registerListener() {
        picture!!.setOnClickListener {
            takePicture()
        }
    }

    private fun takePicture() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = createImageFile()
        val uri = FileProvider.getUriForFile(this, "com.example.homework.fileprovider", photoFile)
        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(pictureIntent, PICTURE_FROM_CAMERA)
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_",
        ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICTURE_FROM_CAMERA) {
                val uri = FileProvider.getUriForFile(this, "com.example.homework.fileprovider", photoFile)
                picture!!.setImageURI(uri)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }
}
 /*       setContent {
            val scaffoldState = rememberScaffoldState(
                rememberDrawerState(DrawerValue.Closed)
            )
            Scaffold(
                content = {
                    ComposePlaygroundTheme {
                        Surface(color = LightBlue) {
                            ProfilePage()
                        }
                    }
                },
                bottomBar = { BottomBarProfile() }
            )
        }
    }
}

@Composable
fun ProfilePage() {

    val notification = rememberSaveable { mutableStateOf("") }
    if (notification.value.isNotEmpty()) {
        Toast.makeText(LocalContext.current, notification.value, Toast.LENGTH_LONG).show()
        notification.value = ""
    }

    var name by rememberSaveable { mutableStateOf("Admin McAdmin") }
    var username by rememberSaveable { mutableStateOf("admin") }
    var bio by rememberSaveable { mutableStateOf("This is your bio.") }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "", color = black, modifier = Modifier.width(40.dp))

            ProfileImage()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name", color = black, modifier = Modifier.width(40.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Username", color = black, modifier = Modifier.width(40.dp))
            TextField(
                value = username,
                onValueChange = { username = it },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Bio", color = black, modifier = Modifier
                    .width(40.dp)
                    .padding(top = 8.dp)
            )
            TextField(
                value = bio,
                onValueChange = { bio = it },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                ),
                singleLine = false,
                modifier = Modifier.height(150.dp)
            )
        }
    }
}

@Composable
fun ProfileImage() {
    val imageUri = rememberSaveable { mutableStateOf("") }
    val painter = rememberImagePainter(
        if (imageUri.value.isEmpty())
            R.drawable.login_background
        else
            imageUri.value
    )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri.value = it.toString() }
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier
                .padding(8.dp)
                .size(100.dp)
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .wrapContentSize()
                    .clickable { launcher.launch("image/*") },
                contentScale = ContentScale.Crop
            )
        }
        Text(text = "Change profile picture")
    }
}

@Composable
fun BottomBarProfile() {
    val selectedIndex = remember { mutableStateOf(0) }
    val context = LocalContext.current
    BottomNavigation(backgroundColor = Color.White, elevation = 10.dp) {

        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Default.ArrowBack, "")
        },
            label = { Text(text = "Log Out") },
            selected = (selectedIndex.value == 0),
            onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
            })
        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Default.Home, "")
        },
            label = { Text(text = "Main Page") },
            selected = (selectedIndex.value == 1),
            onClick = {
                context.startActivity(Intent(context, MainActivity::class.java))
            })
        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Default.Person, "")
        },
            label = { Text(text = "Profile") },
            selected = (selectedIndex.value == 2),
            onClick = {
                selectedIndex.value == 2
            })
    }
}*/