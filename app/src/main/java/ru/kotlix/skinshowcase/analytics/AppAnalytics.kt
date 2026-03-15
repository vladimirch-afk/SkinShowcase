package ru.kotlix.skinshowcase.analytics

import android.app.Application
import android.util.Log
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

/**
 * Единая точка отправки событий и ошибок в Яндекс AppMetrica.
 *
 * Ключ API: зарегистрируйте приложение на https://appmetrica.yandex.com → Настройки → Ключ API.
 * Задайте ключ при сборке: `-PAPPMETRICA_API_KEY=ваш_ключ` или в gradle.properties: APPMETRICA_API_KEY=...
 *
 * Краши: AppMetrica автоматически перехватывает необработанные исключения и отправляет отчёты.
 * Ручная отправка: [reportError] / [reportErrorWithMessage] для пойманных ошибок.
 *
 * События:
 * - app_open — вход в приложение
 * - login_attempt — попытка авторизации (method: "form" | "steam")
 * - login_success — успешная авторизация
 * - screen — переход на экран (screen_name)
 * - action_* — произвольное действие (offer_created, favorite_added, donation_screen_open и т.д.)
 * - action_donation_screen_open — пользователь открыл экран доната (Поддержать проект)
 */
object AppAnalytics {

    private const val TAG = "AppMetrica"

    private var isActive = false

    fun init(application: Application, apiKey: String) {
        if (apiKey.isBlank()) return
        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        AppMetrica.activate(application, config)
        isActive = true
        Log.d(TAG, "init: AppMetrica activated")
        reportAppOpen()
    }

    fun reportAppOpen() {
        if (!isActive) return
        Log.d(TAG, "reportEvent: app_open")
        AppMetrica.reportEvent("app_open")
    }

    /** Попытка авторизации. method: "form" — по логину/паролю, "steam" — через Steam. */
    fun reportLoginAttempt(method: String) {
        if (!isActive) return
        Log.d(TAG, "reportEvent: login_attempt method=$method")
        AppMetrica.reportEvent("login_attempt", mapOf("method" to method))
    }

    fun reportLoginSuccess() {
        if (!isActive) return
        Log.d(TAG, "reportEvent: login_success")
        AppMetrica.reportEvent("login_success")
    }

    /** Просмотр экрана. screenName — маршрут или человекочитаемое имя (home, profile, skin_detail и т.д.). */
    fun reportScreen(screenName: String) {
        if (!isActive) return
        val name = screenName.takeIf { it.isNotBlank() } ?: "unknown"
        Log.d(TAG, "reportEvent: screen screen_name=$name")
        AppMetrica.reportEvent("screen", mapOf("screen_name" to name))
    }

    /**
     * Произвольное действие в приложении.
     * @param actionName имя события (например offer_created, favorite_added, message_sent)
     * @param params опциональные параметры (не логировать чувствительные данные)
     */
    fun reportEvent(actionName: String, params: Map<String, String>? = null) {
        if (!isActive) return
        val eventName = "action_$actionName"
        if (params.isNullOrEmpty()) {
            Log.d(TAG, "reportEvent: $eventName")
            AppMetrica.reportEvent(eventName)
        } else {
            Log.d(TAG, "reportEvent: $eventName params=$params")
            AppMetrica.reportEvent(eventName, params)
        }
    }

    /** Пользователь открыл экран доната (Поддержать проект). */
    fun reportDonationScreenOpened() {
        if (!isActive) return
        Log.d(TAG, "reportEvent: action_donation_screen_open")
        AppMetrica.reportEvent("action_donation_screen_open")
    }

    /**
     * Отправить в AppMetrica пойманную ошибку (non‑fatal).
     * Краши (необработанные исключения) SDK перехватывает сам.
     */
    fun reportError(throwable: Throwable) {
        if (!isActive) return
        val message = throwable.message ?: throwable.javaClass.simpleName
        Log.w(TAG, "reportError: $message", throwable)
        AppMetrica.reportError(message, throwable)
    }

    /**
     * Отправить в AppMetrica пойманную ошибку с пояснением.
     */
    fun reportErrorWithMessage(message: String, throwable: Throwable? = null) {
        if (!isActive) return
        if (throwable != null) {
            Log.w(TAG, "reportError: $message", throwable)
            AppMetrica.reportError(message, throwable)
        } else {
            Log.w(TAG, "reportError: group=error message=$message")
            AppMetrica.reportError("error", message)
        }
    }
}
