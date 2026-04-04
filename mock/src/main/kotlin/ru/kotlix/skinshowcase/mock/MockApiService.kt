package ru.kotlix.skinshowcase.mock

import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import ru.kotlix.skinshowcase.core.network.ApiService
import ru.kotlix.skinshowcase.core.network.ItemResponseDto
import ru.kotlix.skinshowcase.core.network.SkinDto

/**
 * Мок [ApiService]: возвращает данные из [MockData.skins].
 */
class MockApiService : ApiService {

    override suspend fun getSkins(): List<SkinDto> {
        delay(MOCK_DELAY_MS)
        return MockData.skins
    }

    override suspend fun getSkinById(id: String): ItemResponseDto {
        delay(MOCK_DELAY_MS)
        val skin = MockData.skins.find { it.id == id }
            ?: throw HttpException(Response.error<SkinDto>(404, "Not found".toResponseBody("text/plain".toMediaType())))
        return ItemResponseDto(
            itemId = skin.id,
            name = skin.name,
            minPriceUsd = skin.price,
            updatedAt = null
        )
    }

    private companion object {
        const val MOCK_DELAY_MS = 150L
    }
}
