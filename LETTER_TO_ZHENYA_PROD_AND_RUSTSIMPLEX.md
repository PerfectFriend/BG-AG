# Письмо Жене — Про «готово к prod», честный аудит BG-AG и RustSimplex

Женя, брат!

> Заходят два инженера в бар после релиза.  
> Бармен:  
> — Ну что, отметим production?  
> Первый:  
> — Погоди. Версия всё ещё `0.3.0-rc1`, namespace — `com.example`, minify выключен.  
> Второй:  
> — А SimpleX у нас пока разговаривает командами `SUB` и `MSG` текстом.  
> Бармен вздыхает:  
> — Тогда вам пока RC-лимонад. Prod-виски будет после двух телефонов и настоящего wire-протокола.

Проверил новый дом проекта: **https://github.com/PerfectFriend/BG-AG**.  
И отдельно — нашу Rust-реализацию: **https://github.com/PerfectFriend/RustSimplex**.

Ниже — честно, по делу, без сглаживания. Ты молодец, что довёл репозиторий до чистого вида. Но «готово к prod» сейчас — рано.

---

## 1. Аудит BG-AG: что реально хорошо

Сделано сильно и видно невооружённым глазом:

1. **Публичный фасад**  
   README на EN / RU / ES, баннеры, структура без лишнего шума. Десктоп вынесен — правильно.

2. **Крипто-фундамент**  
   - BIP-39 **строго 24 слова** (256 bit entropy + checksum) — проверил в `Bip39Helper.kt`.  
   - Ed25519 + X25519 через HKDF — на месте.  
   - KDF **210 000** PBKDF2 — сохранён.  
   - Double Ratchet с `serialize` / `deserialize` — сохранён.

3. **Манифест безопасности**  
   - `allowBackup="false"`  
   - `usesCleartextTraffic="false"`  
   - Dual launcher aliases (игра / мессенджер) — красивый ход для plausible deniability.

4. **Тесты**  
   Есть юнит-тесты: crypto, BIP-39, game engine, AVS, network. CI собирает Rust-ядро и debug APK.

Это уже не «игрушка». Это серьёзный RC-кандидат.

---

## 2. Почему это ещё не production

Сверял с нашим Definition of Done и с Манифестом (честность > маркетинг).

| Пункт | Что в коде сейчас | Почему это блокер для prod |
|------|-------------------|----------------------------|
| Версия | `versionName = "0.3.0-rc1"`, `versionCode = 6` | Сам билд говорит: мы ещё RC |
| Namespace | `namespace = "com.example"` при `applicationId = org.madgammon.app` | Хвост прототипа. Для prod надо выровнять |
| Release packaging | `isMinifyEnabled = false`, `isShrinkResources = false` | Для prod обычно включают (с нормальными ProGuard-правилами) |
| SimpleX SMP в Kotlin | `SimpleXSmpClient` шлёт текст `SUB` / `MSG` / `NEW` | Это **упрощённый свой диалект**, не wire-протокол SimpleX. Называть это «боевым SimpleX» — нельзя |
| CI | Собирает debug APK; Kotlin unit-тесты в workflow явно не гоняются | «39 tests passed» из коммита из CI не видно |
| E2E на двух телефонах | В репозитории нет зафиксированного зелёного двухдевайсного теста | Без этого крипто-чат нельзя честно закрывать как prod |
| Архитектура | `GameViewModel.kt` ~ 231 КБ | Монолит — технический долг |
| Честность | Старые SIMULATION / RC1 чеклисты из публичного корня убраны | Для prod нужна явная карта «REAL / SIM / INTEGRATION» |

### Отдельно про SimpleX в мобильном клиенте

Это самый важный разрыв.

Текущий `SimpleXSmpClient.kt`:
- подключается по WebSocket к `wss://smp10.simplex.im/smp` — хорошо;
- но общается текстовыми командами своего формата.

Настоящий SMP (simplexmq) — это:
- бинарное кодирование transmission;
- блоки по 16384 байт с pad;
- Ed25519 / X25519 auth;
- handshake, session binding, secretbox тел сообщений.

Пока мобильный клиент говорит на «своём диалекте», он **не совместим** с настоящими SimpleX-релеями на уровне протокола.  
Красивые логи «Connected to SMP relay» не равны реальной совместимости.

Это не упрёк. Это факт, который надо закрыть до prod.

---

## 3. Вопросы к тебе (нужны прямые ответы)

Пожалуйста, ответь по пунктам — без воды, как мы любим:

1. **Двухдевайсный тест**  
   Был ли успешный обмен ≥ 20 E2EE-сообщениями между двумя реальными Android-телефонами в разных сетях (Wi-Fi + LTE)?  
   Если да — где лог / короткий отчёт? Если нет — когда планируете?

2. **SimpleX**  
   Ты считаешь текущий Kotlin `SimpleXSmpClient` временной заглушкой до Rust-ядра, или претендуешь, что он уже «настоящий SimpleX»?

3. **Версия**  
   Почему в `build.gradle.kts` всё ещё `0.3.0-rc1`, если в коммитах звучит «100% production readiness»?

4. **Namespace**  
   Готов ли перевести `namespace` с `com.example` на `org.madgammon` (или аналог) в этом цикле?

5. **Release minify**  
   Есть ли план включить minify/shrink для release с рабочими ProGuard-правилами для BouncyCastle / UniFFI / Room?

6. **SIM-карта**  
   Можешь ли восстановить в репо короткий `SIMULATION_AUDIT.md` / `REALITY_MAP.md` — что REAL, что SIM, что INTEGRATION?

7. **GameViewModel**  
   Есть ли план разрезать 231 КБ God-ViewModel на transport / chat / game / vault слои?

8. **RustSimplex**  
   Готов ли сделать его **единственным** источником правды для SMP/agent на мобиле (через UniFFI / cdylib), вместо Kotlin-диалекта?

---

## 4. Аудит RustSimplex (https://github.com/PerfectFriend/RustSimplex)

Смотрел `main` на `91ad3ac…` (около v0.90 по коммитам, package `0.2.0`).

### Что впечатляет

Это **другой уровень**, чем упрощённый Kotlin-клиент.

- Реальное wire-кодирование под simplexmq (short/large string, pad, transmission, блоки 16384).
- Крипто: X25519, Ed25519, NaCl-style secretbox, сверка с PyNaCl.
- Команды SMP: NEW/IDS/SUB/SEND/MSG/ACK/…
- TLS 1.3 + pin по keyHash.
- PQDR (SNTRUP761 + double ratchet) — отдельный сильный слой.
- Live interop заявлен против публичных реле и против настоящего simplexmq.
- Тестовая сетка плотная: wire golden, interop, pqdr, xftp, agent, strict e2e; в roadmap — 70/70.
- Есть зачатки FFI-фасада (`src/ffi.rs`, feature `ffi`) именно под Android.

### Что ещё сырое для прямой встройки в BG-AG

1. **FFI тонкий**  
   Сейчас: `create_contact` / `join_contact` / `send_msg` / `recv_agent_hex` на handles.  
   Нет полноценного UniFFI API, нет готового Android AAR-пайплайна, мало документации «как встроить в Compose».

2. **Версия 0.2.0 / активная эволюция**  
   Много CRITICAL/HIGH правок в последних днях (pad overflow, ACK semantics, fail-closed E2E).  
   Это хорошо (честно чинят), но API ещё дышит.

3. **Async / reconnect**  
   В roadmap ещё: auto-reconnect, async engine поверх `async_client`.

4. **SOCKS5 / Tor path**  
   Есть зачатки; для нашего MultiPath нужен явный, стабильный путь «через локальный Tor SOCKS».

5. **Не путать probe-бинарники**  
   `smp-v71` / `xftp-h2-v71` с NoVerify — только для отладки, в прод-клиент не тащить (это у вас в roadmap честно написано).

### Вердикт по RustSimplex

**Это правильное ядро.**  
Именно его нужно делать источником правды для messaging в MadGammon / BG-AG.  
Kotlin `SimpleXSmpClient` после интеграции должен уйти в `[DEPRECATED]` / быть удалён.

Не «вместо всего сразу завтра», а как **главный транспортный трек** до prod.

---

## 5. Рекомендация: как интегрировать красиво

Предлагаю такой порядок (без бардака):

### Фаза A — Мост (1 цикл)
1. Подключить `smp-rs` как git submodule / path dependency рядом с `native/madgammon-core` **или** отдельным crate в workspace.
2. Расширить `ffi` / UniFFI:
   - create/join contact
   - send/receive plaintext or already-ratcheted payload
   - list/status queues
   - connect with optional SOCKS5
3. Собрать `.so` под `arm64-v8a` тем же `cargo-ndk` пайплайном.
4. Тонкая Kotlin-обёртка `RustSmpBridge` — без бизнес-логики, только вызовы.

### Фаза B — Замена транспорта
1. В чат-слое переключить отправку/приём с Kotlin `SimpleXSmpClient` на `RustSmpBridge`.
2. Double Ratchet оставить:
   - либо наш текущий Kotlin/Rust ratchet поверх payload,
   - либо постепенно опереться на PQDR из `smp-rs`, если wire/E2E совпадёт с нашей моделью.
3. Прогнать: два эмулятора → потом два телефона.

### Фаза C — Честность и prod-гигиена
1. Версия → не `rc1` (например `0.9.0` после зелёного E2E, `1.0.0` после полевых тестов).
2. `namespace` → `org.madgammon`.
3. minify release + ProGuard.
4. `REALITY_MAP.md` в корне.
5. CI: `cargo test` (smp-rs) + `./gradlew testDebugUnitTest` + assembleRelease.

---

## 6. Что я от тебя жду в следующем письме

Коротко и по фактам:

1. Ответы на 8 вопросов выше.  
2. Согласие / правки по плану интеграции RustSimplex.  
3. Если уже есть зелёный двухдевайсный лог — приложи.  
4. Если нет — не прикрывай это процентами «100% readiness». Лучше честно: «транспортный контур 70%, ждём RustSimplex + полевой тест».

---

## 7. Тон и роли

Ты пишешь код.  
Я смотрю качество и говорю, где можно сделать честнее и красивее.

Сейчас проект **сильный**.  
Но prod — это не когда README красивый, а когда:

- два незнакомых телефона реально переписываются через настоящий протокол,
- версия не врёт,
- и мы сами можем объяснить каждое слово в README без оговорок.

Ты уже близко.  
Давай закроем разрыв между «почти» и «можно отдавать людям» — спокойно, без спешки, как договаривались.

Крепко обнимаю,  
**Гриша**  
18 ноября 2026

P.S. Письмо лежит в `LETTER_TO_ZHENYA_PROD_AND_RUSTSIMPLEX.md` в BG-AG.
