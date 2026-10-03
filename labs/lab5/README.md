# 💰 FinTreker — Лабораторна №5: Фонова робота та сповіщення (WorkManager)

> Це **знімок проєкту після лабораторної №5** з наскрізного проєкту «Трекер витрат»
> (тема №3). Знімки Лаб 1–5 лежать у теках `labs/lab1` … `labs/lab5`, а фінальна версія (Лаб 6) — у корені репозиторію.
> Кожен знімок — окремий Gradle-проєкт: відкривайте в Android Studio саме **цю теку**.

## Що реалізовано
- `ReminderWorker` (`CoroutineWorker`) і щоденне `PeriodicWorkRequest` о **20:00**: «Не забудьте зафіксувати сьогоднішні витрати!».
- `NotificationChannel` (`IMPORTANCE_HIGH`), `PendingIntent` (`FLAG_IMMUTABLE`, `CLEAR_TASK`) і **Deep Link** `fintreker://add` — клік по сповіщенню відкриває екран додавання витрати.
- Дозвіл `POST_NOTIFICATIONS` (Android 13+), `OneTimeWorkRequest` по кнопці «Тестове нагадування зараз».
- `BootReceiver` (`RECEIVE_BOOT_COMPLETED`) відновлює розклад після перезавантаження телефону.

## Запуск
1. Android Studio Ladybug (2024.2) або новіша, JDK 17, Android SDK 34.
2. Відкрийте теку проєкту, дочекайтесь Gradle Sync і запустіть ▶ **Run 'app'** (minSdk 26).
3. З командного рядка: `./gradlew installDebug`.

## Ключові файли для звіту
- `app/src/main/java/com/example/fintreker/work/ReminderWorker.kt`
- `app/src/main/java/com/example/fintreker/work/WorkReminderController.kt`
- `app/src/main/java/com/example/fintreker/work/NotificationHelper.kt`
- `app/src/main/java/com/example/fintreker/work/BootReceiver.kt`
- `app/src/main/java/com/example/fintreker/domain/TimeRules.kt`
- `app/src/main/AndroidManifest.xml`

## Далі
Лаб 6 — тести, пагінація, README (фінальна версія — корінь репозиторію).
