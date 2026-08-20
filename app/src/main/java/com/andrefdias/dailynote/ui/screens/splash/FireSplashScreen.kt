package com.andrefdias.dailynote.ui.screens.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrefdias.dailynote.ui.designsystem.colors.FireColors
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

// ============================================
// SPLASH SCREEN PRINCIPAL
// ============================================

@Composable
fun FireSplashScreen(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationState by remember { mutableStateOf(SplashAnimationState.START) }
    var progress by remember { mutableStateOf(0f) }

    // Animações infinitas de chamas e rotação
    val infiniteTransition = rememberInfiniteTransition(label = "fire")
    val flameHeight by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Simulação do progresso de carregamento da mangueira
    LaunchedEffect(Unit) {
        animationState = SplashAnimationState.LOADING
        while (progress < 1f) {
            delay(50)
            progress += 0.02f
        }
        delay(300)
        animationState = SplashAnimationState.COMPLETE
        delay(500)
        onAnimationComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        // Efeito de fogo em chamas no fundo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFireBackground()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ============================================
            // LOGO COM ANIMAÇÃO
            // ============================================
            AnimatedContent(
                targetState = animationState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(800)) + 
                    scaleIn(initialScale = 0.5f, animationSpec = tween(800)) togetherWith
                    fadeOut(animationSpec = tween(500))
                },
                label = "logo_animation"
            ) { state ->
                when (state) {
                    SplashAnimationState.START -> {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .rotate(rotation * 0.5f)
                                .scale(0.8f)
                        ) {
                            FireLogo()
                        }
                    }
                    SplashAnimationState.LOADING -> {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .rotate(rotation * 0.3f)
                        ) {
                            // Efeito de brilho de calor (glow) ao redor
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1f + flameHeight * 0.1f)
                                    .alpha(0.5f + flameHeight * 0.3f)
                            ) {
                                FireGlowEffect()
                            }
                            
                            // Logo interno
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1f + flameHeight * 0.05f)
                            ) {
                                FireLogo()
                            }
                        }
                    }
                    SplashAnimationState.COMPLETE -> {
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .scale(1.2f)
                        ) {
                            FireLogo()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ============================================
            // TEXTO DE IDENTIDADE
            // ============================================
            AnimatedContent(
                targetState = animationState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) + 
                    slideInVertically(initialOffsetY = { 30 }) togetherWith
                    fadeOut(animationSpec = tween(300))
                },
                label = "text_animation"
            ) { state ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔥 FIRE NOTES",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "SISTEMA DE OCORRÊNCIAS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 4.sp
                    )
                    
                    if (state == SplashAnimationState.COMPLETE) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + scaleIn()
                        ) {
                            Text(
                                text = "🚒 CORPO DE BOMBEIROS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FireColors.Primary,
                                letterSpacing = 6.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // ============================================
            // BARRA DE PROGRESSO ESTILO MANGUEIRA
            // ============================================
            AnimatedContent(
                targetState = animationState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) + 
                    slideInVertically(initialOffsetY = { 50 }) togetherWith
                    fadeOut(animationSpec = tween(300))
                },
                label = "progress_animation"
            ) { state ->
                if (state != SplashAnimationState.COMPLETE) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mangueira de preenchimento
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(8.dp)
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFF6B35),
                                                FireColors.Primary,
                                                Color(0xFF4CAF50)
                                            )
                                        ),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                            
                            // Reflexo de brilho sobre a mangueira
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.0f),
                                                Color.White.copy(alpha = 0.3f),
                                                Color.White.copy(alpha = 0.0f)
                                            )
                                        ),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }

                        // Porcentagem textual
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )

                        // Mensagens operacionais dinâmicas alternantes
                        val messages = listOf(
                            "🚒 Preparando equipamentos...",
                            "🔥 Aquecendo motores...",
                            "🚨 Verificando ocorrências...",
                            "📡 Conectando ao sistema..."
                        )
                        
                        val messageIndex = (progress * messages.size).toInt().coerceIn(0, messages.size - 1)
                        
                        AnimatedContent(
                            targetState = messageIndex,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(400)) + 
                                slideInVertically(initialOffsetY = { 20 }) togetherWith
                                fadeOut(animationSpec = tween(200))
                            },
                            label = "message_animation"
                        ) { index ->
                            Text(
                                text = messages[index],
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Estado de Pronto concluído
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn() + fadeIn()
                    ) {
                        Text(
                            text = "✅ PRONTO!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FireColors.Success,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Versão e copyright
            Text(
                text = "v2.0 • © 2026",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.3f),
                letterSpacing = 1.sp
            )
        }

        // Partículas brilhantes
        FireParticles(animationState)
    }
}

// ============================================
// ESTADOS DA ANIMAÇÃO
// ============================================

enum class SplashAnimationState {
    START,
    LOADING,
    COMPLETE
}

// ============================================
// COMPONENTE DO LOGO
// ============================================

@Composable
private fun FireLogo() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val size = size.minDimension
            
            // Círculos concêntricos de calor do escudo
            drawCircle(
                color = FireColors.Primary.copy(alpha = 0.15f),
                radius = size * 0.45f,
                center = center
            )
            
            drawCircle(
                color = FireColors.Primary.copy(alpha = 0.08f),
                radius = size * 0.55f,
                center = center
            )
        }
        
        // Ícone Central
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FireColors.Primary.copy(alpha = 0.3f),
                            FireColors.Primary.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🔥",
                    fontSize = 48.sp
                )
                Text(
                    text = "🚒",
                    fontSize = 32.sp,
                    modifier = Modifier.offset(y = (-8).dp)
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FireColors.Primary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 0.5f
                    )
                )
        )
    }
}

// ============================================
// GLOW DE CALOR
// ============================================

@Composable
private fun FireGlowEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val size = size.minDimension
        val center = Offset(size / 2f, size / 2f)
        val radius = size * 0.6f

        drawCircle(
            color = FireColors.Primary.copy(alpha = glowAlpha * 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 4f)
        )
        
        drawCircle(
            color = Color(0xFFFF6B35).copy(alpha = glowAlpha * 0.15f),
            radius = radius * 1.2f,
            center = center,
            style = Stroke(width = 2f)
        )
    }
}

// ============================================
// CHAMAS DE FUNDO
// ============================================

private fun DrawScope.drawFireBackground() {
    val centerX = size.width / 2
    val centerY = size.height / 2
    
    for (i in 0..8) {
        val angle = (i * 40f + System.currentTimeMillis() / 2000f) % 360f
        val radius = (300f + 100f * sin(System.currentTimeMillis() / 1000f + i * 0.5f))
        val x = centerX + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
        val y = centerY + 300 + radius * 0.3f * sin(Math.toRadians(angle.toDouble())).toFloat()
        
        val alpha = 0.03f + 0.02f * sin(System.currentTimeMillis() / 1500f + i.toFloat())
        val color = when (i % 3) {
            0 -> Color(0xFFFF6B35)
            1 -> FireColors.Primary
            else -> Color(0xFF4CAF50)
        }
        
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = 20f + 15f * sin(System.currentTimeMillis() / 1000f + i.toFloat()).absoluteValue,
            center = Offset(x, y)
        )
    }
}

// ============================================
// PARTÍCULAS EM SUSPENSÃO
// ============================================

@Composable
private fun FireParticles(state: SplashAnimationState) {
    val particleCount = if (state == SplashAnimationState.LOADING) 30 else 0
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val time = System.currentTimeMillis() / 1000f
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        repeat(particleCount) { i ->
            val angle = (i * 12f + time * 20f) % 360f
            val distance = 150f + 100f * sin(time * 0.5f + i.toFloat()).absoluteValue
            val x = centerX + distance * cos(Math.toRadians(angle.toDouble())).toFloat()
            val y = centerY + distance * 0.3f * sin(Math.toRadians(angle.toDouble())).toFloat() - 100f
            
            val alpha = 0.1f + 0.2f * sin(time + i.toFloat()).absoluteValue
            val size = 3f + 5f * sin(time * 0.7f + i.toFloat()).absoluteValue
            
            drawCircle(
                color = FireColors.Primary.copy(alpha = alpha),
                radius = size,
                center = Offset(x, y)
            )
        }
    }
}
