package com.telegramvault.ui.screens.settings

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.telegramvault.R
import com.telegramvault.databinding.FragmentSettingsBinding
import com.telegramvault.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    // Prevent switch listeners from firing during initial state load
    private var isInitialLoad = true

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSettings()
        setupClickListeners()
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { s ->
                    isInitialLoad = true
                    binding.switchBiometric.isChecked      = s.biometricEnabled
                    binding.switchPattern.isChecked        = s.patternEnabled
                    binding.switchPin.isChecked            = s.pinEnabled
                    binding.switchNotifications.isChecked  = s.notificationsEnabled
                    binding.tvGoogleStatus.text = if (s.googleSignedIn)
                        getString(R.string.google_signed_in) else getString(R.string.google_not_signed_in)
                    binding.tvCurrentLanguage.text = s.language
                    binding.tvCurrentTheme.text    = s.theme
                    isInitialLoad = false
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { e ->
                    when (e) {
                        is SettingsEvent.RestartActivity -> restartActivity()
                        is SettingsEvent.ShowMessage     ->
                            Snackbar.make(binding.root, e.msg, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.switchBiometric.setOnCheckedChangeListener { _, checked ->
            if (!isInitialLoad) viewModel.setBiometric(checked)
        }
        binding.switchPattern.setOnCheckedChangeListener { _, checked ->
            if (isInitialLoad) return@setOnCheckedChangeListener
            if (checked) findNavController().navigate(R.id.action_settings_to_setPattern)
            else viewModel.disablePattern()
        }
        binding.switchPin.setOnCheckedChangeListener { _, checked ->
            if (isInitialLoad) return@setOnCheckedChangeListener
            if (checked) findNavController().navigate(R.id.action_settings_to_setPin)
            else viewModel.disablePin()
        }
        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            if (!isInitialLoad) viewModel.setNotifications(checked)
        }
        binding.btnLanguage.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_language)
        }
        binding.btnTheme.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_theme)
        }
        binding.btnGoogleDrive.setOnClickListener {
            viewModel.toggleGoogleDrive(requireActivity())
        }
        binding.btnAbout.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_about)
        }
    }

    private fun restartActivity() {
        requireActivity().run {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
