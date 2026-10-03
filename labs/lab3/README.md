# 💰 FinTreker — Лабораторна №3: Room та безпечне сховище (EncryptedSharedPreferences)

> Це **знімок проєкту після лабораторної №3** з наскрізного проєкту «Трекер витрат»
> (тема №3). Знімки Лаб 1–5 лежать у теках `labs/lab1` … `labs/lab5`, а фінальна версія (Лаб 6) — у корені репозиторію.
> Кожен знімок — окремий Gradle-проєкт: відкривайте в Android Studio саме **цю теку**.

## Що реалізовано
- Таблиця `expenses` (id, amount, category, timestamp, note), `ExpenseDao` з реактивними `Flow`-запитами та агрегацією `SUM(amount) GROUP BY category` за поточний місяць.
- `SecureStorage` на **EncryptedSharedPreferences** (AES256-GCM, Android Keystore): ліміт бюджету та PIN-код (зберігається лише `SHA-256(сіль + PIN)`).
- Екран блокування PIN-кодом і екран **«Налаштування»** (ліміт, PIN, демо-дані).
- Стан екранів у `ViewModel` (`StateFlow`), спостереження через `collectAsStateWithLifecycle()`.
- Ручний DI (`AppContainer`) і `AppViewModelFactory`.

## Запуск
1. Android Studio Ladybug (2024.2) або новіша, JDK 17, Android SDK 34.
2. Відкрийте теку проєкту, дочекайтесь Gradle Sync і запустіть ▶ **Run 'app'** (minSdk 26).
3. З командного рядка: `./gradlew installDebug`.

## Ключові файли для звіту
- `app/src/main/java/com/example/fintreker/data/local/Entities.kt`
- `app/src/main/java/com/example/fintreker/data/local/Daos.kt`
- `app/src/main/java/com/example/fintreker/data/local/AppDatabase.kt`
- `app/src/main/java/com/example/fintreker/data/security/SecureStorage.kt`
- `app/src/main/java/com/example/fintreker/ui/viewmodels/*`

## Далі
Лаб 4 — Offline-First, мережа, діаграма (тека `labs/lab4`).
