package com.example

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainApp
import com.example.ui.MainViewModel
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.theme.EduBankTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            // Gate the root UI behind Firebase Authentication state
            var firebaseUser by remember { mutableStateOf(Firebase.auth.currentUser) }

            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { auth ->
                    firebaseUser = auth.currentUser
                }
                Firebase.auth.addAuthStateListener(listener)
                onDispose {
                    Firebase.auth.removeAuthStateListener(listener)
                }
            }

            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            EduBankTheme(darkTheme = isDarkTheme) {
                val currentUser = firebaseUser
                if (currentUser == null) {
                    // Unauthenticated: show Sign-In UI only. No feature ViewModels or Firestore queries are composed.
                    AuthScreen()
                } else {
                    // Authenticated: instantiate MainViewModel keyed on the user's UID to fetch their role from `users/{uid}`
                    val application = context.applicationContext as Application
                    val viewModel: MainViewModel = viewModel(
                        key = currentUser.uid,
                        factory = MainViewModel.Factory(
                            application = application,
                            currentUserId = currentUser.uid,
                            userEmail = currentUser.email ?: "",
                            userDisplayName = currentUser.displayName ?: ""
                        )
                    )

                    MainApp(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { isDarkTheme = it },
                        onSignOut = {
                            viewModel.clearSession()
                            Firebase.auth.signOut()
                            coroutineScope.launch {
                                try {
                                    CredentialManager.create(context)
                                        .clearCredentialState(ClearCredentialStateRequest())
                                } catch (e: Exception) {
                                    Log.w("Auth", "Failed to clear credential state on sign out", e)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
