package com.andrefdias.dailynote.ui.screens.auth

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import com.andrefdias.dailynote.BuildConfig

@Composable
fun AuthScreen(
    activity: FragmentActivity,
    pinEnabled: Boolean,
    biometricEnabled: Boolean,
    savedPin: String,
    onAuthenticated: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val showBiometricPrompt = {
        val biometricManager = androidx.biometric.BiometricManager.from(activity)
        if (biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        errorMessage = "Erro: $errString"
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onAuthenticated()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        errorMessage = "Biometria não reconhecida."
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acesso Operacional")
                .setSubtitle("Confirme sua identidade")
                .setNegativeButtonText(if (pinEnabled) "Usar PIN" else "Cancelar")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            errorMessage = "Biometria indisponível neste dispositivo."
        }
    }

    LaunchedEffect(Unit) {
        if (biometricEnabled) {
            showBiometricPrompt()
        }
    }

    // Shake animation for error
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            offsetX.animateTo(15f, animationSpec = tween(50))
            offsetX.animateTo(-15f, animationSpec = tween(50))
            offsetX.animateTo(15f, animationSpec = tween(50))
            offsetX.animateTo(0f, animationSpec = tween(50))
            delay(800)
            isError = false
            pinInput = ""
            errorMessage = ""
        }
    }

    val brush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PulsingFireIcon()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Acesso Restrito",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Área operacional do Daily Notes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (pinEnabled) {
                Box(modifier = Modifier.offset(x = offsetX.value.dp)) {
                    PinDots(pinInput = pinInput, isError = isError)
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(32.dp)) // Maintain height when no error
                }

                Numpad(
                    onNumberClick = { num ->
                        if (pinInput.length < 4 && !isError) {
                            pinInput += num
                            errorMessage = ""
                            if (pinInput.length == 4) {
                                if (pinInput == savedPin) {
                                    onAuthenticated()
                                } else {
                                    isError = true
                                    errorMessage = "PIN Incorreto"
                                }
                            }
                        }
                    },
                    onBackspace = {
                        if (pinInput.isNotEmpty() && !isError) {
                            pinInput = pinInput.dropLast(1)
                            errorMessage = ""
                        }
                    },
                    onBiometric = { showBiometricPrompt() },
                    showBiometric = biometricEnabled
                )
            } else if (biometricEnabled) {
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = { showBiometricPrompt() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint, 
                        contentDescription = null, 
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text("Desbloquear com Biometria", fontSize = 16.sp)
                }
            }
        }
        
        Text(
            text = "Versão: ${BuildConfig.VERSION_NAME} - Publicado em: ${BuildConfig.BUILD_TIME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@Composable
fun PulsingFireIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val primaryColor = MaterialTheme.colorScheme.primary
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .background(primaryColor.copy(alpha = alpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .scale(scale * 0.8f)
                .background(primaryColor.copy(alpha = alpha + 0.2f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Bombeiro",
                tint = primaryColor,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun PinDots(pinInput: String, maxLen: Int = 4, isError: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        for (i in 0 until maxLen) {
            val isFilled = i < pinInput.length
            val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (isFilled) color else Color.Transparent)
                    .border(2.dp, color, CircleShape)
            )
        }
    }
}

@Composable
fun Numpad(
    onNumberClick: (Int) -> Unit,
    onBackspace: () -> Unit,
    onBiometric: () -> Unit,
    showBiometric: Boolean
) {
    val numbers = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9)
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        numbers.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { num ->
                    NumpadButton(text = num.toString(), onClick = { onNumberClick(num) })
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBiometric) {
                NumpadIconButton(icon = Icons.Default.Fingerprint, onClick = onBiometric)
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }
            
            NumpadButton(text = "0", onClick = { onNumberClick(0) })
            
            NumpadIconButton(icon = Icons.Default.Backspace, onClick = onBackspace)
        }
    }
}

@Composable
fun NumpadButton(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable { onClick() }
    ) {
        Text(
            text = text, 
            fontSize = 32.sp, 
            fontWeight = FontWeight.Medium, 
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun NumpadIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            modifier = Modifier.size(36.dp), 
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
