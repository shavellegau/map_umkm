package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.map_umkm.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val email = prefs.getString("userEmail", null)
        val role = prefs.getString("userRole", "user")

        if (email == null) {
            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNav

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.paymentFragment,
                R.id.paymentSuccessFragment,
                R.id.qrisFragment,
                R.id.productDetailFragment,
                R.id.adminDashboardFragment,
                R.id.adminOrdersFragment,
                R.id.adminNotificationFragment,
                R.id.adminMenuFragment,
                R.id.adminVoucherFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }

        val openAdminFromLogin = intent.getBooleanExtra("openAdmin", false)

        if (openAdminFromLogin || role == "admin") {
            bottomNavigationView.visibility = View.GONE

            if (navController.currentDestination?.id != R.id.adminDashboardFragment) {

                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_home, true)
                    .build()

                navController.navigate(R.id.adminDashboardFragment, null, navOptions)
            }
        }

        saveUserFCMToken()

        FirebaseMessaging.getInstance().subscribeToTopic("promo")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM_SUB", "Berhasil subscribe ke promo")
                } else {
                    Log.e("FCM_SUB", "Gagal subscribe", task.exception)
                }
            }
    }

    private fun saveUserFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM_TOKEN", "Gagal ambil token", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM_TOKEN", "Token user: $token")
            getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                .edit()
                .putString("fcm_token", token)
                .apply()
        }
    }
}