// Top-level build file: тут лише оголошення плагінів (версії — у gradle/libs.versions.toml).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // Compose compiler плагін (Kotlin 2.0+)
    alias(libs.plugins.kotlin.compose) apply false
    // Плагін серіалізації для Type-Safe Navigation Compose
    alias(libs.plugins.kotlin.serialization) apply false
    // KSP для кодогенерації Room
    alias(libs.plugins.ksp) apply false
}
