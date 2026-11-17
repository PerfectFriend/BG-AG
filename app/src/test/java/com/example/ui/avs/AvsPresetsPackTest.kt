package com.example.ui.avs

import org.junit.Assert.*
import org.junit.Test

/**
 * Модульные тесты для банка AVS пресетов и математики визуализатора
 */
class AvsPresetsPackTest {

    @Test
    fun testPresetsPackCompleteness() {
        val presets = AvsPresetsPack.presets
        assertTrue("Presets pack must not be empty", presets.isNotEmpty())
        assertEquals("Must contain at least 12 demoscene presets", 12, presets.size)

        val ids = mutableSetOf<String>()
        val names = mutableSetOf<String>()

        presets.forEach { preset ->
            assertTrue("Preset ID must not be blank", preset.id.isNotBlank())
            assertTrue("Preset name must not be blank", preset.name.isNotBlank())
            assertTrue("Preset category must not be blank", preset.category.isNotBlank())
            assertTrue("Preset author must not be blank", preset.author.isNotBlank())
            assertTrue("Preset description must not be blank", preset.description.isNotBlank())

            assertTrue("Preset IDs must be unique: ${preset.id}", ids.add(preset.id))
            assertTrue("Preset names must be unique: ${preset.name}", names.add(preset.name))
        }
    }

    @Test
    fun testGetByIdFallback() {
        val first = AvsPresetsPack.presets[0]
        val fetchedFirst = AvsPresetsPack.getById(first.id)
        assertEquals(first.id, fetchedFirst.id)

        val unknown = AvsPresetsPack.getById("non_existent_preset_xyz")
        assertNotNull(unknown)
        assertEquals(first.id, unknown.id)
    }

    @Test
    fun testAvsFrameContextConstraints() {
        val fft = FloatArray(64) { 0.5f }
        val wave = FloatArray(128) { 0.1f }
        val ctx = AvsFrameContext(
            width = 1080f,
            height = 2400f,
            fft = fft,
            waveform = wave,
            bass = 0.8f,
            mid = 0.5f,
            treble = 0.3f,
            isBeat = true,
            time = 1.5f,
            phase = 0.2f
        )

        assertEquals(1080f, ctx.width)
        assertEquals(2400f, ctx.height)
        assertEquals(64, ctx.fft.size)
        assertEquals(128, ctx.waveform.size)
        assertTrue(ctx.isBeat)
        assertEquals(0.8f, ctx.bass, 0.001f)
    }
}
