package com.example.map_umkm.utils

import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

fun Fragment.setupBackButton(backButton: ImageView?) {
    backButton?.setOnClickListener {
        findNavController().navigateUp()
    }
}
