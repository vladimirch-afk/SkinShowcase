package ru.kotlix.skinshowcase.core.network

/**
 * Базовый URL api-gateway для HTTP-клиента.
 *
 * **Важно для Steam-логина:** на auth-сервисе переменные `AUTH_STEAM_REALM` и
 * `AUTH_STEAM_RETURN_TO` должны использовать **тот же хост и порт**, что и этот URL
 * (scheme + host + port), иначе после Steam браузер откроет, например, `http://localhost:8080/...`
 * на устройстве → [net::ERR_CONNECTION_REFUSED].
 *
 * - Эмулятор Android → обычно `http://10.0.2.2:8080/` (10.0.2.2 = localhost машины с Docker).
 * - Физическое устройство → `http://<LAN-IP-компа>:8080/` и те же значения на сервере.
 */
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/"
    const val CONNECT_TIMEOUT_SEC = 15L
    const val READ_TIMEOUT_SEC = 30L
    const val WRITE_TIMEOUT_SEC = 30L
}
