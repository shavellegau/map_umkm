package com.example.map_umkm

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val menuBirthday = view.findViewById<LinearLayout>(R.id.menuBirthday)
        val tvBirthday = view.findViewById<TextView>(R.id.tvBirthday)
        val menuGender = view.findViewById<LinearLayout>(R.id.menuGender)
        val tvGender = view.findViewById<TextView>(R.id.tvGender)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCloseReward = view.findViewById<ImageView>(R.id.btnCloseReward)

        val etNickname = view.findViewById<EditText>(R.id.etNickname)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)

        etNickname.setText(prefs.getString("userName", ""))
        tvEmail.text = prefs.getString("userEmail", "")

        tvBirthday.text = prefs.getString("userBirthday", "Select your birthday")
        tvGender.text = prefs.getString("userGender", "Select gender")

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        menuBirthday.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(requireContext(), { _, y, m, d ->
                tvBirthday.text = "$d/${m + 1}/$y"
                tvBirthday.setTextColor(resources.getColor(R.color.tuku_dark, null))
            }, year, month, day)

            dpd.show()
        }

        menuGender.setOnClickListener {
            val options = arrayOf("Male", "Female", "Other")
            val builder = android.app.AlertDialog.Builder(requireContext())

            builder.setTitle("Select Gender")
            builder.setItems(options) { _, which ->
                tvGender.text = options[which]
                tvGender.setTextColor(resources.getColor(R.color.tuku_dark, null))
            }

            builder.show()
        }

        btnCloseReward.setOnClickListener {
            it.visibility = View.GONE
            Toast.makeText(requireContext(), "Reward dismissed", Toast.LENGTH_SHORT).show()
        }


        // ==========================================================
        // ===============  MODIF BAGIAN SAVE =======================
        // ==========================================================

        btnSave.setOnClickListener {
            val newName = etNickname.text.toString().trim()

            val editor = prefs.edit()
            editor.putString("userName", newName)
            editor.putString("userBirthday", tvBirthday.text.toString())
            editor.putString("userGender", tvGender.text.toString())
            editor.apply()

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(uid)
                .update("name", newName)
                .addOnSuccessListener {

                    val user = FirebaseAuth.getInstance().currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = newName
                    }

                    user?.updateProfile(profileUpdates)
                    user?.reload()

                    Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
        }

        return view
    }
}
