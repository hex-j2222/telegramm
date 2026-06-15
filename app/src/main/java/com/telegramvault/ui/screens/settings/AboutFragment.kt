package com.telegramvault.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.telegramvault.BuildConfig
import com.telegramvault.databinding.FragmentAboutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvVersion.text = getString(com.telegramvault.R.string.version, BuildConfig.VERSION_NAME)
        binding.btnPrivacy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.org/privacy")))
        }
        binding.btnLicenses.setOnClickListener {
            // Show OSS licenses
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
