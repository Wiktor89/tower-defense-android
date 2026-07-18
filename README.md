# Мини-игры — Android

Нативное Android-приложение (Kotlin + Jetpack Compose). UI на Android, данные — через **Go API**.

## Адрес сервера (секрет)

В репозитории **нет** открытого IP/URL. Хранится только шифротекст:

- `config/server.url.enc` — зашифрованный URL (можно коммитить)
- `local.properties` → `server.url.key=...` — ключ (**не коммитить**, файл в `.gitignore`)

При `./gradlew assembleDebug` Gradle расшифровывает URL и зашивает в `BuildConfig.SERVER_URL`.

Перевыпустить секрет:

```bash
./scripts/seal-server-url.sh 'http://HOST:PORT'
```

Ключ также можно передать через env: `SERVER_URL_KEY=<base64>`.

## Сборка APK

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Экраны

1. Вход / регистрация (+ капча) — Go API  
2. Меню игр и «Вызов дня» — Go API  
3. Игры — WebView на том же сервере (сессия из нативного логина)
