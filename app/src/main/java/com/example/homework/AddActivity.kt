package com.example.homework

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.android.material.button.MaterialButton
import java.util.*
import java.util.concurrent.TimeUnit


class AddActivity : AppCompatActivity() {

    private lateinit var imageButton: Button
    private lateinit var imageView: ImageView
    private lateinit var imageUri: Uri
    var latitude = -200.0
    var longitude = -200.0

    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

    val RECORDAUDIOREQUESTCODE = 1
    private val speechRecognizer: SpeechRecognizer? = null
    var micButton: ImageView? = null
    lateinit var message: TextView
    private val REQUEST_CODE_SPEECH_INPUT = 1
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        message = findViewById<TextView>(R.id.message)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val locationButton = findViewById<MaterialButton>(R.id.locationbtn)
        val micButton = findViewById<MaterialButton>(R.id.micButton)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        }

        micButton.setOnClickListener {
            // on below line we are calling speech recognizer intent.
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            // on below line we are passing language model
            // and model free form in our intent
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // on below line we are passing our
            // language as a default language.
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            // on below line we are specifying a prompt
            // message as speak to text on below line.
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            // on below line we are specifying a try catch block.
            // in this block we are calling a start activity
            // for result method and passing our result code.
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                // on below line we are displaying error message in toast
                Toast
                    .makeText(
                        this@AddActivity, " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }

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

        locationButton.setOnClickListener {
            switchToMapActivities()
        }

        val extras = intent.extras
        if (extras != null) {
            longitude = extras.getDouble("longitude")
            latitude = extras.getDouble("latitude")
            Log.v("After maps","Not empty")
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
                    if (longitude > -200.0 && latitude > -200.0) {
                        reminder.location_x = (longitude * 1000000.0).toInt()
                        reminder.location_y = (latitude * 1000000.0).toInt()
                    } else {
                        reminder.location_x = -200
                        reminder.location_y = -200
                    }
                    //Log.v("longitude before", reminder.location_x.toString())
                    //Log.v("latitude before", reminder.location_y.toString())
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
        } else if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            // on below line we are checking if result code is ok
            if (resultCode == RESULT_OK && data != null) {

                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                // on below line we are setting data
                // to our output text view.
                message.setText(
                    Objects.requireNonNull(res)[0]
                )
            }
        }
    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun switchToMapActivities() {
        val switchActivityIntent = Intent(this, LocationPicker::class.java)
        startActivity(switchActivityIntent)
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORDAUDIOREQUESTCODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORDAUDIOREQUESTCODE && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) Toast.makeText(
                this,
                "Permission Granted",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}