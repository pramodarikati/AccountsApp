package com.example.accountsapp.ui.home

import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.accountsapp.R
import com.example.accountsapp.adapter.ItemPagingAdapter
import com.example.accountsapp.data.model.ItemEntity
import com.example.accountsapp.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var adapter: ItemPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()

        lifecycleScope.launch {
            viewModel.items.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadState ->

            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.items.collectLatest {
                adapter.submitData(it)
            }
        }

        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                viewModel.setSearchQuery(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.refresh()
    }

    private fun setupRecyclerView() {
        adapter = ItemPagingAdapter(
            onDeleteClick = { item ->
                viewModel.delete(item, requireContext())
            },
            onUpdateClick = { showUpdateDialog(it) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun showUpdateDialog(item: ItemEntity) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_item, null)

        val etName = dialogView.findViewById<EditText>(R.id.et_name)
        val etDetails = dialogView.findViewById<EditText>(R.id.et_details)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnUpdate = dialogView.findViewById<Button>(R.id.btn_update)



        etName.setText(item.name)
        val detailsWithoutBraces = item.details?.trim()?.removePrefix("{")?.removeSuffix("}") ?: ""
        etDetails.setText(detailsWithoutBraces)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnUpdate.setOnClickListener {
            val name = etName.text.toString().trim()
            var details = etDetails.text.toString().trim()

            if (name.isEmpty() || details.isEmpty()) {
                Toast.makeText(requireContext(), "Name and Details cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!details.startsWith("{") && !details.endsWith("}")) {
                details = "{$details}"
            }

            val updated = item.copy(
                name = name,
                details = details
            )
            viewModel.update(updated)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
