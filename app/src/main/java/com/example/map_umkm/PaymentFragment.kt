    package com.example.map_umkm

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.TextView
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.activityViewModels
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.example.map_umkm.adapter.OrderAdapter
    import com.example.map_umkm.model.Product
    import com.example.map_umkm.viewmodel.CartViewModel
    import java.text.NumberFormat
    import java.util.Locale

    class PaymentFragment : Fragment() {

        private val cartViewModel: CartViewModel by activityViewModels()

        private lateinit var rvOrderList: RecyclerView
        private lateinit var tvSubtotal: TextView
        private lateinit var tvTax: TextView
        private lateinit var tvTotalPayment: TextView

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_payment, container, false)

            val backButton = view.findViewById<Button>(R.id.btnBack)
            rvOrderList = view.findViewById(R.id.rv_order_list)
            tvSubtotal = view.findViewById(R.id.tvSubtotal)
            tvTax = view.findViewById(R.id.tvTax)
            tvTotalPayment = view.findViewById(R.id.tvTotalPayment)

            backButton.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            val currentCart = cartViewModel.cartList.value ?: mutableListOf()
            val adapter = OrderAdapter(currentCart) { updatedList ->
                cartViewModel.updateCartList(updatedList)
                calculateAndDisplayTotals()
            }

            rvOrderList.layoutManager = LinearLayoutManager(requireContext())
            rvOrderList.adapter = adapter

            calculateAndDisplayTotals()

            return view
        }

    private fun calculateAndDisplayTotals() {
        val subtotal = cartList?.sumOf { it.price * it.quantity } ?: 0
        val tax = subtotal * 0.1
        val total = subtotal + tax

        // Format angka ke mata uang Rupiah
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        tvSubtotal.text = currencyFormat.format(subtotal)
        tvTax.text = currencyFormat.format(tax)
        tvTotalPayment.text = currencyFormat.format(total)
    }
}