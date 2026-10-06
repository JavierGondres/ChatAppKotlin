package com.pucmm.chatApp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.pucmm.chatApp.R
import com.pucmm.chatApp.data.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private var messages: List<Message>,
    private val currentUserId: String,
    private val otherUserName: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val timeFormatter =
        SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_SENT) {
            R.layout.item_message_sent
        } else {
            R.layout.item_message_received
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text
        holder.messageText.visibility = if (message.text.isBlank()) View.GONE else View.VISIBLE
        holder.messageTextTime.text = timeFormatter.format(Date(message.sentAt))
        val senderName = message.senderName.ifBlank {
            if (message.senderId == currentUserId) "" else otherUserName
        }
        holder.messageSenderName?.text = senderName
        holder.messageSenderName?.visibility = if (senderName.isBlank()) View.GONE else View.VISIBLE
        val imageUrl = message.imageUrl
        if (imageUrl.isNullOrBlank()) {
            holder.messageImage?.visibility = View.GONE
            holder.messageImage?.setImageDrawable(null)
        } else {
            holder.messageImage?.visibility = View.VISIBLE
            holder.messageImage?.load(imageUrl)
        }
    }

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.textMessage)
        val messageTextTime: TextView = itemView.findViewById(R.id.textMessageTime)
        val messageSenderName: TextView? = itemView.findViewById(R.id.textSenderName)
        val messageImage: ImageView? = itemView.findViewById(R.id.imageMessage)
    }
}
