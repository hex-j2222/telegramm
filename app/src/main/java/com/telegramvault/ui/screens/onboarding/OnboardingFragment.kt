package com.telegramvault.ui.screens.onboarding

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.telegramvault.R
import com.telegramvault.data.local.prefs.AppPreferences
import com.telegramvault.databinding.FragmentOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var prefs: AppPreferences

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentOnboardingBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pages = listOf(
            OnboardingPage("welcome", R.string.onboarding_title_1, R.string.onboarding_desc_1),
            OnboardingPage("security", R.string.onboarding_title_2, R.string.onboarding_desc_2),
            OnboardingPage("api",      R.string.onboarding_title_3, R.string.onboarding_desc_3)
        )
        binding.viewPager.adapter = OnboardingAdapter(this, pages)
        TabLayoutMediator(binding.tabIndicator, binding.viewPager) { _, _ -> }.attach()

        binding.btnGetStarted.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch { prefs.setFirstLaunch(false) }
            findNavController().navigate(R.id.action_onboarding_to_home)
        }
        binding.tvSkip.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch { prefs.setFirstLaunch(false) }
            findNavController().navigate(R.id.action_onboarding_to_home)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

data class OnboardingPage(val type: String, val titleRes: Int, val descRes: Int)

class OnboardingAdapter(fragment: Fragment, private val pages: List<OnboardingPage>) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount() = pages.size
    override fun createFragment(position: Int) = OnboardingPageFragment.newInstance(pages[position])
}
