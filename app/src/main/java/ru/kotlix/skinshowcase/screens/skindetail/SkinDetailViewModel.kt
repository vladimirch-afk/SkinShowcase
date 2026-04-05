package ru.kotlix.skinshowcase.screens.skindetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.analytics.AppAnalytics
import ru.kotlix.skinshowcase.core.network.RetrofitProvider
import ru.kotlix.skinshowcase.core.network.SkinsProvider
import ru.kotlix.skinshowcase.core.network.auth.AuthApiService
import ru.kotlix.skinshowcase.core.network.auth.ReportUserRequestDto
import ru.kotlix.skinshowcase.core.network.bestApiMessage
import ru.kotlix.skinshowcase.data.ProfileDataProvider
import ru.kotlix.skinshowcase.navigation.NavRoutes

class SkinDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val skinId: String = savedStateHandle.get<String>(NavRoutes.SKIN_DETAIL_ID_ARG) ?: ""
    private val isOwnOffer: Boolean = savedStateHandle.get<Boolean>(NavRoutes.SKIN_DETAIL_IS_OWN_OFFER_ARG) ?: false
    private val isCreatingOffer: Boolean = savedStateHandle.get<Boolean>(NavRoutes.SKIN_DETAIL_IS_CREATING_OFFER_ARG) ?: false
    private val offerOwnerSteamId: String? = savedStateHandle
        .get<String>(NavRoutes.SKIN_DETAIL_OFFER_OWNER_STEAM_ID_ARG)
        ?.trim()
        ?.takeIf { it.isNotEmpty() && it != "_" }
    /** Из инвентаря при создании оффера (если GET /items/{id} не вернул карточку). */
    private val navInventoryAssetId: String? = savedStateHandle
        .get<String>(NavRoutes.SKIN_DETAIL_INVENTORY_ASSET_ID_ARG)
        ?.trim()
        ?.takeIf { it.isNotEmpty() && it != "_" }

    private val _uiState = MutableStateFlow(
        SkinDetailUiState(
            isCreatingOffer = isCreatingOffer,
            isTradeFeedOffer = offerOwnerSteamId != null
        )
    )
    val uiState: StateFlow<SkinDetailUiState> = _uiState.asStateFlow()

    init {
        loadSkin(skinId)
        when {
            isOwnOffer -> loadSeller()
            offerOwnerSteamId != null -> loadOfferOwner(offerOwnerSteamId)
            else -> loadOtherSeller()
        }
    }

    private fun loadSeller() {
        viewModelScope.launch {
            val profile = ProfileDataProvider.getProfileState()
            _uiState.update {
                it.copy(
                    sellerNickname = profile.steamNickname.ifBlank { null },
                    sellerSteamId = profile.steamId
                )
            }
        }
    }

    private fun loadOtherSeller() {
        viewModelScope.launch {
            val seller = ProfileDataProvider.getSellerForSkin(skinId)
            if (seller != null) {
                _uiState.update {
                    it.copy(
                        sellerNickname = seller.nickname,
                        sellerSteamId = seller.steamId,
                        sellerTradeLink = seller.tradeLink
                    )
                }
            }
        }
    }

    private fun loadOfferOwner(steamId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    sellerSteamId = steamId,
                    sellerNickname = null,
                    sellerTradeLink = null
                )
            }
            val link = ProfileDataProvider.getTradeLinkForSteamId(steamId)
            val nickname = ProfileDataProvider.getCounterpartyNicknameFromChats(steamId)
            _uiState.update {
                it.copy(
                    sellerTradeLink = link,
                    sellerNickname = nickname
                )
            }
        }
    }

    private fun loadSkin(id: String) {
        if (id.isBlank()) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "Не указан id скина")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val ownerForInventory = when {
                offerOwnerSteamId != null -> offerOwnerSteamId
                isCreatingOffer || isOwnOffer -> ProfileDataProvider.resolveCurrentUserSteamId()
                else -> null
            }
            runCatching {
                SkinsProvider.repository.getSkinByIdFromApi(
                    id = id,
                    inventoryOwnerSteamId = ownerForInventory,
                    inventoryAssetId = navInventoryAssetId,
                    offerOwnerSteamId = offerOwnerSteamId
                )
            }.fold(
                onSuccess = { skin ->
                    _uiState.update {
                        it.copy(
                            skin = skin,
                            isLoading = false,
                            errorMessage = if (skin == null) "Скин не найден" else null
                        )
                    }
                    if (skin != null && offerOwnerSteamId != null) {
                        viewModelScope.launch {
                            val nick = ProfileDataProvider.getCounterpartyNicknameFromChats(offerOwnerSteamId)
                            if (!nick.isNullOrBlank()) {
                                _uiState.update { cur ->
                                    if (cur.sellerNickname.isNullOrBlank()) {
                                        cur.copy(sellerNickname = nick)
                                    } else {
                                        cur
                                    }
                                }
                            }
                        }
                    }
                },
                onFailure = { e ->
                    AppAnalytics.reportErrorWithMessage("loadSkin($id)", e)
                    _uiState.update {
                        it.copy(
                            skin = null,
                            isLoading = false,
                            errorMessage = e.bestApiMessage()
                        )
                    }
                }
            )
        }
    }

    fun getSkinId(): String = skinId

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearOfferCreateError() {
        _uiState.update { it.copy(offerCreateError = null) }
    }

    fun createOffer() {
        val loaded = _uiState.value.skin
        val classId = loaded?.id?.takeIf { it.isNotBlank() } ?: skinId.takeIf { it.isNotBlank() }
        val assetId = loaded?.inventoryAssetId?.takeIf { it.isNotBlank() } ?: navInventoryAssetId
        if (classId.isNullOrBlank() && assetId.isNullOrBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingOffer = true, offerCreateError = null) }
            val result = ProfileDataProvider.createOffer(
                classId = classId.orEmpty(),
                inventoryAssetId = assetId
            )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmittingOffer = false,
                            navigateToMyOffers = true,
                            offerCreateError = null
                        )
                    }
                    AppAnalytics.reportEvent(
                        "offer_created",
                        mapOf("skin_id" to (classId ?: assetId ?: ""))
                    )
                },
                onFailure = { e ->
                    AppAnalytics.reportErrorWithMessage("createOffer", e)
                    _uiState.update {
                        it.copy(
                            isSubmittingOffer = false,
                            navigateToMyOffers = false,
                            offerCreateError = e.bestApiMessage()
                        )
                    }
                }
            )
        }
    }

    fun clearNavigateToMyOffers() {
        _uiState.update { it.copy(navigateToMyOffers = false) }
    }

    fun reportSeller(reason: String, details: String?, onComplete: (errorMessage: String?) -> Unit) {
        val steamId = _uiState.value.sellerSteamId?.trim()?.takeIf { it.length == 17 }
        if (steamId == null) {
            onComplete("Пользователь не указан")
            return
        }
        val r = reason.trim()
        if (r.isEmpty()) {
            onComplete("Укажите причину")
            return
        }
        viewModelScope.launch {
            val err = runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitProvider.create(AuthApiService::class.java).reportUser(
                        steamId,
                        ReportUserRequestDto(
                            reason = r,
                            details = details?.trim()?.takeIf { it.isNotEmpty() }
                        )
                    )
                }
            }.exceptionOrNull()
            onComplete(err?.bestApiMessage())
            if (err == null) {
                AppAnalytics.reportEvent("user_report", mapOf("reported_steam_id" to steamId))
            }
        }
    }

    // --- данные-заглушки (отключены, загрузка через api-gateway) ---
    // private fun sampleSkin(id: String): Skin = Skin(...)
}
