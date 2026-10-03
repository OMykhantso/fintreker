# 💰 FinTreker — Лабораторна №2: Type-Safe Navigation Compose

> Це **знімок проєкту після лабораторної №2** з наскрізного проєкту «Трекер витрат»
> (тема №3). Знімки Лаб 1–5 лежать у теках `labs/lab1` … `labs/lab5`, а фінальна версія (Лаб 6) — у корені репозиторію.
> Кожен знімок — окремий Gradle-проєкт: відкривайте в Android Studio саме **цю теку**.

## Що реалізовано
- Типізовані маршрути з `@Serializable`: `HomeRoute` → `AddExpenseRoute` → `ExpenseHistoryRoute(category: String?)`, `AnalyticsRoute` (`navigation/NavigationRoutes.kt`).
- `NavHost` з `composable<T>` та `toRoute()`; параметр `category` передається в історію без «магічних рядків».
- **Bottom Navigation Bar** (Огляд / Історія / Аналітика) зі збереженням стану вкладок.
- Анімації переходів: fade між вкладками, slide-in/out для екрана додавання витрати.
- Головний екран із балансом місячного бюджету, історія з фільтром за категорією, аналітика за категоріями.
- Дані поки що зберігаються **в пам'яті** (`data/ExpenseStore.kt`) — база даних з'явиться в Лаб 3.

## Запуск
1. Android Studio Ladybug (2024.2) або новіша, JDK 17, Android SDK 34.
2. Відкрийте теку проєкту, дочекайтесь Gradle Sync і запустіть ▶ **Run 'app'** (minSdk 26).
3. З командного рядка: `./gradlew installDebug`.

## Ключові файли для звіту
- `app/src/main/java/com/example/fintreker/navigation/NavigationRoutes.kt`
- `app/src/main/java/com/example/fintreker/navigation/AppNavigation.kt`
- `app/src/main/java/com/example/fintreker/ui/screens/HomeScreen.kt`
- `app/src/main/java/com/example/fintreker/ui/screens/HistoryScreen.kt`
- `app/src/main/java/com/example/fintreker/ui/screens/AnalyticsScreen.kt`

## Далі
Лаб 3 — Room та безпечне сховище (тека `labs/lab3`).
