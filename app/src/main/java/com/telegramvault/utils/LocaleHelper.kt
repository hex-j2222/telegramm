package com.telegramvault.utils

import android.content.Context
import android.os.Build
import java.util.Locale

object LocaleHelper {

    fun onAttach(context: Context): Context {
        val prefs = context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "system") ?: "system"
        return if (lang == "system") context else setLocale(context, lang)
    }

    fun setLocale(context: Context, language: String): Context {
        saveLanguage(context, language)
        return updateResources(context, language)
    }

    fun saveLanguage(context: Context, language: String) {
        context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
            .edit().putString("language", language).apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = parseLocale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(android.os.LocaleList(locale))
        }
        return context.createConfigurationContext(config)
    }

    private fun parseLocale(language: String): Locale = when {
        language.contains("-") -> {
            val parts = language.split("-")
            Locale(parts[0], parts.getOrElse(1) { "" })
        }
        else -> Locale(language)
    }

    fun getSupportedLanguages(): List<LanguageItem> = listOf(
        LanguageItem("system", "System Default", "افتراضي"),
        LanguageItem("en",     "English",        "English"),
        LanguageItem("ar",     "العربية",        "Arabic"),
        LanguageItem("ru",     "Русский",        "Russian"),
        LanguageItem("de",     "Deutsch",        "German"),
        LanguageItem("fr",     "Français",       "French"),
        LanguageItem("es",     "Español",        "Spanish"),
        LanguageItem("zh",     "中文",            "Chinese"),
        LanguageItem("tr",     "Türkçe",         "Turkish"),
        LanguageItem("fa",     "فارسی",          "Persian"),
        LanguageItem("hi",     "हिन्दी",           "Hindi"),
        LanguageItem("pt",     "Português",      "Portuguese"),
        LanguageItem("it",     "Italiano",       "Italian"),
        LanguageItem("ja",     "日本語",           "Japanese"),
        LanguageItem("ko",     "한국어",           "Korean")
    )

    data class LanguageItem(val code: String, val nativeName: String, val englishName: String)
}
