package com.example.map_umkm

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        tvLogin.setOnClickListener {
            
            finish()
        }

        btnRegister.setOnClickListener {
            handleRegister()
        }
    }

    
    private fun handleRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
            return
        }

        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        
                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "role" to "user" 
                        )

                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                
                                Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show()
                                finish() 
                            }
                            .addOnFailureListener { e ->
                                
                                user.delete()
                                Toast.makeText(this, "Gagal menyimpan data user: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }

                } else {
                    
                    val errorMessage = task.exception?.message ?: "Registrasi gagal, coba lagi."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}
