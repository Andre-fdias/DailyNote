package com.andrefdias.dailynote.ui.screens.security

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import com.andrefdias.dailynote.ui.designsystem.spacing.FireSpacing
import com.andrefdias.dailynote.ui.designsystem.typography.FireTypography
import com.andrefdias.dailynote.ui.designsystem.icons.FireIcons
import com.andrefdias.dailynote.ui.designsystem.components.buttons.FireButton
import kotlinx.coroutines.delay

private const val LOG_TAG = "FireSecurity"
private fun logD(message: String) = android.util.Log.d(LOG_TAG, message)
private fun logE(message: String, throwable: Throwable? = null) =
    android.util.Log.e(LOG_TAG, message, throwable)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinLockScreen(
    correctPin: String,
    onUnlocked: () -> Unit,
    maxAttempts: Int = 3,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    var lockTimer by remember { mutableStateOf(0) }

    // Animação de erro (shake)
    val shakeAnim by animateFloatAsState(
        targetValue = if (showError) 12f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "shake"
    )

    // Timer de bloqueio temporário
    LaunchedEffect(isLocked) {
        if (isLocked) {
            lockTimer = 30
            while (lockTimer > 0) {
                delay(1000)
                lockTimer--
            }
            isLocked = false
            attempts = 0
            enteredPin = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FireColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FireSpacing.Medium),
            modifier = Modifier
                .fillMaxWidth()
                .padding(FireSpacing.Large)
                .offset(x = shakeAnim.dp)
        ) {
            // Ícone animado trancado/destrancado
            AnimatedContent(
                targetState = if (showError || isLocked) "🔒" else "🔓",
                transitionSpec = {
                    fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                },
                label = "lock_icon"
            ) { icon ->
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            if (showError || isLocked) FireColors.Error.copy(alpha = 0.1f) else FireColors.Primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 48.sp
                    )
                }
            }

            Text(
                text = if (isLocked) "🔒 DISPOSITIVO BLOQUEADO" else "FIRE NOTES SECURE",
                style = FireTypography.Headline,
                fontWeight = FontWeight.ExtraBold,
                color = if (isLocked) FireColors.Error else FireColors.Primary,
                fontSize = 20.sp,
                letterSpacing = 0.5.sp
            )

            if (isLocked) {
                Text(
                    text = "Acesso temporariamente suspenso.\nTente novamente em ${lockTimer}s",
                    style = FireTypography.Body,
                    color = FireColors.Error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Digite seu PIN de segurança de 4 dígitos para acessar o boletim operacional.",
                    style = FireTypography.BodySmall,
                    color = FireColors.OnSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = FireSpacing.Medium)
                )
            }

            // Indicador visual de progresso dos dígitos (4 pontos)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = FireSpacing.Medium)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                color = if (index < enteredPin.length) {
                                    if (showError) FireColors.Error else FireColors.Primary
                                } else {
                                    FireColors.OnSurfaceVariant.copy(alpha = 0.2f)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }

            // Numerical tactile keyboard layout (No text input to avoid keyboard overlap)
            Column(
                verticalArrangement = Arrangement.spacedBy(FireSpacing.Small),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = FireSpacing.Small)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "⌫")
                )

                keys.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(FireSpacing.Medium),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        row.forEach { key ->
                            KeypadButton(
                                text = key,
                                enabled = !isLocked,
                                onClick = {
                                    if (key == "⌫") {
                                        if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                    } else if (key == "C") {
                                        enteredPin = ""
                                    } else {
                                        if (enteredPin.length < 4) {
                                            enteredPin += key
                                            if (enteredPin.length == 4) {
                                                if (enteredPin == correctPin) {
                                                    logD("PIN correto. Acesso liberado.")
                                                    onUnlocked()
                                                } else {
                                                    logD("PIN incorreto.")
                                                    showError = true
                                                    attempts++
                                                    errorMessage = "PIN incorreto. Tentativa $attempts de $maxAttempts."
                                                    enteredPin = ""
                                                    if (attempts >= maxAttempts) {
                                                        isLocked = true
                                                        logE("PIN incorreto $maxAttempts vezes. Bloqueio temporário ativado.")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Mensagem de erro com animação
            AnimatedVisibility(
                visible = showError && !isLocked,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Text(
                    text = "⚠️ $errorMessage",
                    style = FireTypography.LabelSmall,
                    color = FireColors.Error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Badge de Segurança Ativa
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FireColors.Success.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, FireColors.Success.copy(alpha = 0.2f)),
                modifier = Modifier.padding(top = FireSpacing.Small)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(FireColors.Success, CircleShape)
                    )
                    Text(
                        text = "CRIPTOGRAFIA DE ARQUIVOS ATIVA",
                        fontSize = 9.sp,
                        color = FireColors.Success,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAction = text == "⌫" || text == "C"
    val containerBg = if (isAction) FireColors.Secondary.copy(alpha = 0.1f) else FireColors.SurfaceVariant.copy(alpha = 0.4f)
    val textCol = if (isAction) FireColors.Secondary else FireColors.OnSurface

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) containerBg else containerBg.copy(alpha = 0.5f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isAction) 16.sp else 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (enabled) textCol else textCol.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace
        )
    }
}
