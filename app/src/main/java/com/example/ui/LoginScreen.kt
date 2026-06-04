package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBypassLogin: () -> Unit,
    viewModel: com.example.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val coroutineScope = rememberCoroutineScope()
    val authState by viewModel.authState.collectAsState()
    
    val mainBgColor = Color(0xFFFDFBFF)
    val primaryBtnColor = Color(0xFF0061A4)
    val onPrimaryBtn = Color(0xFFFFFFFF)

    LaunchedEffect(authState) {
        if (authState is com.example.viewmodel.AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainBgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1E4FF)),
                contentAlignment = Alignment.Center
            ) {
                Text("M", fontWeight = FontWeight.Bold, fontSize = 48.sp, color = Color(0xFF001D36))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Bütçem'e Hoş Geldiniz",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001D36),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Hesaplarınızı yönetin, harcamalarınızı analiz edin ve hedeflerinize ulaşmak için tasarruf edin.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))

            if (authState is com.example.viewmodel.AuthState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDD5)),
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEA580C))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            (authState as com.example.viewmodel.AuthState.Error).message,
                            color = Color(0xFF9A3412),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        activity?.let { viewModel.signInWithGoogle(it) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBtnColor, contentColor = onPrimaryBtn),
                shape = RoundedCornerShape(28.dp),
                enabled = authState !is com.example.viewmodel.AuthState.Loading
            ) {
                if (authState is com.example.viewmodel.AuthState.Loading) {
                    CircularProgressIndicator(color = onPrimaryBtn, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google ile Giriş Yap", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onBypassLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Modu (Çevrimdışı Devam Et)", color = Color(0xFF64748B))
            }
        }
    }
}
