package com.telegramvault.ui.screens.addaccount

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.telegramvault.R
import com.telegramvault.data.model.AuthState
import com.telegramvault.databinding.FragmentAddAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddAccountFragment : Fragment() {

    private var _binding: FragmentAddAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddAccountViewModel by viewModels()

    enum class Step { PHONE, CODE, PASSWORD, REGISTER }
    private var currentStep = Step.PHONE

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAddAccountBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showStep(Step.PHONE)
        setupButtons()
        observeState()
        observeEvents()
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvLearnApiId.setOnClickListener {
            findNavController().navigate(R.id.action_addAccount_to_apiGuide)
        }
        binding.btnNext.setOnClickListener { handleNextClick() }
    }

    private fun handleNextClick() {
        when (currentStep) {
            Step.PHONE -> {
                val phone = binding.etPhone.text.toString().trim()
                if (phone.length < 7) { showError(getString(R.string.error_invalid_phone)); return }
                setLoading(true)
                viewModel.startAuth(phone)
            }
            Step.CODE -> {
                val code = binding.etCode.text.toString().trim()
                if (code.length < 4) { showError(getString(R.string.error_invalid_code)); return }
                setLoading(true)
                viewModel.submitCode(code)
            }
            Step.PASSWORD -> {
                val pwd = binding.etPassword.text.toString()
                if (pwd.isEmpty()) { showError(getString(R.string.error_empty_password)); return }
                setLoading(true)
                viewModel.submitPassword(pwd)
            }
            Step.REGISTER -> {
                val fn = binding.etFirstName.text.toString().trim()
                val ln = binding.etLastName.text.toString().trim()
                if (fn.isEmpty()) { showError("First name required"); return }
                setLoading(true)
                viewModel.submitRegistration(fn, ln)
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    setLoading(state is AuthState.Loading)
                    when (state) {
                        is AuthState.WaitingCode -> {
                            showStep(Step.CODE)
                            binding.tvCodeHint.text =
                                getString(R.string.code_sent_to, state.phone)
                        }
                        is AuthState.WaitingPassword -> {
                            binding.tvPasswordHint.text =
                                if (state.hint.isNotEmpty())
                                    getString(R.string.password_hint_label, state.hint)
                                else ""
                            showStep(Step.PASSWORD)
                        }
                        is AuthState.WaitingRegistration -> showStep(Step.REGISTER)
                        else -> {}
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is AddAccountEvent.AccountSaved ->
                            findNavController().navigate(R.id.action_addAccount_to_home)
                        is AddAccountEvent.ShowError -> {
                            setLoading(false)
                            showError(event.message)
                        }
                    }
                }
            }
        }
    }

    private fun showStep(step: Step) {
        currentStep = step
        binding.stepPhone.isVisible    = step == Step.PHONE
        binding.stepCode.isVisible     = step == Step.CODE
        binding.stepPassword.isVisible = step == Step.PASSWORD
        binding.stepRegister.isVisible = step == Step.REGISTER

        binding.tvStepIndicator.text = when (step) {
            Step.PHONE    -> getString(R.string.step_1_of_3)
            Step.CODE     -> getString(R.string.step_2_of_3)
            Step.PASSWORD -> getString(R.string.step_password)
            Step.REGISTER -> getString(R.string.step_3_of_3)
        }
        binding.btnNext.text = when (step) {
            Step.PHONE    -> getString(R.string.send_code)
            Step.CODE     -> getString(R.string.verify)
            Step.PASSWORD -> getString(R.string.next)
            Step.REGISTER -> getString(R.string.save)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.btnNext.isEnabled     = !loading
    }

    private fun showError(msg: String) =
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
