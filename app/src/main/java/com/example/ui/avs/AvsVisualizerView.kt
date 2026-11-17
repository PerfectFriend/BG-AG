package com.example.ui.avs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Аппаратно-ускоренный Compose Canvas AVS визуализатор со спектроанализатором и жестами.
 */
@Composable
fun AvsVisualizerView(
    audioSessionId: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    initialPresetIndex: Int = 0,
    onToggleFullscreen: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val controller = remember { AudioVisualizerController(context) }

    var currentPresetIndex by remember { mutableIntStateOf(initialPresetIndex) }
    var showHud by remember { mutableStateOf(false) }
    var hudHideTimer by remember { mutableLongStateOf(0L) }

    // Подключение к аудио-сессии MediaPlayer
    LaunchedEffect(audioSessionId) {
        controller.attachSession(audioSessionId)
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.releaseVisualizer()
        }
    }

    // Автоматическое скрытие HUD через 3 секунды
    LaunchedEffect(hudHideTimer) {
        if (showHud) {
            delay(3000)
            showHud = false
        }
    }

    // 60 FPS Цикл обновления кадра
    var frameTimeNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastNanos = nanos
                controller.tick(isPlaying, dt)
                frameTimeNanos = nanos
            }
        }
    }

    val preset = AvsPresetsPack.presets[currentPresetIndex.coerceIn(0, AvsPresetsPack.presets.size - 1)]

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF06090E))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showHud = !showHud
                        hudHideTimer = System.currentTimeMillis()
                    },
                    onDoubleTap = {
                        onToggleFullscreen?.invoke()
                    }
                )
            }
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag > 80f) {
                            // Swipe Right -> Previous Preset
                            currentPresetIndex = if (currentPresetIndex > 0) currentPresetIndex - 1 else AvsPresetsPack.presets.size - 1
                            showHud = true
                            hudHideTimer = System.currentTimeMillis()
                        } else if (totalDrag < -80f) {
                            // Swipe Left -> Next Preset
                            currentPresetIndex = (currentPresetIndex + 1) % AvsPresetsPack.presets.size
                            showHud = true
                            hudHideTimer = System.currentTimeMillis()
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                )
            }
    ) {
        // Рендеринг AVS Пресета на Compose Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ctx = AvsFrameContext(
                width = size.width,
                height = size.height,
                fft = controller.fftBands,
                waveform = controller.waveform,
                bass = controller.bassEnergy,
                mid = controller.midEnergy,
                treble = controller.trebleEnergy,
                isBeat = controller.isBeat,
                time = frameTimeNanos / 1_000_000_000f,
                phase = (frameTimeNanos / 100_000_000f) % (2f * Math.PI.toFloat())
            )

            // Заливка полупрозрачным фоном для следа (Trails / Motion Blur)
            drawRect(
                color = Color(0xFF06090E).copy(alpha = 0.85f),
                size = size
            )

            // Вызов отрисовки пресета
            with(preset) {
                render(ctx)
            }

            // Beat Flash вспышка по периметру
            if (controller.isBeat) {
                drawRect(
                    color = Color(0x2200FFFF),
                    size = size
                )
            }
        }

        // HUD Overlay (название, категория, кнопки переключения и Fullscreen)
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .padding(8.dp)
            ) {
                // Верхняя плашка с названием
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(Color(0xCC0A1118), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AVS: ${preset.name}",
                        color = Color(0xFF00FFCC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${preset.category} | ${preset.author}",
                        color = Color(0xFFAAAAAA),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Кнопка Fullscreen в правом верхнем углу
                if (onToggleFullscreen != null) {
                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .background(Color(0xCC00E5FF), RoundedCornerShape(4.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.Black
                        )
                    }
                }

                // Кнопки Назад / Вперёд по центру по бокам
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            currentPresetIndex = if (currentPresetIndex > 0) currentPresetIndex - 1 else AvsPresetsPack.presets.size - 1
                            hudHideTimer = System.currentTimeMillis()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NavigateBefore,
                            contentDescription = "Prev Preset",
                            tint = Color(0xFF00FFCC)
                        )
                    }

                    IconButton(
                        onClick = {
                            currentPresetIndex = (currentPresetIndex + 1) % AvsPresetsPack.presets.size
                            hudHideTimer = System.currentTimeMillis()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NavigateNext,
                            contentDescription = "Next Preset",
                            tint = Color(0xFF00FFCC)
                        )
                    }
                }
            }
        }
    }
}
