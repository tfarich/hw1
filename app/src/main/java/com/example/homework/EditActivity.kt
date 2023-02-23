package com.example.homework

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.button.MaterialButton
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class EditActivity : AppCompatActivity() {

    private lateinit var imageButton: Button
    private lateinit var imageView: ImageView
    private lateinit var imageUri: Uri
    private var imageChanged by Delegates.notNull<Boolean>()

    companion object {
        val IMAGE_REQUEST_CODE = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val message = findViewById<TextView>(R.id.message)
        imageButton = findViewById<MaterialButton>(R.id.gallerybtn)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val locationButton = findViewById<MaterialButton>(R.id.locationbtn)
        val imgView = findViewById<ImageView>(R.id.preview)
        imageChanged = false

        imageButton = findViewById<MaterialButton>(R.id.gallerybtn)
        imageView = findViewById<ImageView>(R.id.preview)

        imageButton.setOnClickListener {
            imagePicker()
        }

        val simpleCheckBox = findViewById<View>(R.id.checkBox) as CheckBox
        var checkBoxState = false

        simpleCheckBox.setOnClickListener {
            checkBoxState = checkBoxState != true
        }

        var reminder = Reminder(0, "",0, 0, "", "", 0, 0, "")

        val extras = intent.extras
        if (extras != null) {
            var id = 0
            var message2 = ""
            var location_x = 0
            var location_y = 0
            var creator_id = 0
            var reminder_seen = 0
            var creator_icon = ""
            id = extras.getInt("ID")
            //Log.v("ID: ", id.toString())
            message2 = extras.getString("message").toString()
            location_x = extras.getInt("location_x")
            location_y = extras.getInt("location_y")
            var reminder_time : Long = extras.getLong("reminder_time")
            var creation_time : Long = extras.getLong("creation_time")
            creator_id = extras.getInt("creator_id")
            reminder_seen = extras.getInt("reminder_seen")
            creator_icon = extras.getString("reminder_icon").toString()
            imgView.setImageURI(Uri.parse(creator_icon))
            message.setText(message2.toString(), TextView.BufferType.EDITABLE)
            val reminder2 = Reminder(id, message2, location_x, location_y, reminder_time.toString(), creation_time.toString(), creator_id, reminder_seen, creator_icon)
            reminder = reminder2
            val db: DatabaseHandler = DatabaseHandler(this)
        }

        val createButton = findViewById<MaterialButton>(R.id.createbtn)
        val calendar: Calendar = Calendar.getInstance()

        createButton.setOnClickListener {
            reminder.message = message.text.toString()
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour())
            calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute())
            reminder.reminder_time = calendar.getTimeInMillis().toString()
            if (imageChanged) {
                reminder.reminder_icon = imageUri.toString()
            }

            var context = this
            var db = DatabaseHandler(context)

            if (checkBoxState) {
                val time = Calendar.getInstance()
                val timeDiff = (calendar.timeInMillis / 1000L) - (time.timeInMillis / 1000L)
                if (timeDiff > 0) {
                    reminder.creation_time = time.timeInMillis.toString()
                    Log.v("Time difference", timeDiff.toString())
                    val myWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInitialDelay(timeDiff, TimeUnit.SECONDS)
                        .setInputData(
                            workDataOf(
                                "title" to "Reminder",
                                "message" to message.text.toString()
                            )
                        ).build()
                    WorkManager.getInstance(it.context).enqueue(myWorkRequest)
                }
            } else {
                reminder.reminder_time = reminder.creation_time
            }
            db.updateData(reminder)

            Toast.makeText(this, "REMINDER UPDATED", Toast.LENGTH_SHORT).show()
            switchActivities()
            }

        val backButton = findViewById<MaterialButton>(R.id.backbtn)
        backButton.setOnClickListener {
            switchActivities()
        }

        }
    private fun switchActivities() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun imagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent, AddActivity.IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddActivity.IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            imageChanged = true
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
}
