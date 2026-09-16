package com.pucmm.chatApp.di

import com.pucmm.chatApp.data.remote.FirebaseAuthDataSource
import com.pucmm.chatApp.data.remote.FirestoreDataSource
import com.pucmm.chatApp.data.remote.StorageDataSource
import com.pucmm.chatApp.data.repository.AuthRepository
import com.pucmm.chatApp.data.repository.ChatRepository
import com.pucmm.chatApp.data.repository.StorageRepository

object AppModule {
    val authRepository: AuthRepository by lazy {
        AuthRepository(FirebaseAuthDataSource())
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(FirestoreDataSource())
    }

    val storageRepository: StorageRepository by lazy {
        StorageRepository(StorageDataSource())
    }
}
