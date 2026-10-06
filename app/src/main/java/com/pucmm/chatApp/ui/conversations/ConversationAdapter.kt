package com.pucmm.chatApp.ui.conversations

import android.view.LayoutInflater
import com.pucmm.chatApp.R
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// El adapter detecta los clics.

class ConversationAdapter (
    private var conversations: List<ConversationUiModel>,
    private val onConversationClick: (ConversationUiModel) -> Unit)
    : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.userName.text = conversation.otherUserName
        holder.avatar.text = conversation.otherUserName.initials()
        holder.lastMessage.text = conversation.lastMessage
        holder.conversationTime.text = conversation.time
        holder.unreadCount.text = conversation.unreadCount.toString()

        holder.unreadBadge.visibility =
            if(conversation.unreadCount > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }

        holder.itemView.setOnClickListener {
            onConversationClick(conversation)
        }
    }

    fun updateConversations(newConversations: List<ConversationUiModel>) {
        conversations = newConversations
        notifyDataSetChanged()
    }

    class ConversationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val avatar: TextView = itemView.findViewById(R.id.textAvatar)
        val userName: TextView = itemView.findViewById(R.id.textUserName)
        val lastMessage: TextView = itemView.findViewById(R.id.textLastMessage)
        val conversationTime: TextView = itemView.findViewById(R.id.textConversationTime)
        val unreadCount: TextView = itemView.findViewById(R.id.textUnreadCount)
        val unreadBadge: View = itemView.findViewById(R.id.cardUnreadCount)
    }

    override fun getItemCount(): Int {
        return conversations.size
    }
}

private fun String.initials(): String {
    val parts = trim().split(" ").filter { part -> part.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}