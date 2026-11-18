<p align="center">
  <img src="docs/images/banner_top.png" alt="MadGammon Top Banner" width="100%" />
</p>

# 🎲 Shesh-Besh (MadGammon)
### Ecosistema Soberano de Backgammon Criptográfico y Privacidad Zero-Knowledge

<p align="center">
  <a href="README.md"><b>🇬🇧 English</b></a> •
  <a href="README.ru.md"><b>🇷🇺 Русский</b></a> •
  <a href="README.es.md"><b>🇪🇸 Español</b></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Plataforma-Android%208.0%2B%20(API%2026--36)-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Interfaz-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/Núcleo-Rust%20(UniFFI)-DEA584?style=for-the-badge&logo=rust&logoColor=black" alt="Rust" />
  <img src="https://img.shields.io/badge/Seguridad-Double%20Ratchet%20%2B%20Tor-FF6F00?style=for-the-badge&logo=torproject&logoColor=white" alt="Seguridad" />
</p>

---

## 🌌 Filosofía Arquitectónica: Negación Plausible (Plausible Deniability)

**MadGammon** está concebido bajo el principio fundamental de **negación plausible**:

```
+---------------------------------------------------------------------------------+
|                                FACHADA MADGAMMON                                |
|  • Motor de Backgammon: MAD (Loco), LONG (Largo), SHORT (Corto) con IA táctica  |
|  • Visualizador AVS Cyberpunk (12 presets demoscene, streaming de radio SomaFM) |
|  • MatrixKeyboard: Teclado virtual directo que evita keyloggers del sistema OS  |
+---------------------------------------------------------------------------------+
                                         │
                   [Disparador Secreto / PIN Maestro de 6 Dígitos]
                                         ▼
+---------------------------------------------------------------------------------+
|                         BÓVEDA CRIPTOGRÁFICA SOBERANA                           |
|  • Identidad BIP-39: Estrictamente 24 palabras + derivación Ed25519 / X25519    |
|  • Mental Dice: Dados demostrablemente justos con HMAC-SHA256 Commit-Reveal     |
|  • Chat Privado E2EE: Double Ratchet con relleno constante de 16KB anti-DPI     |
|  • Túnel Anti-Censura MultiPath: Tor Onion SOCKS5 + V2Ray + router Tun2Socks    |
|  • Bloqueo de Pánico: PIN señuelo sella la bóveda por 48h (recuperación con 24p)|
+---------------------------------------------------------------------------------+
```

---

## ✨ Características Principales

### 🎲 Motor de Backgammon Soberano
* **Tres Modalidades de Juego**:
  * **MAD («Backgammon Loco»)**: Dobles en cascada donde los puntos no jugados se transfieren al rival.
  * **LONG (Largo Clásico)**: Reglas tradicionales orientales (regla de la cabeza, prohibición de bloqueo de 6 fichas).
  * **SHORT (Corto Estándar)**: Formato de torneo internacional con barra y extracción de fichas.
* **Protocolo Criptográfico Mental Dice**: Generación P2P de tiradas de dados mediante protocolo commit-reveal HMAC-SHA256 sin dependencia de servidores centrales.

### 🎨 Estudio de Visualización AVS Cyberpunk
* **Salvapantallas Pantalla Completa Edge-to-Edge**:
  * 12 presets de visualización estilo Winamp y demoscene (*Neon Gridwave, Cyber Mandala, Quantum Horizon, Plasma Firestorm, Liquid Nebula, Pulsar Ring, etc.*).
  * Captura de espectro en tiempo real mediante Android `Visualizer` API (con generador sintético de respaldo).
  * Gestos táctiles interactivos, menú HUD OSD y consumo de energía ultra optimizado.
* **Radio Cyberpunk Integrada**: Reproducción en segundo plano de emisoras SomaFM (Groove Salad, Secret Agent, DEF CON Radio) y canales de audio de Telegram.

### 🔐 Identidad Criptográfica: Estándar BIP-39 de 24 Palabras
* **Incorporación Determinista de Alta Seguridad**:
  * Generación a partir de 256 bits de entropía segura (`SecureRandom`) + 8 bits de suma de verificación SHA-256.
  * Derivación de claves de firma digital Ed25519 y pares Diffie-Hellman X25519.
  * Copia en un toque, verificación interactiva de 3 palabras y exportación de **Pasaporte Certificado en PDF**.

### 💬 Mensajería Privada con Cifrado de Extremo a Extremo
* **Protocolo Double Ratchet**: Ratcheting criptográfico estilo Signal con cifrado autenticado AES-256-GCM.
* **Relleno Fijo de 16 KB (Padding)**: Máxima resistencia contra Inspección Profunda de Paquetes (DPI) al igualar el tamaño de todos los mensajes.
* **SimpleX SMP / XFTP**: Relés descentralizados y anónimos sin números de teléfono ni identificadores públicos.

### 🧅 Conectividad Anti-Censura MultiPath
* **Router Nativo Tun2Socks**: Desvía el tráfico `tun_fd` de la VPN de Android hacia el proxy SOCKS5 local mediante binarios nativos PIE (`libhev-socks5-tunnel.so`).
* **MultiPath Happy Eyeballs v2**: Carrera de conexión simultánea entre conexión Directa, V2Ray/Xray y circuitos Tor Onion.
* **Servicio en Primer Plano Android 14+**: Entrega fiable de notificaciones con WakeLock y latidos de control.

---

## 🦀 Núcleo Nativo en Rust (`native/madgammon-core`)

Los componentes críticos de rendimiento y seguridad se ejecutan en **Rust** con borrado seguro de memoria (`ZeroizeOnDrop`) e interfaces generadas con **Mozilla UniFFI**:

| Módulo | Ruta en el Crate | Función |
| :--- | :--- | :--- |
| **`mental_dice`** | `crypto::mental_dice` | Generación justa de dados con HMAC-SHA256 Commit-Reveal |
| **`double_ratchet`** | `crypto::double_ratchet` | Sesión Double Ratchet con padding fijo de 16KB |
| **`hybrid_pq`** | `crypto::hybrid_pq` | Intercambio de claves X25519 con verificación SAS de 4 emojis |
| **`erasure`** | `vault::erasure` | Corrección de errores Reed-Solomon $GF(2^8)$ y fragmentación |
| **`mnemonic`** | `vault::mnemonic` | Generador de 24 palabras BIP-39 y PBKDF2-HMAC-SHA512 |
| **`rules`** | `engine::rules` | Máquina de estados finita determinista (MAD/LONG/SHORT) |
| **`tun2socks`** | `net::tun2socks` | Enrutador asíncrono de interfaz TUN a proxy SOCKS5 |

---

## 🛠️ Compilación e Instalación

### Requisitos del Sistema
* **JDK:** OpenJDK 17 o Adoptium Temurin 21 LTS
* **Android SDK:** Compile & Target SDK 36, Android NDK r27c
* **Herramientas Rust:**
  ```bash
  rustup target add aarch64-linux-android x86_64-linux-android
  cargo install cargo-ndk uniffi-bindgen --version 0.28.0
  ```

### 1. Compilar el Núcleo Nativo en Rust
```bash
chmod +x scripts/build_rust_core.sh
./scripts/build_rust_core.sh
```

### 2. Compilar el APK de Android
```bash
./gradlew assembleDebug
# Archivo generado: app/build/outputs/apk/debug/app-debug.apk
```

### 3. Ejecutar Pruebas Unitarias
```bash
./gradlew testDebugUnitTest --tests "com.example.crypto.*" --tests "com.example.data.*" --tests "com.example.ui.avs.*" --tests "com.example.model.*"
```

---

## 🛡️ Buenas Prácticas de Seguridad

1. **PIN de Pánico**: Ante cualquier intento de coacción física, ingresar el PIN de pánico abrirá inmediatamente el juego de backgammon normal y sellará la bóveda secreta durante 48 horas. Solo se puede desbloquear con la frase semilla de 24 palabras.
2. **Protección contra Keyloggers**: Todas las contraseñas, PINs y semillas pueden introducirse a través del teclado `MatrixKeyboard` integrado, evitando la intercepción por el sistema de teclado de Android.
3. **Almacenamiento Físico Offline**: Exporte el **Pasaporte PDF** oficial inmediatamente después del registro e imprímalo para guardarlo en un lugar seguro fuera de línea.

---

## 📜 Reglas Oficiales del Juego

* [**Reglamento Oficial de Backgammon (MAD / LONG / SHORT)**](docs/RULES.md)

---

<p align="center">
  <img src="docs/images/banner_bottom.png" alt="MadGammon Bottom Banner" width="100%" />
</p>
