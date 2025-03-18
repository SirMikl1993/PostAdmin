package com.example.postadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.postadmin.ui.screens.LoginScreen
import com.example.postadmin.ui.screens.MainScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                LoginScreen(onLoginSuccess = { MainScreen() })
            } else {
                MainScreen()
            }
        }
    }
}