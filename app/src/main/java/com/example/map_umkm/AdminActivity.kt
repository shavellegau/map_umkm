package com.example.map_umkm

import android.content.Intent
import android.os.Bundle
import android.widget.Toast // [FIXED] Import yang hilang ditambahkan
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.map_umkm.adapter.AdminPageAdapter
import com.example.map_umkm.data.JsonHelper
import android.widget.Button
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AdminActivity : AppCompatActivity() {

    private lateinit var jsonHelper: JsonHelper
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_viewpager) // Pastikan layout ini ada

        jsonHelper = JsonHelper(this)
        viewPager = findViewById(R.id.view_pager_admin)
        tabLayout = findViewById(R.id.tab_layout_admin)

        viewPager.adapter = AdminPageAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Manajemen Menu"
                1 -> "Konfirmasi Pesanan"
                else -> null
            }
        }.attach()

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        setupOrderNotification()
    }

    private fun setupListeners() {
        val fab: FloatingActionButton = findViewById(R.id.fab_add_product)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)

        fab.setOnClickListener {
            if (viewPager.currentItem == 0) { // Hanya aktif jika di tab menu
                startActivity(Intent(this, AddProductActivity::class.java))
            } else {
                Toast.makeText(this, "Fitur tambah hanya untuk tab menu", Toast.LENGTH_SHORT).show()
            }
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    fun setupOrderNotification() {
        val newOrdersCount = jsonHelper.getMenuData()?.orders
            ?.count { it.status == "Menunggu Konfirmasi" } ?: 0

        val orderTab = tabLayout.getTabAt(1)
        if (orderTab != null) {
            if (newOrdersCount > 0) {
                val badge = orderTab.orCreateBadge
                badge.number = newOrdersCount
            } else {
                orderTab.removeBadge()
            }
        }
    }

    private fun showLogoutConfirmation() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_confirm, null)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnLogout = dialogView.findViewById<Button>(R.id.btnLogout)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnLogout.setOnClickListener {
            dialog.dismiss()
            logout()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun logout() {
        getSharedPreferences("USER_SESSION", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
