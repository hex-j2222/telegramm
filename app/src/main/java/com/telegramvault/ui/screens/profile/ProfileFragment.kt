package com.telegramvault.ui.screens.profile

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.telegramvault.R
import com.telegramvault.data.model.TelegramAccount
import com.telegramvault.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get account from arguments (API 33+ aware)
        val account = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("account", TelegramAccount::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("account")
        }
        account?.let { viewModel.loadAccount(it) }

        setupButtons()
        observeState()
        observeEvents()
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener   { findNavController().navigateUp() }
        binding.btnEdit.setOnClickListener   { toggleEditMode(true) }
        binding.btnCancel.setOnClickListener { toggleEditMode(false) }
        binding.btnSave.setOnClickListener {
            viewModel.updateProfile(
                binding.etFirstName.text.toString().trim(),
                binding.etLastName.text.toString().trim(),
                binding.etBio.text.toString().trim()
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state is ProfileUiState.Loading
                    if (state is ProfileUiState.Loaded) renderAccount(state.account)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is ProfileEvent.UpdateSuccess -> {
                            toggleEditMode(false)
                            Snackbar.make(binding.root, R.string.profile_updated, Snackbar.LENGTH_SHORT).show()
                        }
                        is ProfileEvent.Error ->
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun renderAccount(a: TelegramAccount) {
        binding.tvName.text     = a.displayName
        binding.tvPhone.text    = a.phoneNumber
        binding.tvUsername.text = if (a.username.isNotEmpty()) "@${a.username}" else ""
        binding.tvBio.text      = a.bio.ifEmpty { "—" }
        binding.etFirstName.setText(a.firstName)
        binding.etLastName.setText(a.lastName)
        binding.etBio.setText(a.bio)

        if (a.profilePhotoPath.isNotEmpty()) {
            binding.ivAvatar.visibility   = android.view.View.VISIBLE
            binding.tvInitials.visibility = android.view.View.GONE
            Glide.with(this)
                .load(a.profilePhotoPath)
                .placeholder(R.drawable.ic_account_placeholder)
                .circleCrop()
                .into(binding.ivAvatar)
        } else {
            binding.ivAvatar.visibility   = android.view.View.GONE
            binding.tvInitials.visibility = android.view.View.VISIBLE
            binding.tvInitials.text       = a.initials
        }
    }

    private fun toggleEditMode(editing: Boolean) {
        binding.viewMode.isVisible = !editing
        binding.editMode.isVisible = editing
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
