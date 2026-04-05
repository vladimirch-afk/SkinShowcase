package ru.kotlix.skinshowcase.core.network

/**
 * Базовый URL **api-gateway** (Spring Cloud Gateway) для HTTP-клиента.
 *
 * Маршруты gateway (см. `api-gateway/.../application.yml`):
 * - префикс `/auth/` → auth
 * - префиксы `/api/v1/items/`, `/api/v1/admin/` → items
 * - префикс `/api/chats/` → messaging
 * - префиксы `/api/v1/inventory/`, `/api/v1/market/` → steam-gateway
 * - префикс `/api/v1/trades/` → trades
 *
 * Retrofit задаёт пути **без ведущего слэша** относительно [BASE_URL] (итог: `{BASE_URL}auth/me`, `{BASE_URL}api/chats`, …).
 *
 * **Важно для Steam-логина:** на auth-сервисе `AUTH_STEAM_REALM` и `AUTH_STEAM_RETURN_TO` должны указывать
 * **тот же** scheme + host + port, что и этот URL, иначе после Steam на устройстве будет `net::ERR_CONNECTION_REFUSED`.
 *
 * - Эмулятор → обычно `http://10.0.2.2:8080/` (10.0.2.2 = localhost хоста с Docker).
 * - Физическое устройство → `http://<LAN-IP>:8080/` и те же значения на сервере.
 */
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/"
    const val CONNECT_TIMEOUT_SEC = 15L
    const val READ_TIMEOUT_SEC = 30L
    const val WRITE_TIMEOUT_SEC = 30L
}