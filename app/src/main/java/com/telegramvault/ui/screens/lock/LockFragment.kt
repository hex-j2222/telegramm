package com.telegramvault.ui.screens.lock

import android.os.Bundle
import android.view.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.telegramvault.R
import com.telegramvault.databinding.FragmentLockBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LockFragment : Fragment() {
    private var _binding: FragmentLockBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentLockBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupKeypad()
        observeState()
        checkBiometric()
    }

    private fun setupKeypad() {
        val digits = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6, binding.btn7,
            binding.btn8, binding.btn9
        )
        digits.forEachIndexed { i, btn ->
            btn.setOnClickListener { viewModel.onDigit(i.toString()) }
        }
        binding.btnDelete.setOnClickListener { viewModel.onDelete() }
        binding.btnBiometric.setOnClickListener { showBiometricPrompt() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updatePinDots(state.enteredLength)
                    if (state.error.isNotEmpty()) {
                        binding.tvError.text = state.error
                        binding.tvError.isVisible = true
                        shakeView()
                    } else {
                        binding.tvError.isVisible = false
                    }
                    if (state.unlocked) {
                        findNavController().navigate(R.id.action_lock_to_home)
                    }
                }
            }
        }
    }

    private fun checkBiometric() {
        val manager = BiometricManager.from(requireContext())
        val canAuth = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
        binding.btnBiometric.isVisible = canAuth
        if (canAuth) showBiometricPrompt()
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                viewModel.onBiometricSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    binding.tvError.text = errString
                    binding.tvError.isVisible = true
                }
            }
            override fun onAuthenticationFailed() {}
        }
        val prompt = BiometricPrompt(this, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setNegativeButtonText(getString(R.string.use_pin))
            .build()
        prompt.authenticate(info)
    }

    private fun updatePinDots(count: Int) {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4,
            binding.dot5, binding.dot6)
        dots.forEachIndexed { i, dot ->
            dot.isSelected = i < count
        }
    }

    private fun shakeView() {
        binding.pinContainer.animate()
            .translationX(20f).setDuration(50)
            .withEndAction {
                binding.pinContainer.animate().translationX(-20f).setDuration(50)
                    .withEndAction { binding.pinContainer.animate().translationX(0f).setDuration(50).start() }
                    .start()
            }.start()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
