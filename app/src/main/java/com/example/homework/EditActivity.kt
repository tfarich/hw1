package com.example.homework

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.button.MaterialButton
import java.util.*

class EditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val message = findViewById<TextView>(R.id.message)
        val galleryButton = findViewById<MaterialButton>(R.id.gallerybtn)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val locationButton = findViewById<MaterialButton>(R.id.locationbtn)

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
            message2 = extras.getString("message").toString()
            location_x = extras.getInt("location_x")
            location_y = extras.getInt("location_y")
            var reminder_time : Long = extras.getLong("reminder_time")
            var creation_time : Long = extras.getLong("creation_time")
            creator_id = extras.getInt("creator_id")
            reminder_seen = extras.getInt("reminder_seen")
            creator_icon = extras.getString("reminder_icon").toString()
            message.setText(message2.toString(), TextView.BufferType.EDITABLE)
            val reminder = Reminder(id, message2, location_x, location_y, reminder_time, creation_time, creator_id, reminder_seen, creator_icon)
            val db: DatabaseHandler = DatabaseHandler(this)
        }

        val createButton = findViewById<MaterialButton>(R.id.createbtn)
        var reminder = Reminder(0, "",0, 0, 0, 0, 0, 0, "")
        val calendar: Calendar = Calendar.getInstance()

        createButton.setOnClickListener {
            reminder.message = message.text.toString()
            calendar.set(Calendar.HOUR, timePicker.getCurrentHour())
            calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute())
            reminder.reminder_time = calendar.getTimeInMillis()

            var context = this
            var db = DatabaseHandler(context)
            db.updateData(reminder)

            Toast.makeText(this, "REMINDER UPDATED", Toast.LENGTH_SHORT).show()
            switchActivities()
            }
        }
    private fun switchActivities() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }
}
