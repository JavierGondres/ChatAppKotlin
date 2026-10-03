package com.pucmm.chatApp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pucmm.chatApp.ui.auth.login.LoginActivity
import com.pucmm.chatApp.ui.conversations.ConversationsActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        testFirebaseConnection()

        startActivity(
            Intent(this, ConversationsActivity::class.java)
        )

        finish()
    }

    private fun testFirebaseConnection() {
        val firestore = FirebaseFirestore.getInstance()
        val payload = hashMapOf(
            "status" to "startup_ping",
            "device" to Build.MODEL,
            "packageName" to packageName,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("app_health")
            .document("startup_ping")
            .set(payload)
            .addOnSuccessListener {
                Log.i("FirebaseCheck", "Documento creado correctamente en app_health/startup_ping")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseCheck", "No se pudo crear el documento en Firebase", e)
            }
    }
}