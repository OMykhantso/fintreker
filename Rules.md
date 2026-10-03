# Rules for FinTreker Android Development

Правила для AI-асистента (Agentic AI) — діють для всього коду проєкту.

## Базові (Лаб 1)
1. Завжди використовуй Jetpack Compose для UI. Жодного XML layout.
2. Використовуй Material 3 (`androidx.compose.material3`).
3. Мова: Kotlin. Весь текст інтерфейсу — українською.

## Архітектура (Лаб 2–4, 6)
4. Використовуй архітектурний патерн MVVM з односпрямованим потоком даних (UDF): `Repository` → `ViewModel` (`StateFlow`) → Compose UI. UI ніколи не змінює стан напряму — лише викликає методи ViewModel.
5. Обробляй помилки мережі через `sealed class`/`sealed interface` UiState (`Loading` / `Success` / `Error`). Мережеві винятки не можуть «валити» додаток: репозиторій повертає результат, а ViewModel перетворює його на UiState.
6. Offline-First (Single Source of Truth): UI читає дані лише з Room через `Flow`; мережа лише оновлює кеш.
7. Навігація: тільки Type-Safe Navigation Compose 2.8+ з `@Serializable` маршрутами (`navigation/NavigationRoutes.kt`). Жодних рядкових маршрутів.
8. Бізнес-логіка (баланс, конвертація, валідація, розклад) — чисті функції в пакеті `domain` без Android-залежностей.
9. Фінансові категорії та їхні кольори — лише з `Category` (єдине джерело правди).

## Дані та безпека (Лаб 3)
10. БД: Room (KSP). Реактивні запити повертають `Flow`, записи — `suspend`.
11. PIN-код та ліміт бюджету зберігати ТІЛЬКИ в `EncryptedSharedPreferences` (AES256-GCM, Android Keystore). PIN — лише як SHA-256 хеш із сіллю, ніколи у відкритому вигляді.
12. У Compose спостерігати стан тільки через `collectAsStateWithLifecycle()`, ніколи через `collectAsState()`.

## Фонова робота (Лаб 5)
13. Фонові задачі — тільки WorkManager (`CoroutineWorker`). Сповіщення — через `NotificationChannel` (`IMPORTANCE_HIGH`) і `PendingIntent` з `FLAG_IMMUTABLE`. Для Android 13+ запитувати `POST_NOTIFICATIONS`.

## Тестування (Лаб 6)
14. Завжди пиши Unit-тести для кожної ViewModel, використовуючи MockK (`mockk`, `coEvery`, `coVerify`).
15. Корутини в тестах: `StandardTestDispatcher` + `Dispatchers.setMain` (`MainDispatcherRule`) + `runTest`. `StateFlow` із `stateIn(WhileSubscribed)` перед перевіркою підписувати через `collectInBackground`.
16. Для кожної функції `domain` тестуй edge cases: від'ємні та нульові суми, нульовий курс, відсутність інтернету, порожні дані.
17. Нова фіча не вважається завершеною, доки `./gradlew testDebugUnitTest` не проходить зеленим.
