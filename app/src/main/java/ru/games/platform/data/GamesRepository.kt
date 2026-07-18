package ru.games.platform.data

import ru.games.platform.data.api.ApiClient
import ru.games.platform.data.api.AuthRequest
import ru.games.platform.data.api.CaptchaChallenge
import ru.games.platform.data.api.ChallengeStatus
import ru.games.platform.data.api.GameCatalogItem
import ru.games.platform.data.api.GamesApi
import ru.games.platform.data.api.User

class GamesRepository(
    private val session: SessionStore,
) {
    @Volatile
    private var api: GamesApi? = null
    private var boundUrl: String = ""

    private suspend fun client(): GamesApi {
        val url = session.getBaseUrl()
        require(url.isNotBlank()) { "Сначала укажите адрес сервера" }
        if (api == null || boundUrl != url) {
            api = ApiClient.create(url)
            boundUrl = url
        }
        return api!!
    }

    fun invalidate() {
        api = null
        boundUrl = ""
    }

    suspend fun ping() {
        client().health()
    }

    suspend fun captcha(): CaptchaChallenge = client().captcha()

    suspend fun login(login: String, password: String, captchaId: String, answer: Int): User {
        val user = client().login(AuthRequest(login, password, captchaId, answer))
        session.setUser(user)
        return user
    }

    suspend fun register(login: String, password: String, captchaId: String, answer: Int): User {
        val user = client().register(AuthRequest(login, password, captchaId, answer))
        session.setUser(user)
        return user
    }

    suspend fun refreshUser(userId: Int): User {
        val user = client().user(userId)
        session.setUser(user)
        return user
    }

    suspend fun games(userId: Int): List<GameCatalogItem> = client().games(userId)

    suspend fun challenge(userId: Int): ChallengeStatus = client().challenge(userId)
}
