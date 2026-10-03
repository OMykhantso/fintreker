# 💰 FinTreker — Лабораторна №4: Offline-First, мережа (Retrofit), списки та стан

> Це **знімок проєкту після лабораторної №4** з наскрізного проєкту «Трекер витрат»
> (тема №3). Знімки Лаб 1–5 лежать у теках `labs/lab1` … `labs/lab5`, а фінальна версія (Лаб 6) — у корені репозиторію.
> Кожен знімок — окремий Gradle-проєкт: відкривайте в Android Studio саме **цю теку**.

## Що реалізовано
- Відкрите **API НБУ** через Retrofit; безпечний мапер `NbuRateDto.toEntity()` відкидає некоректні записи.
- Патерн **Offline-First / Single Source of Truth**: курси кешуються в Room, UI читає лише з бази — без інтернету додаток показує збережений курс («Офлайн: курс станом на …»).
- Помилки мережі — через `sealed interface RatesUiState` (`Loading` / `Success` / `Error`).
- Залишок бюджету в USD/EUR, **кругова діаграма** розподілу витрат (`PieChart`) і **shimmer**-плейсхолдери.

## Запуск
1. Android Studio Ladybug (2024.2) або новіша, JDK 17, Android SDK 34.
2. Відкрийте теку проєкту, дочекайтесь Gradle Sync і запустіть ▶ **Run 'app'** (minSdk 26).
3. З командного рядка: `./gradlew installDebug`.

## Ключові файли для звіту
- `app/src/main/java/com/example/fintreker/data/remote/NbuApi.kt`
- `app/src/main/java/com/example/fintreker/data/repository/RatesRepository.kt`
- `app/src/main/java/com/example/fintreker/ui/viewmodels/RatesViewModel.kt`
- `app/src/main/java/com/example/fintreker/ui/components/PieChart.kt`
- `app/src/main/java/com/example/fintreker/ui/components/Shimmer.kt`
- `app/src/main/java/com/example/fintreker/ui/screens/HomeScreen.kt`

## Далі
Лаб 5 — WorkManager та сповіщення (тека `labs/lab5`).
