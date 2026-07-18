package ru.games.platform.data.api

data class CaptchaChallenge(
    val id: String,
    val background: String,
    val piece: String,
    val pieceY: Int,
    val trackWidth: Int,
    val pieceWidth: Int,
    val imageHeight: Int,
)

data class User(
    val id: Int,
    val login: String,
    val role: String? = null,
    val grade: Int? = null,
    val avatar: String? = null,
    val hasPassword: Boolean? = null,
    val createdAt: String? = null,
    val adminToken: String? = null,
)

data class AuthRequest(
    val login: String,
    val password: String,
    val captchaId: String,
    val captchaAnswer: Int,
)

data class GameCatalogItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val icon: String? = null,
    val url: String,
    val available: Boolean = true,
    val tags: List<String> = emptyList(),
    val minGrade: Int? = null,
    val maxGrade: Int? = null,
)

data class ChallengeGameItem(
    val gameId: String,
    val title: String? = null,
    val url: String? = null,
    val position: Int = 0,
    val done: Boolean = false,
)

data class ChallengeDayProgress(
    val date: String,
    val label: String,
    val done: Boolean,
    val isReward: Boolean = false,
)

data class ChallengeWeekProgress(
    val days: List<ChallengeDayProgress> = emptyList(),
    val wins: Int = 0,
    val praise: String? = null,
)

data class StageCompletion(
    val id: Int,
    val userId: Int,
    val gameId: String,
    val stage: Int,
    val planet: String,
    val planetName: String? = null,
    val code: Int,
    val rewardRub: Int = 0,
    val verified: Boolean = false,
)

data class ChallengeStatus(
    val games: List<ChallengeGameItem> = emptyList(),
    val completed: Int = 0,
    val total: Int = 0,
    val allDone: Boolean = false,
    val reward: StageCompletion? = null,
    val week: ChallengeWeekProgress? = null,
)

data class HealthResponse(
    val status: String? = null,
)
