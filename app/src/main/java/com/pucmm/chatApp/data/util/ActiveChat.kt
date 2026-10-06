package com.pucmm.chatApp.data.util

object ActiveChat {
    @Volatile
    var conversationId: String? = null
}
