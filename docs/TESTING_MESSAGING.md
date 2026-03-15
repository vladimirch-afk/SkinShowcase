# Как протестировать работу Messaging (чаты) с бэкендом

## 1. Запуск сервера

Сервер — проект **messaging** (Spring Boot, порт 8082).

```bash
cd /path/to/messaging
./gradlew bootRun --args='--spring.profiles.active=local'
```

Или из IDE: запустить `MessagingApplication` с VM option / program argument: `--spring.profiles.active=local`.

Убедись, что в логах есть строка вида: `Tomcat started on port(s): 8082`.

## 2. Запуск приложения на эмуляторе

- Открой проект **SkinShowcase2** в Android Studio.
- Выбери эмулятор (или создай AVD).
- Run → Run 'app'.

В приложении базовый URL уже настроен на `http://10.0.2.2:8082/api/` (хост-машина с эмулятора).

## 3. JWT для запросов к /api/chats

401 означает: сервер требует заголовок `Authorization: Bearer <JWT>`. Без токена запрос к списку чатов всегда вернёт **401 Unauthorized**.

**Как получить тестовый токен (профиль `local`):**

В проекте **messaging** добавлена утилита, которая печатает JWT с тем же секретом, что в `application-local.yml`:

```bash
cd /path/to/messaging
./gradlew generateLocalJwt
```

В выводе будет строка вида:
```
Test JWT (sub=76561198000000000, valid 24h):
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3NjU2MTE5ODAwMDAwMDAwMCIs...
```

**Вставить токен в приложение:**

В `app/build.gradle.kts` в блоке `buildTypes { debug { ... } }` замени пустую строку на этот токен (экранируй кавычки и обратные слэши в строке):

```kotlin
buildConfigField("String", "MESSAGING_DEBUG_TOKEN", "\"eyJhbGciOiJIUzI1NiJ9.eyJzdWIi....\"")
```

Пересобери приложение. В `SkinsShowcaseApplication` токен подставится в заголовок для всех запросов через `RetrofitProvider`. Токен действует 24 часа, после чего снова запусти `generateLocalJwt`.

## 4. Проверка в приложении

1. Открой экран **Сообщения / Чаты** (вкладка с чатами в нижней навигации).
2. Нажми кнопку обновления списка чатов (если есть) или дождись автоматической загрузки.
3. В Logcat (фильтр `okhttp` или `ru.kotlix.skinshowcase`) должны быть запросы:
   - `GET http://10.0.2.2:8082/api/chats`
   - ответ 200 и список чатов в UI или 401 при отсутствии/неверном JWT.

## 5. Проверка с хоста (без приложения)

Убедиться, что сервер отдаёт данные:

```bash
# без токена — ожидаем 401
curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/api/chats

# с токеном (подставь свой JWT)
curl -s -H "Authorization: Bearer YOUR_JWT" http://localhost:8082/api/chats
```

Должен вернуться JSON-массив (например `[]` или список чатов).

## Реальное устройство

На реальном устройстве `10.0.2.2` недоступен. Нужно либо:

- в `ApiConfig.BASE_URL` подставить IP твоего компьютера в локальной сети, например `http://192.168.1.100:8082/api/`,  
либо  
- использовать `adb reverse tcp:8082 tcp:8082` и в приложении оставить `http://127.0.0.1:8082/api/` (и разрешить cleartext для 127.0.0.1 в `network_security_config.xml`, это уже сделано).
