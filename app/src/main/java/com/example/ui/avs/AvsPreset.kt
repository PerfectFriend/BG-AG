package com.example.ui.avs

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.*

/**
 * Контекст кадра AVS для отрисовки пресетов
 */
data class AvsFrameContext(
    val width: Float,
    val height: Float,
    val fft: FloatArray,
    val waveform: FloatArray,
    val bass: Float,
    val mid: Float,
    val treble: Float,
    val isBeat: Boolean,
    val time: Float,
    val phase: Float
)

/**
 * Описание AVS пресета
 */
data class AvsPreset(
    val id: String,
    val name: String,
    val category: String,
    val author: String,
    val description: String,
    val render: DrawScope.(AvsFrameContext) -> Unit
)

/**
 * Банк классических и демо-сцен пресетов Winamp AVS для мобильной версии
 */
object AvsPresetsPack {

    val presets: List<AvsPreset> = listOf(
        // 1. Classic Cyan Spectrum
        AvsPreset(
            id = "classic_cyan_spectrum",
            name = "Classic Cyan Spectrum",
            category = "Winamp Classic",
            author = "Nullsoft / Zhenya",
            description = "Аутентичный спектральный эквалайзер с градиентными столбиками и точечными пиками",
            render = { ctx ->
                val barCount = 32
                val barWidth = ctx.width / barCount
                val gap = barWidth * 0.25f
                val w = barWidth - gap
                val maxH = ctx.height * 0.85f
                val baseY = ctx.height * 0.92f

                for (i in 0 until barCount) {
                    val fftIdx = (i * ctx.fft.size / barCount).coerceIn(0, ctx.fft.size - 1)
                    val amp = ctx.fft[fftIdx].coerceIn(0.04f, 1.0f)
                    val h = maxH * amp
                    val x = i * barWidth + gap / 2f
                    val topY = baseY - h

                    // Градиент столбика
                    val brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00FFCC),
                            Color(0xFF0099FF),
                            Color(0xFF002266)
                        ),
                        startY = topY,
                        endY = baseY
                    )

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(x, topY),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(3f, 3f)
                    )

                    // Пиковая точка
                    val peakY = (topY - 6f).coerceAtLeast(ctx.height * 0.05f)
                    drawCircle(
                        color = if (amp > 0.7f) Color(0xFFFFCC00) else Color(0xFF00FFFF),
                        radius = w * 0.4f,
                        center = Offset(x + w / 2f, peakY)
                    )
                }
            }
        ),

        // 2. Neon SuperScope
        AvsPreset(
            id = "neon_superscope",
            name = "Neon SuperScope",
            category = "Oscilloscope",
            author = "Lissajous / AVS",
            description = "Люминофорный зелёный осциллограф с гармоническими фигурами Лиссажу",
            render = { ctx ->
                val cx = ctx.width / 2f
                val cy = ctx.height / 2f
                val radius = min(cx, cy) * (0.65f + ctx.bass * 0.25f)

                val path = Path()
                val points = 120
                for (i in 0..points) {
                    val t = (i.toFloat() / points) * 2f * PI.toFloat()
                    val waveIdx = (i * ctx.waveform.size / points).coerceIn(0, ctx.waveform.size - 1)
                    val wave = ctx.waveform[waveIdx]

                    val r = radius * (1f + wave * 0.35f)
                    val x = cx + r * cos(t + ctx.phase) * sin(2f * t + ctx.phase * 0.5f)
                    val y = cy + r * sin(t + ctx.phase) * cos(3f * t + ctx.phase * 0.3f)

                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                // Эффект свечения фосфора
                drawPath(
                    path = path,
                    color = Color(0x3300FF66),
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
                drawPath(
                    path = path,
                    color = Color(0xFF00FF66),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            }
        ),

        // 3. Fire Plasma Ring
        AvsPreset(
            id = "fire_plasma_ring",
            name = "Fire Plasma Ring",
            category = "Plasma / Fire",
            author = "Justin Frankel",
            description = "Пылающее плазменное кольцо с протуберанцами, пульсирующими под ударные баса",
            render = { ctx ->
                val cx = ctx.width / 2f
                val cy = ctx.height / 2f
                val baseR = min(cx, cy) * 0.45f

                val count = 90
                val path = Path()
                for (i in 0..count) {
                    val angle = (i.toFloat() / count) * 2f * PI.toFloat()
                    val fftIdx = (abs(sin(angle)) * (ctx.fft.size - 1)).toInt().coerceIn(0, ctx.fft.size - 1)
                    val amp = ctx.fft[fftIdx]

                    val flare = sin(angle * 7f + ctx.phase * 4f) * 20f + amp * 90f * (1f + ctx.bass)
                    val r = baseR + flare
                    val x = cx + r * cos(angle)
                    val y = cy + r * sin(angle)

                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()

                // Огненное свечение
                val fireBrush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFEE00),
                        Color(0xFFFF3300),
                        Color(0x99990000),
                        Color(0x00000000)
                    ),
                    center = Offset(cx, cy),
                    radius = baseR * (1.6f + ctx.bass * 0.4f)
                )

                drawPath(path = path, brush = fireBrush)
                drawPath(
                    path = path,
                    color = Color(0xFFFFCC00),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
        ),

        // 4. Cyberpunk Highway
        AvsPreset(
            id = "cyberpunk_highway",
            name = "Cyberpunk Highway",
            category = "3D Wireframe",
            author = "Synthwave 80s",
            description = "Бесконечная неоновая кибер-магистраль с уходящей в горизонт сеткой и солнцем",
            render = { ctx ->
                val horizonY = ctx.height * 0.45f
                val cx = ctx.width / 2f

                // Заходящее неоновое солнце
                val sunR = min(ctx.width, ctx.height) * 0.22f
                drawCircle(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFF007F), Color(0xFFFF7700), Color(0xFFFFCC00)),
                        startY = horizonY - sunR * 1.5f,
                        endY = horizonY
                    ),
                    radius = sunR * (1f + ctx.bass * 0.15f),
                    center = Offset(cx, horizonY - sunR * 0.3f)
                )

                // Горизонтальные линии дороги, бегущие на зрителя
                val linesCount = 14
                for (i in 1..linesCount) {
                    val progress = ((i.toFloat() + (ctx.phase * 2f) % 1f) / linesCount).pow(2.2f)
                    val y = horizonY + (ctx.height - horizonY) * progress
                    val lineAlpha = (progress * 0.9f).coerceIn(0.1f, 1f)

                    val w = ctx.width * progress
                    val x1 = cx - w / 2f
                    val x2 = cx + w / 2f

                    drawLine(
                        color = Color(0xFF00F0FF).copy(alpha = lineAlpha),
                        start = Offset(x1, y),
                        end = Offset(x2, y),
                        strokeWidth = (1f + progress * 3f)
                    )
                }

                // Перспективные радиальные лучи сетки
                val rays = 10
                for (i in -rays..rays) {
                    val bottomX = cx + (i.toFloat() / rays) * (ctx.width * 0.7f)
                    drawLine(
                        color = Color(0xFFD000FF).copy(alpha = 0.75f),
                        start = Offset(cx + i * 2f, horizonY),
                        end = Offset(bottomX, ctx.height),
                        strokeWidth = 2f
                    )
                }
            }
        ),

        // 5. Sacred Spirograph
        AvsPreset(
            id = "sacred_spirograph",
            name = "Sacred Spirograph",
            category = "Sacred Geometry",
            author = "Mandala Mind",
            description = "Сакральная геометрия и гипнотический спирограф, реагирующий на гармоники",
            render = { ctx ->
                val cx = ctx.width / 2f
                val cy = ctx.height / 2f
                val maxR = min(cx, cy) * 0.8f

                val petals = 8
                val points = 240
                val path = Path()

                for (i in 0..points) {
                    val theta = (i.toFloat() / points) * 2f * PI.toFloat()
                    val fftIdx = (i % ctx.fft.size)
                    val mod = 1f + ctx.fft[fftIdx] * 0.4f

                    val r = maxR * (0.35f + 0.35f * sin(petals * theta + ctx.phase * 1.5f)) * mod
                    val x = cx + r * cos(theta + ctx.phase * 0.2f)
                    val y = cy + r * sin(theta + ctx.phase * 0.2f)

                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()

                drawPath(
                    path = path,
                    color = Color(0xFF00FFD5).copy(alpha = 0.35f),
                    style = Stroke(width = 8f)
                )
                drawPath(
                    path = path,
                    color = Color(0xFFFFEE55),
                    style = Stroke(width = 2.5f)
                )
            }
        ),

        // 6. Deep Space Warp
        AvsPreset(
            id = "deep_space_warp",
            name = "Deep Space Warp",
            category = "Cosmic 3D",
            author = "Star Voyager",
            description = "Прыжок в гиперпространство с ускорением звёздного потока от ударов баса",
            render = { ctx ->
                val cx = ctx.width / 2f
                val cy = ctx.height / 2f
                val starCount = 80
                val speed = 1f + ctx.bass * 3.5f

                for (i in 0 until starCount) {
                    val seedAngle = (i * 137.5f) * (PI.toFloat() / 180f)
                    val progress = ((ctx.phase * speed * 0.4f + i * 0.05f) % 1f)
                    val dist = progress.pow(2f) * max(cx, cy) * 1.3f

                    val x = cx + cos(seedAngle) * dist
                    val y = cy + sin(seedAngle) * dist

                    val trailLen = 6f + progress * 35f * (1f + ctx.bass * 2f)
                    val tailX = x - cos(seedAngle) * trailLen
                    val tailY = y - sin(seedAngle) * trailLen

                    val starColor = when (i % 3) {
                        0 -> Color(0xFF00FFFF)
                        1 -> Color(0xFFFF55AA)
                        else -> Color(0xFFFFFFFF)
                    }.copy(alpha = progress.coerceIn(0.2f, 1f))

                    drawLine(
                        color = starColor,
                        start = Offset(tailX, tailY),
                        end = Offset(x, y),
                        strokeWidth = 1f + progress * 3.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        ),

        // 7. Quantum DNA Helix
        AvsPreset(
            id = "quantum_dna_helix",
            name = "Quantum DNA Helix",
            category = "Molecular / Bio",
            author = "BioTech Lab",
            description = "Вращающаяся в пространстве двойная спираль ДНК с квантовыми связями",
            render = { ctx ->
                val nodes = 24
                val startY = ctx.height * 0.12f
                val endY = ctx.height * 0.88f
                val stepY = (endY - startY) / nodes
                val cx = ctx.width / 2f
                val amp = ctx.width * 0.28f * (1f + ctx.mid * 0.4f)

                for (i in 0 until nodes) {
                    val y = startY + i * stepY
                    val angle = (i * 0.42f) + ctx.phase * 2f
                    val x1 = cx + amp * sin(angle)
                    val x2 = cx - amp * sin(angle)

                    val fftIdx = (i * ctx.fft.size / nodes).coerceIn(0, ctx.fft.size - 1)
                    val bondEnergy = ctx.fft[fftIdx]

                    // Молекулярная связь между нитями
                    drawLine(
                        color = Color(0x6600E5FF),
                        start = Offset(x1, y),
                        end = Offset(x2, y),
                        strokeWidth = 1.5f + bondEnergy * 4f
                    )

                    // Нить 1
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 4f + bondEnergy * 6f,
                        center = Offset(x1, y)
                    )

                    // Нить 2
                    drawCircle(
                        color = Color(0xFFFF0077),
                        radius = 4f + bondEnergy * 6f,
                        center = Offset(x2, y)
                    )
                }
            }
        ),

        // 8. Analogue VU Meters
        AvsPreset(
            id = "analogue_vu_meters",
            name = "Analogue VU Meters",
            category = "Vintage Hi-Fi",
            author = "Warm Tubes",
            description = "Винтажные аналоговые стрелочные индикаторы уровня с баллистикой и подсветкой",
            render = { ctx ->
                val meterW = ctx.width * 0.44f
                val meterH = ctx.height * 0.55f
                val topY = (ctx.height - meterH) / 2f

                for (ch in 0..1) {
                    val leftX = if (ch == 0) ctx.width * 0.04f else ctx.width * 0.52f
                    val pivotX = leftX + meterW / 2f
                    val pivotY = topY + meterH * 0.88f

                    // Корпус стрелочника
                    drawRoundRect(
                        color = Color(0xFF181512),
                        topLeft = Offset(leftX, topY),
                        size = Size(meterW, meterH),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = Color(0xFF4A3E2D),
                        topLeft = Offset(leftX, topY),
                        size = Size(meterW, meterH),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 3f)
                    )

                    // Тёплая шкала
                    drawArc(
                        color = Color(0xFFFFD580).copy(alpha = 0.5f),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(leftX + meterW * 0.1f, topY + meterH * 0.15f),
                        size = Size(meterW * 0.8f, meterH * 0.8f),
                        style = Stroke(width = 2f)
                    )

                    // Стрелка
                    val energy = if (ch == 0) ctx.bass else ctx.mid
                    val needleAngle = 200f + energy * 140f
                    val rad = needleAngle * (PI.toFloat() / 180f)
                    val needleLen = meterH * 0.72f
                    val endX = pivotX + needleLen * cos(rad)
                    val endY = pivotY + needleLen * sin(rad)

                    drawLine(
                        color = if (energy > 0.85f) Color(0xFFFF2222) else Color(0xFFFFE0A0),
                        start = Offset(pivotX, pivotY),
                        end = Offset(endX, endY),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )

                    // Центральный винт
                    drawCircle(color = Color(0xFF887766), radius = 6f, center = Offset(pivotX, pivotY))
                }
            }
        ),

        // 9. Demoscene Neon Crystal
        AvsPreset(
            id = "demoscene_neon_crystal",
            name = "Demoscene Neon Crystal",
            category = "3D Polyhedra",
            author = "Future Crew Style",
            description = "Вращающийся неоновый 3D-октаэдр с импульсными вспышками граней",
            render = { ctx ->
                val cx = ctx.width / 2f
                val cy = ctx.height / 2f
                val size = min(cx, cy) * (0.6f + ctx.bass * 0.35f)

                val rotX = ctx.phase * 1.2f
                val rotY = ctx.phase * 1.8f

                // Вершины октаэдра в 3D
                val vertices = listOf(
                    floatArrayOf(0f, -1f, 0f),
                    floatArrayOf(0f, 1f, 0f),
                    floatArrayOf(-1f, 0f, 0f),
                    floatArrayOf(1f, 0f, 0f),
                    floatArrayOf(0f, 0f, -1f),
                    floatArrayOf(0f, 0f, 1f)
                )

                // Проекция 3D -> 2D
                val projected = vertices.map { v ->
                    val x0 = v[0]
                    val y0 = v[1]
                    val z0 = v[2]

                    // Rotation Y
                    val x1 = x0 * cos(rotY) + z0 * sin(rotY)
                    val z1 = -x0 * sin(rotY) + z0 * cos(rotY)

                    // Rotation X
                    val y2 = y0 * cos(rotX) - z1 * sin(rotX)
                    val z2 = y0 * sin(rotX) + z1 * cos(rotX)

                    val fov = 3.2f
                    val scale = fov / (fov + z2)
                    Offset(cx + x1 * size * scale, cy + y2 * size * scale)
                }

                // Рёбра октаэдра
                val edges = listOf(
                    0 to 2, 0 to 3, 0 to 4, 0 to 5,
                    1 to 2, 1 to 3, 1 to 4, 1 to 5,
                    2 to 4, 4 to 3, 3 to 5, 5 to 2
                )

                edges.forEach { (a, b) ->
                    drawLine(
                        color = Color(0xFF00FFEA),
                        start = projected[a],
                        end = projected[b],
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
        ),

        // 10. Laser Show
        AvsPreset(
            id = "laser_show",
            name = "Laser Show",
            category = "Stage FX",
            author = "Club Trance",
            description = "Мощные лазерные веерные лучи со стробоскопическими ударами на дропах",
            render = { ctx ->
                val originX = ctx.width / 2f
                val originY = ctx.height

                val beamCount = 18
                for (i in 0 until beamCount) {
                    val sweep = sin(ctx.phase * 2.5f + i * 0.35f) * 0.85f
                    val angle = -PI.toFloat() / 2f + sweep * (PI.toFloat() / 3f)

                    val fftIdx = (i * ctx.fft.size / beamCount).coerceIn(0, ctx.fft.size - 1)
                    val power = ctx.fft[fftIdx]

                    val beamLen = ctx.height * 1.2f
                    val targetX = originX + cos(angle) * beamLen
                    val targetY = originY + sin(angle) * beamLen

                    val color = when (i % 3) {
                        0 -> Color(0xFF00FF44)
                        1 -> Color(0xFF00E5FF)
                        else -> Color(0xFFFF0055)
                    }.copy(alpha = (0.3f + power * 0.7f).coerceIn(0.1f, 1f))

                    drawLine(
                        color = color,
                        start = Offset(originX, originY),
                        end = Offset(targetX, targetY),
                        strokeWidth = 2f + power * 7f,
                        cap = StrokeCap.Round
                    )
                }
            }
        ),

        // 11. Liquid Waveform
        AvsPreset(
            id = "liquid_waveform",
            name = "Liquid Waveform",
            category = "Fluid Waves",
            author = "Oceanic Pulse",
            description = "Многослойные интерференционные волны с градиентным свечением",
            render = { ctx ->
                val layers = 3
                val points = 80
                val stepX = ctx.width / points

                for (layer in 0 until layers) {
                    val path = Path()
                    val baseY = ctx.height * (0.45f + layer * 0.15f)
                    val color = when (layer) {
                        0 -> Color(0xFF00FFAA).copy(alpha = 0.7f)
                        1 -> Color(0xFF0088FF).copy(alpha = 0.5f)
                        else -> Color(0xFFAA00FF).copy(alpha = 0.35f)
                    }

                    for (i in 0..points) {
                        val waveIdx = (i * ctx.waveform.size / points).coerceIn(0, ctx.waveform.size - 1)
                        val w = ctx.waveform[waveIdx]

                        val sineWave = sin(i * 0.18f + ctx.phase * 3f + layer * 1.2f) * 35f
                        val y = baseY + sineWave + w * 70f * (1f + ctx.bass * 0.8f)
                        val x = i * stepX

                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 4f - layer * 0.8f, cap = StrokeCap.Round)
                    )
                }
            }
        ),

        // 12. Bass Reactor Core
        AvsPreset(
            id = "bass_reactor_core",
            name = "Bass Reactor Core",
            category = "Sci-Fi Energy",
            author = "Cyber Fusion",
            description = "Энергетический реактор с защитными кольцами, детонирующими от баса",
            render = { ctx ->
                val cx = ctx.width / 2f
                val cy = ctx.height / 2f
                val coreRadius = min(cx, cy) * (0.2f + ctx.bass * 0.3f)

                // Плазменное ядро
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFFFFFF), Color(0xFF00F0FF), Color(0x00000000)),
                        center = Offset(cx, cy),
                        radius = coreRadius * 1.5f
                    ),
                    radius = coreRadius * 1.5f,
                    center = Offset(cx, cy)
                )

                // Защитные вращающиеся кольца
                val rings = 4
                for (r in 1..rings) {
                    val ringR = coreRadius + r * (min(cx, cy) * 0.14f)
                    val rot = ctx.phase * (if (r % 2 == 0) 1.5f else -1.5f)

                    drawArc(
                        color = Color(0xFFFFCC00).copy(alpha = 0.85f),
                        startAngle = rot * 50f,
                        sweepAngle = 75f,
                        useCenter = false,
                        topLeft = Offset(cx - ringR, cy - ringR),
                        size = Size(ringR * 2f, ringR * 2f),
                        style = Stroke(width = 3.5f)
                    )

                    drawArc(
                        color = Color(0xFF00FFCC).copy(alpha = 0.85f),
                        startAngle = rot * 50f + 180f,
                        sweepAngle = 75f,
                        useCenter = false,
                        topLeft = Offset(cx - ringR, cy - ringR),
                        size = Size(ringR * 2f, ringR * 2f),
                        style = Stroke(width = 3.5f)
                    )
                }
            }
        )
    )

    fun getById(id: String): AvsPreset {
        return presets.firstOrNull { it.id == id } ?: presets[0]
    }
}
