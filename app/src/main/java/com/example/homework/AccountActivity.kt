package com.example.homework

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val email = findViewById<TextView>(R.id.email)
        val username = findViewById<TextView>(R.id.username)
        val password = findViewById<TextView>(R.id.password)
        val passwordConfirm = findViewById<TextView>(R.id.passwordconfirm)

        val createButton = findViewById<MaterialButton>(R.id.createbtn)

        createButton.setOnClickListener {
            //var emailRegex = Regex("^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$")
            val match = android.util.Patterns.EMAIL_ADDRESS.matcher(email.text).matches()
            if (email.text.isNullOrEmpty() || username.text.isNullOrEmpty() || password.text.isNullOrEmpty() || passwordConfirm.text.isNullOrEmpty()) {
                Toast.makeText(this, "MUST COMPLETE ALL FIELDS", Toast.LENGTH_SHORT).show()
            } else if (!match) {
                Toast.makeText(this, "PLEASE USE A VALID EMAIL ADDRESS", Toast.LENGTH_SHORT).show()
            } else if (password.text.toString() == passwordConfirm.text.toString()) {
                Toast.makeText(this, "ACCOUNT CREATED", Toast.LENGTH_SHORT).show()
                switchActivities()
            } else {
                Toast.makeText(this, "PASSWORDS MUST MATCH", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, LoginActivity::class.java)
        startActivity(switchActivityIntent)
    }
}