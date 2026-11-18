package com.example.data

import android.content.Context
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.MessageDigest
import java.security.SecureRandom

object Bip39Helper {
    private var cachedWordlist: List<String>? = null

    /**
     * Loads the BIP-39 English wordlist from assets or file fallback for JVM unit tests.
     */
    fun loadWordlist(context: Context? = null): List<String> {
        cachedWordlist?.let { return it }
        val stream = try {
            context?.assets?.open("bip39_english.txt")
        } catch (_: Exception) { null }
            ?: java.io.File("src/main/assets/bip39_english.txt").takeIf { it.exists() }?.inputStream()
            ?: java.io.File("app/src/main/assets/bip39_english.txt").takeIf { it.exists() }?.inputStream()
            ?: Bip39Helper::class.java.classLoader?.getResourceAsStream("bip39_english.txt")
            ?: Bip39Helper::class.java.classLoader?.getResourceAsStream("assets/bip39_english.txt")

        return try {
            stream?.bufferedReader()?.use { reader ->
                val words = reader.readLines().map { it.trim() }.filter { it.isNotEmpty() }
                if (words.size == 2048) {
                    cachedWordlist = words
                    words
                } else {
                    emptyList()
                }
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Generates a random 24-word seed phrase according to BIP-39.
     * Uses 256 bits (32 bytes) of entropy + 8 bits checksum = 264 bits = 24 words.
     */
    fun generateMnemonic(context: Context? = null): String {
        val wordlist = loadWordlist(context)
        if (wordlist.size != 2048) {
            // Returning a fixed phrase here would hand every affected install the same
            // publicly known seed, so fail loudly instead.
            throw IllegalStateException(
                "BIP-39 wordlist is missing or truncated (${wordlist.size} of 2048 words); refusing to generate a predictable seed phrase"
            )
        }

        val entropy = ByteArray(32)
        SecureRandom().nextBytes(entropy)

        // Compute SHA-256 for checksum
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(entropy)
        
        // 256 / 32 = 8 bits checksum (first full byte of SHA-256)
        val checksumByte = hash[0]
        
        // Convert entropy bytes and 8-bit checksum to a continuous list of bits (264 bits)
        val bits = BooleanArray(264)
        var bitIndex = 0
        for (b in entropy) {
            for (i in 7 downTo 0) {
                bits[bitIndex++] = ((b.toInt() ushr i) and 1) == 1
            }
        }
        // Append 8 bits of checksum (from the first byte of SHA-256)
        for (i in 7 downTo 0) {
            bits[bitIndex++] = ((checksumByte.toInt() ushr i) and 1) == 1
        }

        // Convert 264 bits to 24 words (24 * 11 bits)
        val mnemonicWords = mutableListOf<String>()
        for (w in 0 until 24) {
            var index = 0
            for (i in 0 until 11) {
                index = index shl 1
                if (bits[w * 11 + i]) {
                    index = index or 1
                }
            }
            mnemonicWords.add(wordlist[index])
        }

        return mnemonicWords.joinToString(" ")
    }

    /**
     * Validates a BIP-39 seed phrase (supporting standard 24-word and 12-word mnemonics).
     */
    fun validateMnemonic(context: Context? = null, mnemonic: String): Boolean {
        val wordlist = loadWordlist(context)
        if (wordlist.size != 2048) return false

        val words = mnemonic.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (words.size != 24 && words.size != 12) return false

        // 1. Verify words exist in wordlist
        for (w in words) {
            if (!wordlist.contains(w)) return false
        }

        // 2. Perform checksum verification
        try {
            if (words.size == 24) {
                val bits = BooleanArray(264)
                for (w in 0 until 24) {
                    val index = wordlist.indexOf(words[w])
                    if (index < 0) return false
                    for (i in 10 downTo 0) {
                        bits[w * 11 + (10 - i)] = ((index ushr i) and 1) == 1
                    }
                }

                // Extract entropy bytes (32 bytes = 256 bits)
                val entropy = ByteArray(32)
                for (currByte in 0 until 32) {
                    var value = 0
                    for (b in 0 until 8) {
                        value = value shl 1
                        if (bits[currByte * 8 + b]) {
                            value = value or 1
                        }
                    }
                    entropy[currByte] = value.toByte()
                }

                // Extract checksum (last 8 bits)
                var expectedChecksumVal = 0
                for (b in 256 until 264) {
                    expectedChecksumVal = expectedChecksumVal shl 1
                    if (bits[b]) {
                        expectedChecksumVal = expectedChecksumVal or 1
                    }
                }

                // Compute actual checksum from entropy (first 8 bits of SHA-256)
                val md = MessageDigest.getInstance("SHA-256")
                val hash = md.digest(entropy)
                val actualChecksumVal = hash[0].toInt() and 0xFF

                return expectedChecksumVal == actualChecksumVal
            } else {
                // 12 words fallback
                val bits = BooleanArray(132)
                for (w in 0 until 12) {
                    val index = wordlist.indexOf(words[w])
                    if (index < 0) return false
                    for (i in 10 downTo 0) {
                        bits[w * 11 + (10 - i)] = ((index ushr i) and 1) == 1
                    }
                }

                // Extract entropy bytes (16 bytes = 128 bits)
                val entropy = ByteArray(16)
                for (currByte in 0 until 16) {
                    var value = 0
                    for (b in 0 until 8) {
                        value = value shl 1
                        if (bits[currByte * 8 + b]) {
                            value = value or 1
                        }
                    }
                    entropy[currByte] = value.toByte()
                }

                // Extract checksum (last 4 bits)
                var expectedChecksumVal = 0
                for (b in 128 until 132) {
                    expectedChecksumVal = expectedChecksumVal shl 1
                    if (bits[b]) {
                        expectedChecksumVal = expectedChecksumVal or 1
                    }
                }

                val md = MessageDigest.getInstance("SHA-256")
                val hash = md.digest(entropy)
                val actualChecksumVal = (hash[0].toInt() ushr 4) and 0x0F

                return expectedChecksumVal == actualChecksumVal
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Converts a BIP-39 mnemonic (24 words) into a 512-bit (64-byte) binary seed
     * using standard PBKDF2 with HMAC-SHA512 (2048 iterations, salt "mnemonic" + passphrase).
     */
    fun mnemonicToSeed(mnemonic: String, passphrase: String = ""): ByteArray {
        val normalizedMnemonic = mnemonic.trim().lowercase().split("\\s+".toRegex()).joinToString(" ")
        val password = normalizedMnemonic.toByteArray(Charsets.UTF_8)
        val salt = ("mnemonic$passphrase").toByteArray(Charsets.UTF_8)
        return pbkdf2HmacSha512(password, salt, 2048, 64)
    }

    /**
     * Derives full cryptographic identity keys and fingerprints from the 24-word seed phrase.
     * Uses HKDF-SHA256 domain separation to derive:
     * - Ed25519 identity keypair (for signatures / auth)
     * - X25519 DH keypair (for key exchange / ratchet)
     * - Sovereign public fingerprint
     */
    fun deriveCryptoIdentity(mnemonic: String): CryptoIdentity {
        val seedBytes = mnemonicToSeed(mnemonic)
        val seedBytesHex = seedBytes.joinToString("") { "%02x".format(it) }

        // Master Private Key: first 32 bytes (256 bits)
        val masterPrivateKeyBytes = seedBytes.copyOfRange(0, 32)

        // 1. Derive Ed25519 Identity Keypair using HKDF-SHA256 domain separation
        val ed25519Seed = deriveSubkey(seedBytes, "madgammon-ed25519-identity-key".toByteArray(Charsets.UTF_8), 32)
        val ed25519PrivKey = Ed25519PrivateKeyParameters(ed25519Seed, 0)
        val ed25519PubKey = ed25519PrivKey.generatePublicKey()
        val ed25519PubKeyBytes = ed25519PubKey.encoded
        val ed25519PrivKeyHex = ed25519Seed.joinToString("") { "%02x".format(it) }
        val ed25519PubKeyHex = ed25519PubKeyBytes.joinToString("") { "%02x".format(it) }

        // 2. Derive X25519 DH Keypair using HKDF-SHA256 domain separation
        val x25519Seed = deriveSubkey(seedBytes, "madgammon-x25519-dh-key".toByteArray(Charsets.UTF_8), 32)
        val x25519PrivKey = X25519PrivateKeyParameters(x25519Seed, 0)
        val x25519PubKey = x25519PrivKey.generatePublicKey()
        val x25519PubKeyBytes = x25519PubKey.encoded
        val x25519PrivKeyHex = x25519Seed.joinToString("") { "%02x".format(it) }
        val x25519PubKeyHex = x25519PubKeyBytes.joinToString("") { "%02x".format(it) }

        // 3. Derive Sovereign Identity Fingerprint from Ed25519 public key (as in SSH / PGP standard)
        val sha256 = MessageDigest.getInstance("SHA-256")
        sha256.update("madgammon-identity-fingerprint".toByteArray(Charsets.UTF_8))
        val fingerprintBytes = sha256.digest(ed25519PubKeyBytes)
        val masterKeyFingerprintHex = fingerprintBytes.joinToString("") { "%02x".format(it) }

        // Identity Fingerprint formatted: e.g. "A1B2:C3D4:E5F6:7890:1234:5678:90AB:CDEF"
        val fingerprint = fingerprintBytes.take(16)
            .chunked(2)
            .joinToString(":") { chunk -> chunk.joinToString("") { "%02X".format(it) } }

        val words = mnemonic.trim().lowercase().split("\\s+".toRegex())

        return CryptoIdentity(
            seedPhrase = words.joinToString(" "),
            words = words,
            seedBytesHex = seedBytesHex,
            masterKeyFingerprintHex = masterKeyFingerprintHex,
            identityFingerprint = fingerprint,
            ed25519PublicKeyHex = ed25519PubKeyHex,
            ed25519PrivateKeyHex = ed25519PrivKeyHex,
            x25519PublicKeyHex = x25519PubKeyHex,
            x25519PrivateKeyHex = x25519PrivKeyHex,
            masterPublicKeyHex = ed25519PubKeyHex,
            masterPrivateKeyHex = seedBytesHex
        )
    }

    /**
     * HKDF-SHA256 key derivation with info context tag.
     */
    private fun deriveSubkey(ikm: ByteArray, info: ByteArray, outLen: Int): ByteArray {
        val hkdf = HKDFBytesGenerator(SHA256Digest())
        hkdf.init(HKDFParameters(ikm, "madgammon-kdf-salt".toByteArray(Charsets.UTF_8), info))
        val out = ByteArray(outLen)
        hkdf.generateBytes(out, 0, outLen)
        return out
    }

    private fun pbkdf2HmacSha512(password: ByteArray, salt: ByteArray, iterations: Int, keyLengthBytes: Int): ByteArray {
        val mac = javax.crypto.Mac.getInstance("HmacSHA512")
        mac.init(javax.crypto.spec.SecretKeySpec(password, "HmacSHA512"))
        val hLen = mac.macLength // 64 bytes
        val l = (keyLengthBytes + hLen - 1) / hLen
        val r = keyLengthBytes - (l - 1) * hLen
        val result = ByteArray(keyLengthBytes)
        var offset = 0
        val block = ByteArray(salt.size + 4)
        System.arraycopy(salt, 0, block, 0, salt.size)

        for (i in 1..l) {
            block[salt.size] = (i ushr 24).toByte()
            block[salt.size + 1] = (i ushr 16).toByte()
            block[salt.size + 2] = (i ushr 8).toByte()
            block[salt.size + 3] = i.toByte()

            var u = mac.doFinal(block)
            val t = u.clone()

            for (c in 2..iterations) {
                u = mac.doFinal(u)
                for (k in t.indices) {
                    t[k] = (t[k].toInt() xor u[k].toInt()).toByte()
                }
            }

            val bytesToCopy = if (i == l) r else hLen
            System.arraycopy(t, 0, result, offset, bytesToCopy)
            offset += bytesToCopy
        }
        return result
    }

    /**
     * Signs a message using Ed25519 identity private key.
     */
    fun signMessage(privateKeyHex: String, message: ByteArray): ByteArray {
        val privBytes = hexToBytes(privateKeyHex)
        val privParams = Ed25519PrivateKeyParameters(privBytes, 0)
        val signer = Ed25519Signer()
        signer.init(true, privParams)
        signer.update(message, 0, message.size)
        return signer.generateSignature()
    }

    /**
     * Verifies an Ed25519 signature against a public key and message.
     */
    fun verifySignature(publicKeyHex: String, message: ByteArray, signature: ByteArray): Boolean {
        return try {
            val pubBytes = hexToBytes(publicKeyHex)
            val pubParams = Ed25519PublicKeyParameters(pubBytes, 0)
            val verifier = Ed25519Signer()
            verifier.init(false, pubParams)
            verifier.update(message, 0, message.size)
            verifier.verifySignature(signature)
        } catch (e: Exception) {
            false
        }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.trim()
        val result = ByteArray(clean.length / 2)
        for (i in result.indices) {
            val index = i * 2
            result[i] = clean.substring(index, index + 2).toInt(16).toByte()
        }
        return result
    }
}

/**
 * Encapsulates the derived cryptographic identity.
 */
data class CryptoIdentity(
    val seedPhrase: String,
    val words: List<String>,
    val seedBytesHex: String,
    val masterKeyFingerprintHex: String,
    val identityFingerprint: String,
    val createdAt: Long = System.currentTimeMillis(),
    val onionPeerId: String = "anon-" + identityFingerprint.take(12),
    val masterPublicKeyHex: String = masterKeyFingerprintHex,
    val masterPrivateKeyHex: String = seedBytesHex,
    val ed25519PublicKeyHex: String = "",
    val ed25519PrivateKeyHex: String = "",
    val x25519PublicKeyHex: String = "",
    val x25519PrivateKeyHex: String = ""
)

