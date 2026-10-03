package com.pucmm.chatApp.ui.conversations

import android.view.LayoutInflater
import com.pucmm.chatApp.R
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// El adapter detecta los clics.

class ConversationAdapter (
    private val conversations: List<ConversationUiModel>,
    private val onConversationClick: (ConversationUiModel) -> Unit) // Unit es como un void
    : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.userName.text = conversation.otherUserName
        holder.lastMessage.text = conversation.lastMessage
        holder.conversationTime.text = conversation.time
        holder.unreadCount.text = conversation.unreadCount.toString()

        // Preguntamos por el dato, y modificamos vista.
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

    class ConversationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
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