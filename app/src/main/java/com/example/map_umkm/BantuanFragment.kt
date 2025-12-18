package com.example.map_umkm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.map_umkm.databinding.FragmentBantuanBinding
import java.net.URLEncoder

class BantuanFragment : Fragment() {

    private var _binding: FragmentBantuanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBantuanBinding.inflate(inflater, container, false)

        setupListeners()
        setupSearchEngine()

        return binding.root
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnChatLangsung.setOnClickListener { openWhatsApp() }
        binding.btnKirimEmail.setOnClickListener { openEmail() }

        binding.itemProdukToko.setOnClickListener {
            toggleExpand(binding.expandProduk, binding.imgArrowProduk)
        }

        binding.itemPesananMaya.setOnClickListener {
            toggleExpand(binding.expandPesanan, binding.imgArrowPesanan)
        }
    }

    private fun toggleExpand(expandLayout: View, arrow: View) {
        if (expandLayout.visibility == View.VISIBLE) {
            expandLayout.visibility = View.GONE
            arrow.animate().rotation(0f).setDuration(200).start()
        } else {
            expandLayout.visibility = View.VISIBLE
            arrow.animate().rotation(90f).setDuration(200).start()
        }
    }

    private fun setupSearchEngine() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterContent(s.toString().lowercase().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterContent(query: String) {
        if (query.isEmpty()) {
            binding.itemProdukToko.visibility = View.VISIBLE
            binding.itemPesananMaya.visibility = View.VISIBLE
            return
        }

        val matchProduk = "produk dan toko lokasi jam buka menu".contains(query)
        val matchPesanan = "pesanan maya status order pengiriman driver batal".contains(query)

        binding.itemProdukToko.visibility = if (matchProduk) View.VISIBLE else View.GONE
        binding.itemPesananMaya.visibility = if (matchPesanan) View.VISIBLE else View.GONE
    }

    private fun openWhatsApp() {
        val phoneNumber = "6281234567890"
        val message = "Halo Tetangga Tuku, saya butuh bantuan terkait aplikasi."
        try {
            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${URLEncoder.encode(message, "UTF-8")}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.whatsapp"))
        } catch (e: Exception) {
            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${URLEncoder.encode(message, "UTF-8")}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun openEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("halo@tuku.coffee"))
            putExtra(Intent.EXTRA_SUBJECT, "Bantuan Aplikasi Tetangga Tuku")
            putExtra(Intent.EXTRA_TEXT, "Halo Tim Tuku,\n\nSaya ingin bertanya mengenai...")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Tidak ada aplikasi Email", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}