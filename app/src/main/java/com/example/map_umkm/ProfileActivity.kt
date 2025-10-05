package com.example.map_umkm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var tvGreeting: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvGreeting = findViewById(R.id.tvGreeting)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        btnLogout = findViewById(R.id.btnLogout)

        val uid = auth.currentUser?.uid

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "User"
                        tvGreeting.text = "Hi, $name 👋"
                    } else {
                        tvGreeting.text = "Hi, User 👋"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memuat data profil", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnEdit.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Akun")
                .setMessage("Apakah kamu yakin ingin menghapus akun ini?")
                .setPositiveButton("Ya") { _, _ ->
                    uid?.let {
                        db.collection("users").document(it).delete()
                        auth.currentUser?.delete()
                        Toast.makeText(this, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, RegisterActivity::class.java))
                        finish()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
