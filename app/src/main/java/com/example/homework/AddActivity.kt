package com.example.homework

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.ByteArrayOutputStream
import java.util.*


class AddActivity : AppCompatActivity() {

    private lateinit var imageButton: Button
    private lateinit var imageView: ImageView
    private lateinit var imageUri: Uri

    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val message = findViewById<TextView>(R.id.message)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val locationButton = findViewById<MaterialButton>(R.id.locationbtn)
        val time = System.currentTimeMillis()

        imageButton = findViewById<MaterialButton>(R.id.gallerybtn)
        imageView = findViewById<ImageView>(R.id.preview)

        imageButton.setOnClickListener {
            imagePicker()
        }

        var preview = findViewById<ImageView>(R.id.preview)

        val imagePath = ""
        val calendar: Calendar = Calendar.getInstance()

        var reminder = Reminder(0, "",0, 0, 0, 0, 0, 0, "")

        // create reminder
        val createButton = findViewById<MaterialButton>(R.id.createbtn)

        createButton.setOnClickListener {
            if (message.text.isNullOrEmpty()) {
                Toast.makeText(this, "MUST ADD MESSAGE", Toast.LENGTH_SHORT).show()
            } else {
                reminder.message = message.text.toString()
                calendar.set(Calendar.HOUR, timePicker.getCurrentHour())
                calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute())
                reminder.reminder_time = calendar.getTimeInMillis()
                reminder.creation_time = time
                reminder.reminder_icon = imageUri.toString()
                Log.v("Image:", imageUri.toString())

                var context = this
                var db = DatabaseHandler(context)
                db.insertData(reminder)

                Toast.makeText(this, "REMINDER CREATED", Toast.LENGTH_SHORT).show()
                switchActivities()
            }
        }

        val backButton = findViewById<MaterialButton>(R.id.backbtn)
        backButton.setOnClickListener {
            switchActivities()
        }
    }

    private fun imagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent,IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val contentResolver = applicationContext.contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            if (data != null) {
                imageUri = data.data!!
            }
            imageUri?.let { contentResolver.takePersistableUriPermission(it, takeFlags) }
            //Log.v("alright", imageUri.toString())
            imageView.setImageURI(data?.data)
        }
    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }
}