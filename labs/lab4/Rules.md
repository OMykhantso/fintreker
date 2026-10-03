# Rules for FinTreker Android Development

Правила для AI-асистента (Agentic AI) — діють для всього коду проєкту.

## Базові (Лаб 1)
1. Завжди використовуй Jetpack Compose для UI. Жодного XML layout.
2. Використовуй Material 3 (`androidx.compose.material3`).
3. Мова: Kotlin. Весь текст інтерфейсу — українською.
4. Фінансові категорії та їхні кольори — лише з `Category` (єдине джерело правди).

## Навігація (Лаб 2)
5. Навігація: тільки Type-Safe Navigation Compose 2.8+ з `@Serializable` маршрутами. Жодних рядкових маршрутів.
6. Бізнес-логіка (баланс, валідація, частки діаграми) — чисті функції в пакеті `domain` без Android-залежностей.

## Дані та безпека (Лаб 3)
7. БД: Room (KSP). Реактивні запити повертають `Flow`, записи — `suspend`.
8. PIN-код та ліміт бюджету зберігати ТІЛЬКИ в `EncryptedSharedPreferences` (AES256-GCM, Android Keystore). PIN — лише як SHA-256 хеш із сіллю.
9. У Compose спостерігати стан тільки через `collectAsStateWithLifecycle()`, ніколи через `collectAsState()`.
10. Стан екрана тримай у `ViewModel` (`StateFlow`); UI лише відображає стан і викликає методи ViewModel.

## Мережа (Лаб 4)
11. Мережа: Retrofit + Gson (відкрите API НБУ). Помилки мережі не валять додаток.
12. Offline-First (Single Source of Truth): UI читає дані лише з Room через `Flow`; мережа лише оновлює кеш.
