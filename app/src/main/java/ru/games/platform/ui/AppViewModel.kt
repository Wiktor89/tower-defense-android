package ru.games.platform.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.games.platform.data.GamesRepository
import ru.games.platform.data.SessionStore
import ru.games.platform.data.api.ApiClient
import ru.games.platform.data.api.CaptchaChallenge
import ru.games.platform.data.api.ChallengeStatus
import ru.games.platform.data.api.GameCatalogItem
import ru.games.platform.data.api.User

data class HomeUiState(
    val loading: Boolean = false,
    val games: List<GameCatalogItem> = emptyList(),
    val challenge: ChallengeStatus? = null,
    val error: String? = null,
)

data class AuthUiState(
    val loading: Boolean = false,
    val registerMode: Boolean = false,
    val captcha: CaptchaChallenge? = null,
    val captchaAnswer: Int = 0,
    val error: String? = null,
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val session = SessionStore(app)
    private val repo = GamesRepository(session)

    val baseUrl: StateFlow<String> = session.baseUrl.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "",
    )
    val user: StateFlow<User?> = session.user.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null,
    )

    private val _home = MutableStateFlow(HomeUiState())
    val home: StateFlow<HomeUiState> = _home.asStateFlow()

    private val _auth = MutableStateFlow(AuthUiState())
    val auth: StateFlow<AuthUiState> = _auth.asStateFlow()

    private val _serverError = MutableStateFlow<String?>(null)
    val serverError: StateFlow<String?> = _serverError.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun saveServer(url: String, onOk: () -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            _serverError.value = null
            try {
                val normalized = url.trim().trimEnd('/')
                require(normalized.startsWith("http://") || normalized.startsWith("https://")) {
                    "URL должен начинаться с http:// или https://"
                }
                session.setBaseUrl(normalized)
                repo.invalidate()
                repo.ping()
                onOk()
            } catch (e: Exception) {
                _serverError.value = ApiClient.errorMessage(e)
            } finally {
                _busy.value = false
            }
        }
    }

    fun loadCaptcha() {
        viewModelScope.launch {
            _auth.update { it.copy(loading = true, error = null) }
            try {
                val ch = repo.captcha()
                _auth.update { it.copy(loading = false, captcha = ch, captchaAnswer = 0) }
            } catch (e: Exception) {
                _auth.update { it.copy(loading = false, error = ApiClient.errorMessage(e)) }
            }
        }
    }

    fun setRegisterMode(register: Boolean) {
        _auth.update { it.copy(registerMode = register, error = null) }
        loadCaptcha()
    }

    fun setCaptchaAnswer(value: Int) {
        _auth.update { it.copy(captchaAnswer = value) }
    }

    fun submitAuth(login: String, password: String, onOk: () -> Unit) {
        viewModelScope.launch {
            val st = _auth.value
            val captcha = st.captcha
            if (captcha == null) {
                _auth.update { it.copy(error = "Капча не загружена") }
                return@launch
            }
            if (login.isBlank() || password.length < 4) {
                _auth.update { it.copy(error = "Логин и пароль (мин. 4) обязательны") }
                return@launch
            }
            _auth.update { it.copy(loading = true, error = null) }
            try {
                if (st.registerMode) {
                    repo.register(login.trim(), password, captcha.id, st.captchaAnswer)
                } else {
                    repo.login(login.trim(), password, captcha.id, st.captchaAnswer)
                }
                _auth.update { it.copy(loading = false) }
                onOk()
            } catch (e: Exception) {
                _auth.update { it.copy(loading = false, error = ApiClient.errorMessage(e)) }
                loadCaptcha()
            }
        }
    }

    fun refreshHome() {
        viewModelScope.launch {
            val u = session.getUser() ?: return@launch
            _home.update { it.copy(loading = true, error = null) }
            try {
                val fresh = runCatching { repo.refreshUser(u.id) }.getOrDefault(u)
                val games = repo.games(fresh.id)
                val challenge = repo.challenge(fresh.id)
                _home.update {
                    it.copy(loading = false, games = games, challenge = challenge)
                }
            } catch (e: Exception) {
                _home.update { it.copy(loading = false, error = ApiClient.errorMessage(e)) }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            session.clearUser()
            _home.value = HomeUiState()
            onDone()
        }
    }
}
