package com.telegramvault.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.telegramvault.R
import com.telegramvault.TelegramVaultApp
import com.telegramvault.data.local.prefs.AppPreferences
import com.telegramvault.databinding.ActivityMainBinding
import com.telegramvault.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var prefs: AppPreferences
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun attachBaseContext(newBase: Context) {
        // Apply saved language before activity inflates
        val lang = runBlocking {
            newBase.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
                .getString("language", "system") ?: "system"
        }
        val ctx = if (lang == "system") newBase else LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Apply theme from saved preference
        lifecycleScope.launch {
            val theme = prefs.theme.first()
            TelegramVaultApp.instance.applyTheme(theme)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        lifecycleScope.launch { determineStartDestination() }
    }

    private fun setupNavigation() {
        val host = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = host.navController
    }

    private suspend fun determineStartDestination() {
        val isFirst    = prefs.firstLaunch.first()
        val lockEnabled = prefs.lockEnabled.first()
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(
            when {
                isFirst     -> R.id.onboardingFragment
                lockEnabled -> R.id.lockFragment
                else        -> R.id.homeFragment
            }
        )
        navController.graph = graph
    }
}
