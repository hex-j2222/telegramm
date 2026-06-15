package com.telegramvault.ui.screens.settings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.telegramvault.databinding.FragmentThemeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeFragment : Fragment() {
    private var _binding: FragmentThemeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentThemeBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnDark.setOnClickListener  { viewModel.setTheme("dark") }
        binding.btnLight.setOnClickListener { viewModel.setTheme("light") }
        binding.btnSystem.setOnClickListener{ viewModel.setTheme("system") }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
