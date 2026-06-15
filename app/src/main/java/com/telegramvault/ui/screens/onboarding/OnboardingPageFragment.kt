package com.telegramvault.ui.screens.onboarding

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.telegramvault.R
import com.telegramvault.databinding.FragmentOnboardingPageBinding

class OnboardingPageFragment : Fragment() {
    private var _binding: FragmentOnboardingPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentOnboardingPageBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val type    = arguments?.getString(ARG_TYPE) ?: "welcome"
        val titleR  = arguments?.getInt(ARG_TITLE) ?: R.string.onboarding_title_1
        val descR   = arguments?.getInt(ARG_DESC)  ?: R.string.onboarding_desc_1
        binding.tvTitle.setText(titleR)
        binding.tvDescription.setText(descR)
        binding.lottieAnim.setAnimation(when (type) {
            "security" -> R.raw.anim_security
            "api"      -> R.raw.anim_api
            else       -> R.raw.anim_welcome
        })
        binding.lottieAnim.playAnimation()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    companion object {
        private const val ARG_TYPE  = "type"
        private const val ARG_TITLE = "title"
        private const val ARG_DESC  = "desc"
        fun newInstance(page: OnboardingPage) = OnboardingPageFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TYPE, page.type)
                putInt(ARG_TITLE, page.titleRes)
                putInt(ARG_DESC, page.descRes)
            }
        }
    }
}
