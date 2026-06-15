package com.telegramvault.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telegramvault.databinding.ItemLanguageBinding
import com.telegramvault.utils.LocaleHelper

class LanguageAdapter(
    private val items: List<LocaleHelper.LanguageItem>,
    private val onClick: (LocaleHelper.LanguageItem) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.VH>() {

    inner class VH(private val b: ItemLanguageBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: LocaleHelper.LanguageItem) {
            b.tvNativeName.text  = item.nativeName
            b.tvEnglishName.text = item.englishName
            b.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemLanguageBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos])
    override fun getItemCount() = items.size
}
