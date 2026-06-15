package com.telegramvault.ui.screens.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.telegramvault.databinding.FragmentApiGuideBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ApiGuideFragment : Fragment() {
    private var _binding: FragmentApiGuideBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentApiGuideBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnOpenTelegram.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://my.telegram.org/apps")))
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
