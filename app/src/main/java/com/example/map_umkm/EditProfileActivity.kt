package com.example.map_umkm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etName: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etName = findViewById(R.id.etName)
        btnSave = findViewById(R.id.btnSave)

        val uid = auth.currentUser?.uid

        uid?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { doc ->
                    etName.setText(doc.getString("name"))
                }
        }

        btnSave.setOnClickListener {
            val newName = etName.text.toString()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uid?.let {
                db.collection("users").document(it)
                    .update("name", newName)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profil diperbarui!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal update profil", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
