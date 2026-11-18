package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.data.AuthRepository
import com.anchorguard.client.network.NetworkResult
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import kotlinx.coroutines.launch

@Composable
fun AuthLoginScreen(
    onSignedIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository(context) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "Sign in",
        subtitle = "Use your AnchorGuard parent account email and password."
    ) {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        AnchorPrimaryButton(
            text = if (busy) "Signing in…" else "Sign in",
            onClick = {
                if (busy) return@AnchorPrimaryButton
                error = null
                busy = true
                scope.launch {
                    when (val r = repo.login(email, password)) {
                        is NetworkResult.Success -> onSignedIn()
                        is NetworkResult.Failure -> error = r.error.userMessage()
                    }
                    busy = false
                }
            },
            enabled = !busy && email.contains("@") && password.length >= 8
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onCreateAccount) { Text("Need an account? Create one") }
        AnchorSecondaryButton(text = "Back", onClick = onBack)
    }
}
