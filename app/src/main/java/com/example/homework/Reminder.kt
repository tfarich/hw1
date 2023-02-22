package com.example.homework

import android.media.Image
import java.sql.Blob
import java.sql.Timestamp

data class Reminder (
    var id: Int,
    var message: String,
    var location_x: Int,
    val location_y: Int,
    var reminder_time: String,
    var creation_time: String,
    val creator_id: Int,
    val reminder_seen: Int,
    var reminder_icon: String

) {
    override fun toString(): String {
        return "Reminder(message='$message', location_x=$location_x, location_y=$location_y, reminder_time=$reminder_time, creation_time=$creation_time, creator_id=$creator_id, reminder_seen=$reminder_seen, reminder_icon=$reminder_icon)"
    }

    fun messageToString(): String {
        return message
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reminder

        if (id != other.id) return false
        if (message != other.message) return false
        if (location_x != other.location_x) return false
        if (location_y != other.location_y) return false
        if (reminder_time != other.reminder_time) return false
        if (creation_time != other.creation_time) return false
        if (creator_id != other.creator_id) return false
        if (reminder_seen != other.reminder_seen) return false
        if (reminder_icon != other.reminder_icon) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + location_x.hashCode()
        result = 31 * result + location_y.hashCode()
        result = 31 * result + reminder_time.hashCode()
        result = 31 * result + creation_time.hashCode()
        result = 31 * result + creator_id
        result = 31 * result + reminder_seen.hashCode()
        result = 31 * result + reminder_icon.hashCode()
        return result
    }
}