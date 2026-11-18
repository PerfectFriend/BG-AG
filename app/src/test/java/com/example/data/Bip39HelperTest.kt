package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Bip39HelperTest {

    @Test
    fun testGenerateAndValidate24WordMnemonic() {
        val mnemonic = Bip39Helper.generateMnemonic(null)
        assertNotNull(mnemonic)
        val words = mnemonic.split("\\s+".toRegex())
        assertEquals("BIP-39 mnemonic must contain exactly 24 words", 24, words.size)

        // Must validate successfully with correct checksum
        val isValid = Bip39Helper.validateMnemonic(null, mnemonic)
        assertTrue("Generated 24-word mnemonic must pass BIP-39 validation", isValid)
    }

    @Test
    fun testCorruptedMnemonicFailsValidation() {
        val mnemonic = Bip39Helper.generateMnemonic(null)
        val words = mnemonic.split("\\s+".toRegex()).toMutableList()

        // Corrupt last word (checksum word)
        words[words.size - 1] = if (words.last() == "abandon") "zoo" else "abandon"
        val corruptedMnemonic = words.joinToString(" ")

        val isValid = Bip39Helper.validateMnemonic(null, corruptedMnemonic)
        assertFalse("Corrupted 24-word mnemonic must fail BIP-39 checksum check", isValid)
    }

    @Test
    fun testDeriveCryptoIdentityFrom24Words() {
        val mnemonic = Bip39Helper.generateMnemonic(null)
        val identity = Bip39Helper.deriveCryptoIdentity(mnemonic)

        assertEquals(24, identity.words.size)
        assertNotNull(identity.ed25519PublicKeyHex)
        assertEquals(64, identity.ed25519PublicKeyHex.length) // 32 bytes hex
        assertNotNull(identity.x25519PublicKeyHex)
        assertEquals(64, identity.x25519PublicKeyHex.length) // 32 bytes hex
        assertNotNull(identity.identityFingerprint)
        assertTrue(identity.identityFingerprint.contains(":"))
    }
}
