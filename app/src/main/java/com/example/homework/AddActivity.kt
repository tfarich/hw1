package com.example.homework

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.content.Context
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import java.util.*


class AddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val message = findViewById<TextView>(R.id.message)
        val galleryButton = findViewById<MaterialButton>(R.id.gallerybtn)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val locationButton = findViewById<MaterialButton>(R.id.locationbtn)
        val time = System.currentTimeMillis()

        val uri = ""

        galleryButton.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, 500)
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
                reminder.reminder_icon = uri

                var context = this
                var db = DatabaseHandler(context)
                db.insertData(reminder)

                Toast.makeText(this, "REMINDER CREATED", Toast.LENGTH_SHORT).show()
                switchActivities()
            }
        }
    }
    private fun switchActivities() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }
}



