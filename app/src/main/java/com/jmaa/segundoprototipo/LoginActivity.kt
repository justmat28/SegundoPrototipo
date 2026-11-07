package com.jmaa.segundoprototipo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jmaa.segundoprototipo.databinding.ActivityLoginBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                iniciarSesion(email, password)
            } else {
                Toast.makeText(this, "Ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarSesion(emailText: String, passwordText: String) {
        binding.btnLogin.isEnabled = false
        binding.progressLogin.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                SupabaseManager.client.auth.signInWith(Email) {
                    email = emailText
                    password = passwordText
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Sesión autorizada", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnLogin.isEnabled = true
                    binding.progressLogin.visibility = View.GONE
                    Toast.makeText(this@LoginActivity, "Error de autenticación: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}