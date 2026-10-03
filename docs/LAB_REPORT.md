# Пам'ятка для звітів: що здавати по кожній лабораторній

Тема: **№3 «Трекер витрат» (Expense Tracker)**. Шляхи вказано від кореня репозиторію,
`src/main/…` = `app/src/main/java/com/example/fintreker/…`.

## Лаб 1 — Знайомство з Android Studio та базовий UI
- **Скріншоти:** екран «Нова витрата» до введення та після (сума, обрана категорія, повідомлення про помилку при сумі ≤ 0).
- **Лістинги:** `ui/screens/AddExpenseScreen.kt`, `MainActivity.kt`, [`Rules.md`](../Rules.md).
- **AI-завдання:** палітра фінансових категорій → `domain/Category.kt` (+ `ui/theme/Theme.kt`); Mock-дані витрат → `data/DemoData.kt`.

## Лаб 2 — Type-Safe Navigation
- **Скріншоти:** Огляд → Нова витрата → Історія (перехід вперед із параметром, повернення назад); Bottom Navigation Bar.
- **Лістинги:** `navigation/NavigationRoutes.kt`, `navigation/AppNavigation.kt`.
- **AI-завдання:** Bottom Navigation Bar з анімованими переходами → `AppNavigation.kt` (`NavigationBar`, `AnimatedVisibility`, `slideInHorizontally`/`fadeIn`).
  Типізований маршрут із параметром: `ExpenseHistoryRoute(category: String?)`.

## Лаб 3 — Room та безпечне сховище
- **Скріншоти:** історія витрат із БД; екран «Налаштування» зі встановленим лімітом і PIN; екран введення PIN.
- **Лістинги:** `data/local/Entities.kt`, `data/local/Daos.kt`, `data/local/AppDatabase.kt`, `data/security/SecureStorage.kt`.
- **AI-завдання:** агрегація `SUM(amount) GROUP BY category` за поточний місяць → `ExpenseDao.observeCategoryTotals`.
  Приклад міграції `Migration(1, 2)` — у README.

## Лаб 4 — Offline-First, мережа, стан
- **Скріншоти:** головний екран з курсом НБУ онлайн **і** з вимкненим інтернетом (рядок «Офлайн: курс станом на …»); екран «Аналітика» з діаграмою.
- **Лістинги:** `data/remote/NbuApi.kt`, `data/repository/RatesRepository.kt`, `ui/viewmodels/RatesViewModel.kt`, `ui/screens/HomeScreen.kt`.
- **AI-завдання:** кругова діаграма → `ui/components/PieChart.kt`; безпечний Mapper `NbuRateDto.toEntity()` → `NbuApi.kt`; Shimmer → `ui/components/Shimmer.kt`.

## Лаб 5 — WorkManager та сповіщення
- **Скріншоти:** Push-сповіщення «Не забудьте зафіксувати сьогоднішні витрати!» (кнопка «Тестове нагадування зараз» у Налаштуваннях); екран «Нова витрата», відкритий кліком по сповіщенню.
- **Лістинги:** `work/ReminderWorker.kt`, `work/WorkReminderController.kt`, `work/NotificationHelper.kt`, `work/BootReceiver.kt`, `AndroidManifest.xml`.
- **AI-завдання:** Notification Channel (`IMPORTANCE_HIGH`) і `PendingIntent` (`FLAG_ACTIVITY_CLEAR_TASK`, `FLAG_IMMUTABLE`) → `NotificationHelper.kt`;
  точне планування на 20:00 та відновлення після перезавантаження (`RECEIVE_BOOT_COMPLETED`) → `domain/TimeRules.kt` (`millisUntilNext`) + `BootReceiver.kt`.

## Лаб 6 — AI-інструменти та тестування
- **Скріншот:** успішний запуск `./gradlew testDebugUnitTest` (Android Studio → Run → всі тести зелені) або звіт `app/build/reports/tests/testDebugUnitTest/index.html`.
  Той самий звіт CI публікує як артефакт `unit-test-report` у вкладці Actions.
- **Лістинги:** `app/src/test/java/com/example/fintreker/viewmodels/*Test.kt`, `domain/*Test.kt`, `data/*Test.kt`.
- **Фіча:** пагінація (Infinite Scroll) → `ui/viewmodels/HistoryViewModel.kt` + `ui/screens/HistoryScreen.kt`.
- **Згенерований README:** [`README.md`](../README.md).
- **AI-завдання (edge cases):** від'ємні суми, нульовий курс, відсутність інтернету — `MoneyTest`, `RatesRepositoryTest`, `RatesViewModelTest`.
