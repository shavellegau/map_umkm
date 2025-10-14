package com.example.map_umkm

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.util.*

class EditProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val menuBirthday = view.findViewById<LinearLayout>(R.id.menuBirthday)
        val tvBirthday = view.findViewById<TextView>(R.id.tvBirthday)
        val menuGender = view.findViewById<LinearLayout>(R.id.menuGender)
        val tvGender = view.findViewById<TextView>(R.id.tvGender)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCloseReward = view.findViewById<ImageView>(R.id.btnCloseReward)

        // Tombol back
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Pilih tanggal lahir
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

        // Pilih gender
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

        // Tutup reward info
        btnCloseReward.setOnClickListener {
            it.visibility = View.GONE
            Toast.makeText(requireContext(), "Reward dismissed", Toast.LENGTH_SHORT).show()
        }

//        val layoutRewardInfo = view.findViewById<LinearLayout>(R.id.layoutRewardInfo)
//        btnCloseReward.setOnClickListener {
//            layoutRewardInfo.visibility = View.GONE
//            Toast.makeText(requireContext(), "Reward dismissed", Toast.LENGTH_SHORT).show()
//        }


        // Tombol save
        btnSave.setOnClickListener {
            Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        return view
    }
}
