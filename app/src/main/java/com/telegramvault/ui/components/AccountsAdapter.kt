package com.telegramvault.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.telegramvault.R
import com.telegramvault.data.model.TelegramAccount
import com.telegramvault.databinding.ItemAccountBinding

class AccountsAdapter(
    private val onAccountClick: (TelegramAccount) -> Unit,
    private val onDeleteClick: (TelegramAccount) -> Unit
) : ListAdapter<TelegramAccount, AccountsAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemAccountBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(account: TelegramAccount) {
            b.tvName.text    = account.displayName
            b.tvPhone.text   = account.phoneNumber
            b.tvUsername.text = if (account.username.isNotEmpty()) "@${account.username}" else ""
            b.tvInitials.text = account.initials

            if (account.profilePhotoPath.isNotEmpty()) {
                Glide.with(b.ivAvatar)
                    .load(account.profilePhotoPath)
                    .placeholder(R.drawable.ic_account_placeholder)
                    .circleCrop()
                    .into(b.ivAvatar)
                b.tvInitials.visibility = android.view.View.GONE
                b.ivAvatar.visibility   = android.view.View.VISIBLE
            } else {
                b.ivAvatar.visibility   = android.view.View.GONE
                b.tvInitials.visibility = android.view.View.VISIBLE
            }

            b.root.setOnClickListener { onAccountClick(account) }
            b.btnDelete.setOnClickListener { onDeleteClick(account) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<TelegramAccount>() {
            override fun areItemsTheSame(o: TelegramAccount, n: TelegramAccount) = o.id == n.id
            override fun areContentsTheSame(o: TelegramAccount, n: TelegramAccount) = o == n
        }
    }
}
