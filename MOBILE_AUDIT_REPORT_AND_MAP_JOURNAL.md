# 🧭 МАДГАММОН (BG-AG): КАРТА-ЖУРНАЛ И ПОЛНЫЙ АУДИТ МОБИЛЬНОЙ ВЕРСИИ

**Дата обновления**: 17 Ноября 2026 г.  
**Платформа**: Android (Kotlin 2.0 / Jetpack Compose / Skia / Android NDK)  
**Репозиторий**: `/home/thomas/Projects/BG-AG` (Строго мобильный стек)

---

## 📊 1. СВОДНАЯ СТАТИСТИКА ГОТОВНОСТИ

```
┌───────────────────────────────────────────────────────────┐
│ ОБЩИЙ СТАТУС ПРОЕКТА: РЕЛИЗ-КАНДИДАТ (RC1+)               │
├──────────────────────────────────────┬────────────────────┤
│ 🧊 ГОТОВО К ПРОДАКШЕНУ [FROZEN]       │ 95% компонентов    │
│ 🔥 ТРЕБУЕТ ВНИМАНИЯ И ДОРАБОТКИ [HOT] │  5% компонентов    │
│ 🛡️ ПРОВЕРКА СБОРКИ (GRADLE / KOTLIN)  │ ✅ BUILD SUCCESSFUL│
└──────────────────────────────────────┴────────────────────┘
```

```
ПРОГРЕСС ГОТОВНОСТИ К РЕЛИЗУ:
[██████████████████████████████████████████████████░░] 95%
```

---

## 🧊 2. СЕКТОР [FROZEN] (ХОЛОДНЫЙ / СИНИЙ — ГОТОВО К ПРОДАКШЕНУ, АУДИТ ЗАВЕРШЁН)

### 🌐 2.1. Защита от блокировок и нативный Tun2Socks (НОВОЕ)
* **[`FoxrayVpnService.kt`](file:///home/thomas/Projects/BG-AG/app/src/main/java/com/example/service/FoxrayVpnService.kt)**:
  * Прямой запуск нативного PIE-бинарника `libhev-socks5-tunnel.so` из `nativeLibraryDir` с гарантированной выдачей прав на исполнение (`setExecutable(true)`).
  * Фоновый поток считывания логов `HevLogReader`.
  * Двухуровневый бесшовный fallback на пользовательский IP-маршрутизатор `Tun2SocksPacketRouter` (DoH + SOCKS5) при любых сбоях окружения.

### ⚡ 2.2. MultiPath P2P Connection Racing (НОВОЕ)
* **[`TorP2PManager.kt`](file:///home/thomas/Projects/BG-AG/app/src/main/java/com/example/data/TorP2PManager.kt)**:
  * Внедрён протокол Happy Eyeballs v2 (RFC 8305): параллельно стартуют Direct TCP, V2Ray Reality и Tor SOCKS5.
  * Побеждает самый быстрый и стабильный транспорт с минимальным RTT (50-200 мс), исключая 30-секундные зависания UI при блокировках.

### 🔋 2.3. Удержание фона и оптимизация батареи на Android 14/15 (НОВОЕ)
* **[`SimpleXPushService.kt`](file:///home/thomas/Projects/BG-AG/app/src/main/java/com/example/service/SimpleXPushService.kt)**:
  * Полноценная Foreground Service служба с типом `specialUse` и подтипом `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` в манифесте.
  * Удержание `PARTIAL_WAKE_LOCK` и регулярный пинг (45 сек) для предотвращения разрыва NAT сотовыми операторами.
* **[`MainActivity.kt`](file:///home/thomas/Projects/BG-AG/app/src/main/java/com/example/MainActivity.kt)**:
  * Интегрирован запрос на добавление в белый список оптимизации батареи (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`), гарантирующий приём ходов в фоне.

### 🎮 2.4. Игровое ядро нард (Backgammon Core Engine)
* **`GameEngine.kt`**:
  * Классические Длинные нарды (правило головы, снятие 2 шашек при первом дубле, блокировка 6 шашек, сброс с доски).
  * Классические Короткие нарды (бар, выбивание блотов, обязательный ввод).
  * Бешеные нарды (кумулятивная прогрессия дублей).
  * Физика зариков, Жребий первого хода, Куб удвоения (Даве).

### 🎨 2.5. Мобильный Winamp AVS Visualizer & Fullscreen Screensaver
* **`AudioVisualizerController.kt`**: Аппаратный захват FFT и Waveform через `android.media.audiofx.Visualizer` + живой органический синтетический спектр.
* **`AvsPresetsPack.kt`**: 12 пресетов (Classic Cyan, Neon SuperScope, Fire Plasma, Cyberpunk Highway, Sacred Spirograph, Deep Space Warp, Quantum DNA Helix, Analogue VU Meters, Demoscene Crystal, Laser Show, Liquid Waveform, Bass Reactor Core).
* **`AvsVisualizerView.kt`**: Рендеринг на Compose Canvas (Skia/Vulkan) 60 FPS, свайпы для переключения пресетов.
* **`AvsFullscreenDialog.kt`**: Полноэкранный скринсейвер Edge-to-Edge со скрытием системных баров.

### 📻 2.6. Аудиосистема и интернет-радио
* **`RadioManager.kt`**: 21 радиостанция, плеер локальных треков SAF, стриминг Telegram-канала RadioArmageddonFM.
* **`DiceSoundPlayer.kt`**: Реалистичные звуки зариков и фишек нард.

### 🎲 2.7. Криптографический протокол Mental Dice
* **`MentalDiceEngine.kt`**: ZK Commit-Reveal протокол броска костей на HMAC-SHA256 без сервера.
* **`MentalDiceAuditDialog.kt`**: Интерактивный аудит криптографических доказательств.

### 🔐 2.8. Криптоконтейнер и хранилище ключей
* **`SecureCryptoVault.kt`**: AES-256-GCM + PBKDF2 (100,000 итераций).
* **`Bip39Helper.kt`**: 12/24 слова сид-фразы по словарю BIP-39.
* **`ErasureCodedVault.kt`**: Шардирование Рида-Соломона GF(2^8).
* **`DoubleRatchetVault.kt`**: Сквозное шифрование с Perfect Forward Secrecy.

---

## 🔥 3. СЕКТОР [HOT] (ГОРЯЧИЙ — ОПЦИОНАЛЬНОЕ РАСШИРЕНИЕ)

### 🦀 3.1. Кросс-компиляция Rust ядра `madgammon-core` под Android NDK
* **Текущее состояние**:
  * В Android-версии все правила и криптография полностью самодостаточны и реализованы на чистом Kotlin (не требуют установленного Rust toolchain для сборки APK).
  * Для хост-машин с установленным `cargo-ndk` подготовлен скрипт `scripts/build_rust_core.sh`.

---

## 🗺️ 4. ОБНОВЛЕННАЯ КАРТА КОМПОНЕНТОВ

| Модуль | Пакет / Файл | Статус | Назначение |
| :--- | :--- | :---: | :--- |
| **Игровое ядро** | `model.GameEngine` | 🧊 **[FROZEN]** | Правила Длинных, Коротких и Бешеных нард |
| **Кости Mental Dice** | `crypto.MentalDiceEngine` | 🧊 **[FROZEN]** | ZK-генерация костей без доверенного сервера |
| **AVS Контроллер** | `ui.avs.AudioVisualizerController` | 🧊 **[FROZEN]** | Аппаратный захват FFT/Waveform + синтез спектра |
| **AVS Пресеты** | `ui.avs.AvsPresetsPack` | 🧊 **[FROZEN]** | 12 аутентичных пресетов демо-сцены |
| **AVS Виджет** | `ui.avs.AvsVisualizerView` | 🧊 **[FROZEN]** | Compose Canvas 60 FPS со свайпами |
| **AVS Fullscreen** | `ui.avs.AvsFullscreenDialog` | 🧊 **[FROZEN]** | Полноэкранный скринсейвер Edge-to-Edge |
| **Интернет-радио** | `audio.RadioManager` | 🧊 **[FROZEN]** | 21 станция, воспроизведение Telegram-каналов |
| **Звуки доски** | `audio.DiceSoundPlayer` | 🧊 **[FROZEN]** | Звуки зариков и фишек нард |
| **Темы доски** | `ui.screens.board.BoardThemeSpecs` | 🧊 **[FROZEN]** | 7 арт-тем (Ван Гог, Гауди, Неон и др.) |
| **Native Tun2Socks** | `service.FoxrayVpnService` | 🧊 **[FROZEN]** | Исполнение `libhev-socks5-tunnel.so` + fallback |
| **MultiPath P2P** | `data.TorP2PManager` | 🧊 **[FROZEN]** | Happy Eyeballs v2 параллельный опрос Direct/V2Ray/Tor |
| **Фоновый Push/Sync** | `service.SimpleXPushService` | 🧊 **[FROZEN]** | Android 14+ FGS с WakeLock и Keep-Alive пингом |
| **Оптимизация батареи**| `MainActivity` | 🧊 **[FROZEN]** | Запрос на исключение из Doze Mode |
| **SimpleX SMP** | `data.SimpleXSmpClient` | 🧊 **[FROZEN]** | Двусторонний WebSocket к публичным SMP-реле |
| **Криптоконтейнер** | `crypto.SecureCryptoVault` | 🧊 **[FROZEN]** | AES-256-GCM шифрование с PBKDF2 |
| **BIP-39 Мнемоника** | `data.Bip39Helper` | 🧊 **[FROZEN]** | 12/24 слова сид-фразы кошелька |
| **Rust NDK Скрипт** | `scripts/build_rust_core.sh` | 🔥 **[HOT]** | Опциональная сборка Rust .so при наличии cargo-ndk |
