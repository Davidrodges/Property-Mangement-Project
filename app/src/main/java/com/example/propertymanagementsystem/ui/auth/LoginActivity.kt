package com.example.propertymanagementsystem.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.propertymanagementsystem.databinding.ActivityLoginBinding
import com.example.propertymanagementsystem.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var registeredEmail: String? = null
    private var registeredPassword: String? = null

    private val registerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                registeredEmail = data.getStringExtra("REGISTERED_EMAIL")
                registeredPassword = data.getStringExtra("REGISTERED_PASSWORD")
                registeredEmail?.let { email ->
                    binding.etEmail.setText(email)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            if (validateForm()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()

                if (registeredEmail != null && registeredPassword != null) {
                    if (email != registeredEmail || password != registeredPassword) {
                        Toast.makeText(this, "Invalid credentials entered", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }

                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("USER_EMAIL", email)
                }
                startActivity(intent)
                finish()
            }
        }

        binding.btnRegisterNav.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            registerLauncher.launch(intent)
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }
}
