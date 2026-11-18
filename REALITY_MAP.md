# 🗺️ MadGammon Reality Map & Production Audit Register

> *"Engineering integrity > marketing. Production is reached when two independent devices communicate over the real wire protocol, the version number does not lie, and every security claim is backed by test logs."*  
> — Engineering Audit Alignment (November 2026)

---

## 🧭 Status Taxonomy

| Status Badge | Definition | Verification Standard |
| :--- | :--- | :--- |
| 🟢 **`[REAL / PROD]`** | Fully implemented, mathematically rigorous, actively covered by automated unit/integration tests. | Passes JVM/Native test suites with 0 mock fallbacks. |
| 🟡 **`[INTEGRATION]`** | Production-ready architecture or native engine currently being wired into the mobile client layer. | Active FFI bindings / pipeline under development. |
| ⚪ **`[MOCK / DEPRECATED]`**| Temporary high-level prototype or simulation logic slated for complete removal. | Deprecated; superseded by canonical Rust core. |

---

## 📊 Comprehensive Component Verification Matrix

### 1. Cryptographic Identity & Vault

| Component | Target File | Status | Technical Details & Verification |
| :--- | :--- | :---: | :--- |
| **BIP-39 Mnemonic Generator** | `data.Bip39Helper` | 🟢 **`[REAL]`** | Strictly 24 words. 256 bits of cryptographically secure entropy (`SecureRandom`) + 8-bit SHA-256 checksum. Verified by `Bip39HelperTest`. |
| **BIP-39 Checksum Validator** | `data.Bip39Helper` | 🟢 **`[REAL]`** | Bit-level 264-bit reconstruction and verification. Detects single-bit word tampering. Verified by `Bip39HelperTest`. |
| **Key Derivation (HKDF)** | `data.Bip39Helper` | 🟢 **`[REAL]`** | Derives separate Ed25519 signing keys and X25519 Diffie-Hellman ratcheting keypairs with domain-separated info tags. |
| **PBKDF2-HMAC-SHA512** | `data.Bip39Helper` | 🟢 **`[REAL]`** | 2,048 iterations for BIP-39 binary seed derivation; 210,000 iterations for master PIN vault derivation. |
| **Reed-Solomon Erasure Coding** | `crypto.ErasureCodedVault` | 🟢 **`[REAL]`** | Galois Field $GF(2^8)$ sharding (4 data + 2 parity). Full reconstruction from any 4 shards. Verified by `ErasureCodedVaultTest`. |
| **PDF Passport Certificate** | `data.PdfPassportGenerator`| 🟢 **`[REAL]`** | High-resolution vector A4 PDF generation with 24-word grid, fingerprint badge, and cold-storage instructions. |

### 2. Zero-Knowledge Mental Dice Protocol

| Component | Target File | Status | Technical Details & Verification |
| :--- | :--- | :---: | :--- |
| **Commit-Reveal Engine** | `crypto.MentalDiceEngine` | 🟢 **`[REAL]`** | Provably fair dice rolls without central servers. Alice and Bob generate random seeds, exchange HMAC-SHA256 commitments, reveal seeds, and compute deterministic dice values. Verified by `MentalDiceEngineTest`. |
| **Rust Native Dice Core** | `native/madgammon-core` | 🟢 **`[REAL]`** | `crypto::mental_dice` module compiled via UniFFI with automatic zeroization (`ZeroizeOnDrop`). |

### 3. Messaging & Transport Layer

| Component | Target File | Status | Technical Details & Verification |
| :--- | :--- | :---: | :--- |
| **Double Ratchet (E2EE)** | `crypto.DoubleRatchetVault`| 🟢 **`[REAL]`** | Signal-standard asymmetric KDF ratcheting with AES-256-GCM payload encryption. State serialization/deserialization verified by `DoubleRatchetVaultTest`. |
| **Constant-Size Padding (16KB)**| `crypto.DoubleRatchetVault`| 🟢 **`[REAL]`** | Fixed 16,384-byte frame padding to prevent Deep Packet Inspection (DPI) and metadata traffic analysis. |
| **RustSimplex Core (`smp-rs`)**| `RustSimplex` (GitHub) | 🟡 **`[INTEGRATION]`**| True `simplexmq` wire protocol: binary transmission encoding, TLS 1.3 keyHash pinning, PQDR, and native FFI. Phase A integration into BG-AG underway. |
| **Kotlin `SimpleXSmpClient`** | `data.SimpleXSmpClient` | ⚪ **`[DEPRECATED]`** | Custom text dialect (`SUB`/`MSG`/`NEW`) used during prototyping. Slated for removal upon completion of `RustSmpBridge`. |

### 4. Anti-Censorship & Network Resiliency

| Component | Target File | Status | Technical Details & Verification |
| :--- | :--- | :---: | :--- |
| **Native Tun2Socks Router** | `service.FoxrayVpnService`| 🟢 **`[REAL]`** | Direct execution of PIE binary `libhev-socks5-tunnel.so` with fallback to `Tun2SocksPacketRouter`. Connects Android `tun_fd` to local SOCKS5 proxy. |
| **MultiPath Happy Eyeballs v2** | `data.TorP2PManager` | 🟢 **`[REAL]`** | RFC 8305 connection racing across Direct, V2Ray/Xray, and Tor Onion circuits with automatic fast-failover. |
| **Android 14+ Foreground Service**| `service.SimpleXPushService`| 🟢 **`[REAL]`**| `specialUse` service type with WakeLock and 45-second heartbeat loop to prevent OS process killing. |
| **Battery Optimization Whitelist**| `MainActivity` / Manifest | 🟢 **`[REAL]`** | `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` runtime intent for uninterrupted background push delivery. |

### 5. Backgammon Game Engine & Audio Studio

| Component | Target File | Status | Technical Details & Verification |
| :--- | :--- | :---: | :--- |
| **MAD Backgammon Ruleset** | `model.GameEngine` | 🟢 **`[REAL]`** | Cascading doubles, turn succession, unused dice transfers. Verified by `GameEngineRulesTest`. |
| **LONG Backgammon Ruleset** | `model.GameEngine` | 🟢 **`[REAL]`** | Traditional Eastern long backgammon rules (head rule, 6-checker prime block validation). Verified by `GameEngineRulesTest`. |
| **SHORT Backgammon Ruleset** | `model.GameEngine` | 🟢 **`[REAL]`** | International standard: bar re-entry, hitting checkers, bearing off. Verified by `GameEngineTest`. |
| **Cyberpunk AVS Visualizer** | `ui.avs.AvsVisualizerView` | 🟢 **`[REAL]`** | 60 FPS Compose Canvas, 12 demoscene/Winamp presets, Android `Visualizer` API spectrum capture. Verified by `AvsPresetsPackTest`. |
| **AVS Fullscreen Screensaver** | `ui.avs.AvsFullscreenDialog`| 🟢 **`[REAL]`**| Edge-to-Edge immersive dialog with swipe gesture preset switching and HUD controls. |

### 6. Production Packaging & Release Hygiene

| Item | Target File / Config | Status | Notes |
| :--- | :--- | :---: | :--- |
| **Package Namespace** | `app/build.gradle.kts` | 🟢 **`[REAL]`** | Aligned from prototype `com.example` to production `org.madgammon`. |
| **Application ID** | `app/build.gradle.kts` | 🟢 **`[REAL]`** | `org.madgammon.app`. |
| **Version Designation** | `app/build.gradle.kts` | 🟢 **`[REAL]`** | `0.9.0-rc2` (honest milestone acknowledging RC status prior to field test). |
| **Release Minification & R8** | `app/build.gradle.kts` | 🟢 **`[REAL]`** | `isMinifyEnabled = true`, `isShrinkResources = true` with hardened `proguard-rules.pro` for BouncyCastle, UniFFI, Room. |
| **Continuous Integration (CI)** | `.github/workflows/` | 🟢 **`[REAL]`** | Automated Rust core tests (`cargo test`), JVM unit tests (`testDebugUnitTest`), and APK assembly. |
| **Two-Device Physical Test** | Real Android Hardware | 🟡 **`[PENDING]`** | Mandatory field test (20+ messages across LTE vs Wi-Fi) scheduled for Phase B. |

---

## 🛣️ Roadmap to Production 1.0.0

```
┌───────────────────────────────────────────────┐
│              PHASE 1: PROD HYGIENE            │  <-- [COMPLETED]
│  • Namespace aligned to org.madgammon         │
│  • ProGuard rules hardened & minify enabled   │
│  • CI automated testing pipeline configured   │
│  • REALITY_MAP.md published in repository     │
└──────────────────────┬────────────────────────┘
                       │
┌──────────────────────▼────────────────────────┐
│           PHASE 2: RUSTSIMPLEX BRIDGE         │  <-- [NEXT STEP]
│  • Link smp-rs from RustSimplex repository    │
│  • Generate UniFFI bindings for Kotlin        │
│  • Build native shared libraries (.so)        │
│  • Create thin Kotlin RustSmpBridge           │
└──────────────────────┬────────────────────────┘
                       │
┌──────────────────────▼────────────────────────┐
│        PHASE 3: FIELD TEST & 1.0.0 PROD       │
│  • Replace Kotlin SimpleX client with bridge  │
│  • End-to-end test on 2 physical Android phones│
│  • Verify LTE <-> Wi-Fi cross-network traffic │
│  • Release MadGammon v1.0.0 Production        │
└───────────────────────────────────────────────┘
```
