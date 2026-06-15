package com.telegramvault.ui.screens.auth

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.telegramvault.R
import com.telegramvault.databinding.FragmentSetPinBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetPinFragment : Fragment() {
    private var _binding: FragmentSetPinBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SetPinViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentSetPinBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupKeypad()
        observeState()
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
    }

    private fun setupKeypad() {
        listOf(binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        ).forEachIndexed { i, btn -> btn.setOnClickListener { viewModel.onDigit(i.toString()) } }
        binding.btnDelete.setOnClickListener { viewModel.onDelete() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateDots(state.enteredLength)
                    binding.tvInstruction.setText(
                        if (state.isConfirming) R.string.confirm_pin else R.string.enter_new_pin
                    )
                    binding.tvError.isVisible = state.error.isNotEmpty()
                    binding.tvError.text = state.error
                    if (state.done) findNavController().navigateUp()
                }
            }
        }
    }

    private fun updateDots(count: Int) {
        listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4,
            binding.dot5, binding.dot6).forEachIndexed { i, d -> d.isSelected = i < count }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
