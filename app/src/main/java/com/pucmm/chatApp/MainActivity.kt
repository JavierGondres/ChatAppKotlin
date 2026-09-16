package com.pucmm.chatApp

import android.os.Bundle
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import java.util.concurrent.TimeUnit


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

        verifyFirebaseConnection()
    }

    private fun verifyFirebaseConnection() {
        val firestore = FirebaseFirestore.getInstance()
        val task = firestore.collection("app_health")
            .document("startup_ping")
            .get(Source.SERVER)

        Tasks.withTimeout(task, 10, TimeUnit.SECONDS)
            .addOnSuccessListener {
                Log.i("FirebaseCheck", "Conexión OK. exists=${it.exists()}")
            }
            .addOnFailureListener {
                Log.e("FirebaseCheck", "No se pudo conectar a Firebase", it)
            }
    }
}