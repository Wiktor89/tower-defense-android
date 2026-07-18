# Мини-игры — Android

Нативное Android-приложение (Kotlin + Jetpack Compose). UI на Android, все данные — через **Go API** проекта `tower-defense`.

## Что умеет

- Настройка адреса Go-сервера (`http://IP:8089`)
- Вход / регистрация + капча (API)
- Меню игр и «Вызов дня» (API)
- Запуск игр в WebView на том же сервере (сессия подставляется из нативного логина)

## Сборка APK

```bash
cd /AndroidStudioProjects/tower-defense-android
./gradlew assembleDebug
```

APK:

`app/build/outputs/apk/debug/app-debug.apk`

Release:

```bash
./gradlew assembleRelease
```

Или откройте папку в **Android Studio** → Build → Build APK(s).

## Перед запуском

1. На сервере: `./run.sh` (HTTP).
2. В приложении укажите URL, например `http://192.168.0.101:8089`.
3. Телефон и сервер в одной сети; в файрволе открыт порт `8089`.

## Структура

```
app/src/main/java/ru/games/platform/
  data/api/     — Retrofit-модели и клиент Go API
  data/         — SessionStore, GamesRepository
  ui/           — Compose-экраны
  MainActivity.kt
```
