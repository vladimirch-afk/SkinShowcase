package ru.kotlix.skinshowcase.mock

/**
 * Идентификаторы документов (PDF) для экрана «Документы» в профиле.
 * Путь к файлу в assets приложения: [getDocumentAssetPath].
 */
object MockDocuments {

    const val DOC_AGREEMENT = "agreement"
    const val DOC_INSTRUCTIONS = "instructions"

    /**
     * Возвращает путь к PDF в assets приложения (например для загрузки в PDFView).
     * Если id неизвестен, возвращает null.
     */
    fun getDocumentAssetPath(documentId: String): String? = when (documentId) {
        DOC_AGREEMENT -> "documents/agreement.pdf"
        DOC_INSTRUCTIONS -> "documents/instructions.pdf"
        else -> null
    }
}
