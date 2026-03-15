# Skins Showcase (Android)

Android-приложение на **Kotlin** и **Jetpack Compose** с модульной архитектурой.

## Модули

| Модуль | Назначение |
|--------|------------|
| **app** | Точка входа: `Application`, `MainActivity`, навигация (navigation-compose), нижняя панель, экраны. Зависит от `core` и `designsystem`. |
| **core** | Сетевой слой (Retrofit, OkHttp, Gson), Room (DAO, Entity, AppDatabase), репозитории, use-case, `BaseViewModel`, `Result`, `AppDispatchers`. |
| **designsystem** | Дизайн-система: тема (Color, Typography, `SkinShowcaseTheme`), UI-компоненты (например, `PrimaryButton`). Зависит от `core`. |

## Навигация

- **Нижняя панель (Bottom Bar)** показывается только на табах: Главная, Скины, Избранное, Профиль. На overlay-экранах панель скрыта (`AnimatedVisibility(visible = isTabRoute)`).
- **Табы (под панелью):** `HomeScreen`, `SkinsScreen`, `FavoritesScreen`, `ProfileScreen`.
- **Overlay-экраны (над табами, без панели):** `SkinDetailScreen` (`skin/{skinId}`), `SettingsScreen`, `AboutScreen`. Переход: из Профиля — Настройки/О приложении; из Скинов/Избранного — детали скина по `onSkinClick(skinId)`.

## Структура пакетов

```
app/
  navigation/           → NavRoutes, BottomNavItems, SkinsShowcaseNavHost
  screens/             → home, skins, favorites, profile, skindetail, settings, about
core/
  network/              → ApiConfig, RetrofitProvider, ApiService, SkinDto
  database/            → AppDatabase, DatabaseProvider, entity, dao
  domain/              → Skin, mapper
  repository/          → SkinsRepository
  usecase/             → GetSkinsUseCase, ObserveFavoritesUseCase, ToggleFavoriteUseCase
designsystem/
  theme/               → Color, Type, Theme
  components/          → PrimaryButton, …
```

## Сборка

- **Version catalog:** `gradle/libs.versions.toml`
- **Общий конфиг:** `buildSrc/src/main/kotlin/Config.kt` (minSdk, compileSdk, versionCode, versionName)

```bash
./gradlew :app:assembleDebug
./gradlew :core:assemble
./gradlew :designsystem:assemble
```

## Добавление feature-модулей

1. В `settings.gradle.kts`: `include(":featurename")`
2. Создать `featurename/build.gradle.kts` с зависимостями `project(":core")`, `project(":designsystem")` при необходимости
3. Подключать в `app` через `implementation(project(":featurename"))`
