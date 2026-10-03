# Rules for FinTreker Android Development

Правила для AI-асистента (Agentic AI) — діють для всього коду проєкту.

## Базові (Лаб 1)
1. Завжди використовуй Jetpack Compose для UI. Жодного XML layout.
2. Використовуй Material 3 (`androidx.compose.material3`).
3. Мова: Kotlin. Весь текст інтерфейсу — українською.
4. Фінансові категорії та їхні кольори — лише з `Category` (єдине джерело правди).

## Навігація (Лаб 2)
5. Навігація: тільки Type-Safe Navigation Compose 2.8+ з `@Serializable` маршрутами (`navigation/NavigationRoutes.kt`). Жодних рядкових маршрутів.
6. Бізнес-логіка (баланс, валідація, частки діаграми) — чисті функції в пакеті `domain` без Android-залежностей.
