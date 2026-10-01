# 💰 FinTreker — Трекер витрат (Android, Kotlin + Jetpack Compose)

Наскрізний навчальний проєкт з 6 лабораторних робіт (тема «Трекер витрат»).

## Можливості
- Додавання витрат: сума (`KeyboardType.Decimal`, валідація > 0), категорії (`FilterChip`), нотатка.
- Баланс: залишок місячного бюджету, прогрес, попередження про перевищення ліміту.
- Конвертація залишку в USD/EUR за курсом НБУ (працює офлайн з кешу Room).
- Аналітика: кругова діаграма за категоріями, історія з фільтром, Infinite Scroll (пагінація).
- Безпека: PIN-код (SHA-256) і ліміт бюджету в `EncryptedSharedPreferences` (AES256-GCM).
- Щоденне нагадування о 20:00 (WorkManager), клік відкриває екран додавання витрати (Deep Link), розклад відновлюється після перезавантаження.

## Лабораторні → код
| Лаб | Тема | Де |
|---|---|---|
| 1 | Material 3 UI, `Rules.md` | `ui/screens/Screens.kt` (AddExpenseScreen), `ui/theme`, `Rules.md` |
| 2 | Type-Safe Navigation | `navigation/Navigation.kt` (`HomeRoute`, `AddExpenseRoute`, `ExpenseHistoryRoute(category)`, `SettingsRoute`), Bottom Bar, анімації |
| 3 | Room + SecureStorage | `data/local/Database.kt` (агрегація `SUM ... GROUP BY category`), `data/security/SecureStorage.kt` |
| 4 | Offline-First, Retrofit | `data/remote/NbuApi.kt`, `data/repository/Repositories.kt`, `ui/viewmodels` |
| 5 | WorkManager, сповіщення | `work/Work.kt`, `AndroidManifest.xml` (`RECEIVE_BOOT_COMPLETED`) |
| 6 | Тести, пагінація | `app/src/test`, `HistoryViewModel.loadMore()` |

## Архітектура (MVVM, Single Source of Truth)
```
NBU API (Retrofit) ──refresh()──▶ Room (rates, expenses) ──Flow──▶ Repository ──▶ ViewModel (StateFlow) ──▶ Compose UI
                                        ▲                                                │
                                        └──────────── suspend insert/delete ◀────────────┘
domain/  — чисті функції (баланс, конвертація, валідація, розклад) без Android-залежностей
```

## Запуск
1. Відкрийте папку в Android Studio (Hedgehog+ / JDK 17), дочекайтесь Gradle Sync.
2. Запустіть на емуляторі/пристрої (minSdk 26).
3. У Налаштуваннях можна додати демо-дані та надіслати тестове нагадування.

Тести: `./gradlew testDebugUnitTest` (JUnit4 + MockK + `StandardTestDispatcher`).

## Міграція Room (приклад для Лаб 3, якщо додати `note`-подібне поле)
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN merchant TEXT")
    }
}
// Room.databaseBuilder(...).addMigrations(MIGRATION_1_2)
```

> Примітка: проєкт написано без локальної збірки (немає Android SDK у середовищі генерації) — при першому Sync можливі дрібні правки версій.
