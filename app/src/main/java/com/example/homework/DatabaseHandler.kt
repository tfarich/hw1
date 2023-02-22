package com.example.homework

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.media.Image
import android.util.Log
import android.widget.Toast
import java.sql.Timestamp

val DATABASE_NAME = "MyDB"
val TABLE_NAME = "Reminders"
val REMINDER_ID = "reminder_id"
val COL_MESSAGE = "message"
val COL_LOCATION_X = "location_x"
val COL_LOCATION_Y = "location_y"
val COL_REMINDER_TIME = "reminder_time"
val COL_CREATION_TIME = "creation_time"
val COL_CREATOR_ID = "creator_id"
val COL_REMINDER_SEEN = "reminder_seen"
val COL_REMINDER_ICON = "icon" // path

class DatabaseHandler(var context: Context) : SQLiteOpenHelper(context,DATABASE_NAME,null,1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE " + TABLE_NAME + " (" + REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_LOCATION_X + " INTEGER," + COL_LOCATION_Y + " INTEGER," + COL_REMINDER_TIME + " VARCHAR(256)," + COL_CREATION_TIME + " VARCHAR(256)," + COL_CREATOR_ID + " INTEGER," + COL_REMINDER_SEEN + " INTEGER," + COL_MESSAGE + " VARCHAR(256)," + COL_REMINDER_ICON + " BLOB)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun insertData(reminder : Reminder) {
        val db = this.writableDatabase
        var cv = ContentValues()
        cv.put(COL_MESSAGE, reminder.message)
        cv.put(COL_LOCATION_X, reminder.location_x)
        cv.put(COL_LOCATION_Y, reminder.location_y)
        cv.put(COL_REMINDER_TIME, reminder.reminder_time)
        cv.put(COL_CREATION_TIME, reminder.creation_time)
        cv.put(COL_CREATOR_ID, reminder.creator_id)
        cv.put(COL_REMINDER_SEEN, reminder.reminder_seen)
        cv.put(COL_REMINDER_ICON, reminder.reminder_icon)
        var result = db.insert(TABLE_NAME,null,cv)
        if(result == (-1).toLong()) {
            Toast.makeText(context, "Failed to Save Reminder", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "REMINDER CREATED", Toast.LENGTH_SHORT).show()
        }
        db.close()
    }

    @SuppressLint("Range")
    fun readData() : MutableList<Reminder>{
        var list : MutableList<Reminder> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from " + TABLE_NAME
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val reminder = Reminder(0, "",0,0,"","",0, 0,"")
                reminder.id = result.getString(result.getColumnIndex(REMINDER_ID)).toInt()
                reminder.message = result.getString(result.getColumnIndex(COL_MESSAGE))
                reminder.reminder_time = result.getString(result.getColumnIndex(COL_REMINDER_TIME))
                reminder.reminder_icon = result.getString(result.getColumnIndex(COL_REMINDER_ICON))
                list.add(reminder)
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return list
    }

    @SuppressLint("Range")
    fun updateData(reminder: Reminder) {
        val db = this.writableDatabase
        var cv = ContentValues()
        cv.put(COL_MESSAGE, reminder.message)
        cv.put(COL_LOCATION_X, reminder.location_x)
        cv.put(COL_LOCATION_Y, reminder.location_y)
        cv.put(COL_REMINDER_TIME, reminder.reminder_time)
        cv.put(COL_CREATION_TIME, reminder.creation_time)
        cv.put(COL_CREATOR_ID, reminder.creator_id)
        cv.put(COL_REMINDER_SEEN, reminder.reminder_seen)
        cv.put(COL_REMINDER_ICON, reminder.reminder_icon)
        db.update(TABLE_NAME, cv, "REMINDER_ID=?", arrayOf(reminder.id.toString()))
        db.close()
    }

    fun deleteData(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "REMINDER_ID=?", arrayOf(id.toString()))
        db.close()
    }
}