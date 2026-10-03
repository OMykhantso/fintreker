# 💰 FinTreker — Лабораторна №1: Знайомство з Android Studio та базовий UI (Compose)

> Це **знімок проєкту після лабораторної №1** з наскрізного проєкту «Трекер витрат»
> (тема №3). Знімки Лаб 1–5 лежать у теках `labs/lab1` … `labs/lab5`, а фінальна версія (Лаб 6) — у корені репозиторію.
> Кожен знімок — окремий Gradle-проєкт: відкривайте в Android Studio саме **цю теку**.

## Що реалізовано
- Екран **«Нова витрата»** на Jetpack Compose + Material 3: поле суми з `KeyboardType.Decimal`, вибір категорії через `FilterChip`, нотатка.
- Кнопка «Зберегти витрату» з валідацією: сума має бути **> 0** (кома або крапка, повідомлення про помилку під полем).
- Категорії та їхні кольори — в одному місці (`domain/Category.kt`), палітра в `ui/theme/Theme.kt`.
- Mock-дані витрат (`data/MockData.kt`) і список «Останні витрати» під формою.
- `Rules.md` — правила для AI-асистента (Agentic AI).

## Запуск
1. Android Studio Ladybug (2024.2) або новіша, JDK 17, Android SDK 34.
2. Відкрийте теку проєкту, дочекайтесь Gradle Sync і запустіть ▶ **Run 'app'** (minSdk 26).
3. З командного рядка: `./gradlew installDebug`.

## Ключові файли для звіту
- `app/src/main/java/com/example/fintreker/ui/screens/AddExpenseScreen.kt`
- `app/src/main/java/com/example/fintreker/MainActivity.kt`
- `app/src/main/java/com/example/fintreker/domain/Category.kt`
- `app/src/main/java/com/example/fintreker/domain/Money.kt`
- `app/src/main/java/com/example/fintreker/ui/theme/Theme.kt`
- `app/src/main/java/com/example/fintreker/data/MockData.kt`
- `Rules.md`

## Далі
Лаб 2 — Type-Safe Navigation (тека `labs/lab2`).
