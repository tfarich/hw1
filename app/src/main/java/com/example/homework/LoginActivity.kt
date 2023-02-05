package com.example.homework

import android.content.Context
import android.content.Intent
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.util.concurrent.Executor

private lateinit var executor: Executor
private lateinit var biometricPrompt: BiometricPrompt
private lateinit var promptInfo: BiometricPrompt.PromptInfo


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPreferences = this.getSharedPreferences("Login", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putString("username", "admin")
        editor.putString("password", "admin")
        editor.commit()

        val username = findViewById<TextView>(R.id.username)
        val password = findViewById<TextView>(R.id.password)

        val loginButton = findViewById<MaterialButton>(R.id.loginbtn)
        val createAccountButton = findViewById<MaterialButton>(R.id.accountbtn)

        loginButton.setOnClickListener {
            val sharedPreferences1 = this.getSharedPreferences("Login", Context.MODE_PRIVATE)
            if (username.text.toString() == sharedPreferences1.getString(
                    "username",
                    ""
                ) && password.text.toString() == sharedPreferences.getString("password", "")
            ) {
                Toast.makeText(this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show()
                switchActivities()
            } else {
                Toast.makeText(this, "INCORRECT LOGIN", Toast.LENGTH_SHORT).show()
            }
        }
        createAccountButton.setOnClickListener {
            switchActivitiesAccount()
        }
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                    switchActivities()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Login")
            .setNegativeButtonText("Use Username and Password")
            .build()

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        val biometricLoginButton =
            findViewById<MaterialButton>(R.id.fingerprintbtn)
        biometricLoginButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun switchActivitiesAccount() {
        val switchActivityIntent = Intent(this, AccountActivity::class.java)
        startActivity(switchActivityIntent)
    }

}