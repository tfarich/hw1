package com.example.homework

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.google.android.material.button.MaterialButton
import java.util.*
import java.util.concurrent.TimeUnit


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

        imageButton = findViewById<MaterialButton>(R.id.gallerybtn)
        imageView = findViewById<ImageView>(R.id.preview)

        imageButton.setOnClickListener {
            imagePicker()
        }

        imageUri = Uri.parse("0")

        var preview = findViewById<ImageView>(R.id.preview)

        val imagePath = ""
        val calendar: Calendar = Calendar.getInstance()

        var reminder = Reminder(0, "",0, 0, "", "", 0, 0, "")

        // create reminder
        val createButton = findViewById<MaterialButton>(R.id.createbtn)

        val simpleCheckBox = findViewById<View>(R.id.checkBox) as CheckBox
        var checkBoxState = false

        simpleCheckBox.setOnClickListener {
            checkBoxState = checkBoxState != true
        }

        createButton.setOnClickListener {
            if (message.text.isNullOrEmpty()) {
                Toast.makeText(this, "MUST ADD MESSAGE", Toast.LENGTH_SHORT).show()
            } else {
                val time = Calendar.getInstance()
                reminder.message = message.text.toString()
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                //Log.v("Hour being chosen", timePicker.hour.toString())
                calendar.set(Calendar.MINUTE, timePicker.minute)
                //Log.v("Minute being chosen", timePicker.minute.toString())
                val calendarCurr = Calendar.getInstance()
                calendar.set(Calendar.SECOND, 0)
                reminder.reminder_time = calendar.timeInMillis.toString()

                reminder.creation_time = time.timeInMillis.toString()
                //val dateFormat = SimpleDateFormat("EEE MMM dd hh:mm:ss 'GMT'Z yyyy")
                //Log.v("reminder_time", dateFormat.format(calendar.getTime()))
                //Log.v("creation time", dateFormat.format(time.getTime()))
                if (imageUri.toString() != "0") {
                    reminder.reminder_icon = imageUri.toString()
                    //Log.v("Image:", imageUri.toString())

                    var context = this
                    var db = DatabaseHandler(context)
                    Log.v("THE REMINDER ADDED", reminder.toString())

                    if (checkBoxState) {
                        val timeDiff = (calendar.timeInMillis/1000L)-(time.timeInMillis/1000L)
                        //Log.v("Time difference", timeDiff.toString())
                        val myWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                            .setInitialDelay(timeDiff, TimeUnit.SECONDS)
                            .setInputData(workDataOf(
                                "title" to "Reminder",
                                "message" to message.text.toString()
                            )).build()
                        //WorkManager.getInstance(requireContext()).enqueue(myWorkRequest)
                        WorkManager.getInstance(it.context).enqueue(myWorkRequest)
                    } else {
                        reminder.reminder_time = reminder.creation_time
                    }
                    db.insertData(reminder)
                    switchActivities()
                } else {
                    Toast.makeText(this, "MUST CHOOSE IMAGE", Toast.LENGTH_SHORT).show()
                }
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