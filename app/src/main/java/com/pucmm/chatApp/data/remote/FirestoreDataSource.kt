package com.pucmm.chatApp.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.pucmm.chatApp.data.model.Conversation
import com.pucmm.chatApp.data.model.Message
import com.pucmm.chatApp.data.model.User
import com.pucmm.chatApp.data.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeUsers(): Flow<List<User>> = callbackFlow {
        val registration = firestore.collection(Constants.USERS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(Exception(error.toFirestoreMessage("No se pudieron cargar los usuarios"), error))
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.map { document -> document.toUser() }.orEmpty()
                trySend(users)
            }
        awaitClose { registration.remove() }
    }

    fun observeConversations(currentUid: String): Flow<List<Conversation>> = callbackFlow {
        if (currentUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(Constants.CONVERSATIONS_COLLECTION)
            .whereArrayContains("participantIds", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(Exception(error.toFirestoreMessage("No se pudieron cargar las conversaciones"), error))
                    return@addSnapshotListener
                }
                val conversations = snapshot?.documents
                    ?.map { document -> document.toConversation() }
                    .orEmpty()
                trySend(conversations)
            }
        awaitClose { registration.remove() }
    }

    fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        if (conversationId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(Constants.CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .collection(Constants.MESSAGES_COLLECTION)
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(Exception(error.toFirestoreMessage("No se pudieron cargar los mensajes"), error))
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents
                    ?.map { document -> document.toMessage() }
                    .orEmpty()
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    suspend fun createConversation(currentUid: String, otherUid: String): Result<String> {
        if (currentUid.isBlank() || otherUid.isBlank() || currentUid == otherUid) {
            return Result.failure(Exception("No se pudo crear el chat"))
        }

        val conversationId = buildConversationId(currentUid, otherUid)
        return runCatching {
            val conversationRef = firestore.collection(Constants.CONVERSATIONS_COLLECTION)
                .document(conversationId)
            firestore.runTransaction { transaction ->
                val existing = transaction.get(conversationRef)
                if (!existing.exists()) {
                    transaction.set(
                        conversationRef,
                        mapOf(
                            "participantIds" to listOf(currentUid, otherUid).sorted(),
                            "lastMessage" to "",
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                }
            }.await()
            conversationId
        }.withFirestoreMessage("No se pudo crear el chat")
    }

    suspend fun sendMessage(conversationId: String, message: Message): Result<Unit> {
        val text = message.text.trim()
        if (conversationId.isBlank() || text.isEmpty()) {
            return Result.failure(Exception("El mensaje no puede estar vacío"))
        }

        return runCatching {
            val sentAt = System.currentTimeMillis()
            val conversationRef = firestore.collection(Constants.CONVERSATIONS_COLLECTION)
                .document(conversationId)
            val messageRef = conversationRef.collection(Constants.MESSAGES_COLLECTION).document()
            firestore.runBatch { batch ->
                batch.set(
                    messageRef,
                    mapOf(
                        "senderId" to message.senderId,
                        "senderName" to message.senderName,
                        "receiverId" to message.receiverId,
                        "text" to text,
                        "sentAt" to sentAt,
                        "isRead" to false
                    )
                )
                batch.update(
                    conversationRef,
                    mapOf(
                        "lastMessage" to text,
                        "updatedAt" to sentAt
                    )
                )
            }.await()
            Unit
        }.withFirestoreMessage("No se pudo enviar el mensaje")
    }

    private fun buildConversationId(firstUid: String, secondUid: String): String {
        return listOf(firstUid, secondUid).sorted().joinToString(separator = "_")
    }

    private fun DocumentSnapshot.toUser(): User {
        return User(
            uid = getString("uid") ?: id,
            email = getString("email").orEmpty(),
            displayName = getString("displayName").orEmpty(),
            photoUrl = getString("photoUrl"),
            createdAt = getLong("createdAt") ?: 0L
        )
    }

    private fun DocumentSnapshot.toConversation(): Conversation {
        val participantIds = (get("participantIds") as? List<*>)
            ?.mapNotNull { it as? String }
            .orEmpty()
        return Conversation(
            conversationId = id,
            participantIds = participantIds,
            lastMessage = getString("lastMessage").orEmpty(),
            updatedAt = getLong("updatedAt") ?: 0L
        )
    }

    private fun DocumentSnapshot.toMessage(): Message {
        return Message(
            id = id,
            senderId = getString("senderId").orEmpty(),
            senderName = getString("senderName").orEmpty(),
            receiverId = getString("receiverId").orEmpty(),
            text = getString("text").orEmpty(),
            imageUrl = getString("imageUrl"),
            sentAt = getLong("sentAt") ?: 0L,
            isRead = getBoolean("isRead") ?: false
        )
    }

    private fun <T> Result<T>.withFirestoreMessage(fallback: String): Result<T> {
        return fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Result.failure(Exception(error.toFirestoreMessage(fallback), error))
            }
        )
    }

    private fun Throwable.toFirestoreMessage(fallback: String): String {
        val code = (this as? FirebaseFirestoreException)?.code
            ?: (cause as? FirebaseFirestoreException)?.code
        return when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "No hay permiso para usar el chat. Revisa las reglas de Firestore."
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Revisa tu conexión a internet"
            else -> fallback
        }
    }
}
