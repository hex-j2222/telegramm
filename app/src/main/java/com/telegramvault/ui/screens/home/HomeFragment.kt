package com.telegramvault.ui.screens.home

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.telegramvault.R
import com.telegramvault.data.model.TelegramAccount
import com.telegramvault.data.model.UiState
import com.telegramvault.databinding.FragmentHomeBinding
import com.telegramvault.ui.components.AccountsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: AccountsAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeState()
        observeEvents()
    }

    private fun setupRecyclerView() {
        adapter = AccountsAdapter(
            onAccountClick = { viewModel.onAccountClicked(it) },
            onDeleteClick  = { showDeleteDialog(it) }
        )
        binding.rvAccounts.adapter = adapter
        binding.rvAccounts.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        binding.fabAddAccount.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addAccount)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible  = state is UiState.Loading
                    binding.emptyState.isVisible   = state is UiState.Empty
                    binding.rvAccounts.isVisible   = state is UiState.Success
                    if (state is UiState.Success) adapter.submitList(state.data)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is HomeEvent.OpenProfile -> {
                            val bundle = android.os.Bundle().apply {
                                putParcelable("account", event.account)
                            }
                            findNavController().navigate(R.id.action_home_to_profile, bundle)
                        }
                        is HomeEvent.AccountDeleted ->
                            Snackbar.make(binding.root,
                                getString(R.string.account_deleted, event.name),
                                Snackbar.LENGTH_SHORT).show()
                        is HomeEvent.Error ->
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(account: TelegramAccount) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_account_title)
            .setMessage(getString(R.string.delete_account_message, account.displayName))
            .setPositiveButton(R.string.delete)  { _, _ -> viewModel.onDeleteAccount(account) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
