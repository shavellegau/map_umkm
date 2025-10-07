package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.OrderAdapter
import com.example.map_umkm.model.Product
import java.io.Serializable
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import java.text.NumberFormat
import java.util.Locale

class PaymentFragment : Fragment() {

    // Kunci untuk Bundle
    companion object {
        private const val ARG_CART_LIST = "cart_list"

        fun newInstance(cartList: ArrayList<Product>): PaymentFragment {
            val fragment = PaymentFragment()
            val args = Bundle()
            args.putSerializable(ARG_CART_LIST, cartList)
            fragment.arguments = args
            return fragment
        }
    }

    // Properti untuk menyimpan data keranjang
    private var cartList: List<Product>? = null
    private lateinit var rvOrderList: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotalPayment: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cartList = it.getSerializable(ARG_CART_LIST) as? List<Product>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        val backButton = view.findViewById<Button>(R.id.btnBack)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Inisialisasi RecyclerView dan TextView
        rvOrderList = view.findViewById(R.id.rv_order_list)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment)

        // Set up RecyclerView jika cartList tidak kosong
        if (!cartList.isNullOrEmpty()) {
            val orderAdapter = OrderAdapter(cartList as MutableList<Product>)
            rvOrderList.layoutManager = LinearLayoutManager(requireContext())
            rvOrderList.adapter = orderAdapter
        }

        // Hitung dan tampilkan total harga terlepas dari isi cartList
        calculateAndDisplayTotals()

        return view
    }

    private fun calculateAndDisplayTotals() {
        val subtotal = cartList?.sumOf { it.price * it.quantity.toDouble() } ?: 0.0
        val tax = subtotal * 0.1
        val total = subtotal + tax


        // Format angka ke mata uang Rupiah
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        tvSubtotal.text = currencyFormat.format(subtotal)
        tvTax.text = currencyFormat.format(tax)
        tvTotalPayment.text = currencyFormat.format(total)
    }
}