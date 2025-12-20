package com.example.map_umkm

import android.os.Bundle
import android.util.Log
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
    private lateinit var etReferralCode: EditText
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
        etReferralCode = findViewById(R.id.etReferralCode)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        tvLogin.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            handleRegister()
        }
    }

    private fun handleRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val referralInput = etReferralCode.text.toString().trim().uppercase()

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
                            "role" to "user",
                            "tukuPoints" to 0,
                            "currentXp" to 0,
                            "ownReferralCode" to uid.take(6).uppercase()
                        )

                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                if (referralInput.isNotEmpty()) {
                                    checkAndApplyReferral(uid, referralInput)
                                } else {
                                    Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                            .addOnFailureListener { e ->
                                user.delete()
                                Toast.makeText(this, "Gagal simpan data: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(this, task.exception?.message ?: "Registrasi gagal", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkAndApplyReferral(myUid: String, inputCode: String) {
        db.collection("users").whereEqualTo("ownReferralCode", inputCode).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Kode referral tidak valid, akun tetap dibuat.", Toast.LENGTH_LONG).show()
                    finish()
                    return@addOnSuccessListener
                }

                val referrerUid = documents.documents[0].id
                executeReferralReward(myUid, referrerUid, inputCode)
            }
    }

    private fun executeReferralReward(myUid: String, referrerUid: String, code: String) {
        val voucherData = hashMapOf(
            "title" to "Voucher Diskon 50%",
            "desc" to "Bonus pendaftaran kode $code",
            "code" to "REF-$code",
            "isActive" to true,
            "discountAmount" to 50000.0,
            "createdAt" to System.currentTimeMillis()
        )

        db.runTransaction { transaction ->
            val myRef = db.collection("users").document(myUid)
            val referrerRef = db.collection("users").document(referrerUid)

            transaction.update(myRef, "referredBy", code)

            transaction.set(myRef.collection("vouchers").document(), voucherData)
            transaction.set(referrerRef.collection("vouchers").document(), voucherData)
            null
        }.addOnSuccessListener {
            Toast.makeText(this, "Registrasi Berhasil! Bonus 50% aktif.", Toast.LENGTH_LONG).show()
            finish()
        }.addOnFailureListener {
            Log.e("Register", "Referral error: ${it.message}")
            finish()
        }
    }
}