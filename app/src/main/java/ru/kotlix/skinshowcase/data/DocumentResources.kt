package ru.kotlix.skinshowcase.data

/**
 * Идентификаторы и пути к документам (PDF) в assets приложения.
 * Используется экраном «Документы» в профиле вместо моков.
 */
object DocumentResources {

    const val DOC_AGREEMENT = "agreement"
    const val DOC_INSTRUCTIONS = "instructions"

    /**
     * Путь к PDF в assets (например для PDFView).
     * Если id неизвестен, возвращает null.
     */
    fun getDocumentAssetPath(documentId: String): String? = when (documentId) {
        DOC_AGREEMENT -> "documents/agreement.pdf"
        DOC_INSTRUCTIONS -> "documents/instructions.pdf"
        else -> null
    }
}
