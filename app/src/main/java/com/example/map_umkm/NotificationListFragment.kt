package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.NotificationAdapter
import com.example.map_umkm.databinding.FragmentNotificationListBinding
import com.example.map_umkm.model.Notification
import com.example.map_umkm.model.NotificationEntity
import com.example.map_umkm.viewmodel.NotificationViewModel

import com.example.map_umkm.AppDatabase
import com.example.map_umkm.repository.NotificationRepository
import com.example.map_umkm.viewmodel.NotificationViewModelFactory

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationListFragment : Fragment() {

    private var _binding: FragmentNotificationListBinding? = null
    private val binding get() = _binding!!

    
    private val viewModel: NotificationViewModel by activityViewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = NotificationRepository(database.notificationDao())
        NotificationViewModelFactory(repository)
    }

    private lateinit var adapter: NotificationAdapter
    private var type: String = "INFO"

    companion object {
        fun newInstance(type: String): NotificationListFragment {
            val fragment = NotificationListFragment()
            val args = Bundle()
            args.putString("TYPE", type)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        type = arguments?.getString("TYPE") ?: "INFO"

        adapter = NotificationAdapter(emptyList())
        binding.recyclerNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotificationListFragment.adapter
        }

        
        if (type == "PROMO") {
            viewModel.promoList.observe(viewLifecycleOwner) { updateUI(it) }
        } else {
            viewModel.infoList.observe(viewLifecycleOwner) { updateUI(it) }
        }
    }

    private fun updateUI(list: List<NotificationEntity>) {
        if (list.isNotEmpty()) {
            binding.tvEmptyNotification.visibility = View.GONE
            binding.recyclerNotifications.visibility = View.VISIBLE

            val displayList = list.map { entity ->
                val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val timestampString = formatter.format(Date(entity.timestamp))
                Notification(entity.title, entity.body, timestampString)
            }
            adapter.updateList(displayList)
        } else {
            binding.recyclerNotifications.visibility = View.GONE
            binding.tvEmptyNotification.visibility = View.VISIBLE
            binding.tvEmptyNotification.text = if(type == "PROMO") "Belum ada promo" else "Belum ada pesanan"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}