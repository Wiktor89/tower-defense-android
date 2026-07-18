package ru.games.platform.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GamesApi {
    @GET("api/health")
    suspend fun health(): HealthResponse

    @GET("api/captcha")
    suspend fun captcha(): CaptchaChallenge

    @POST("api/users/login")
    suspend fun login(@Body body: AuthRequest): User

    @POST("api/users/register")
    suspend fun register(@Body body: AuthRequest): User

    @GET("api/users/{id}")
    suspend fun user(@Path("id") id: Int): User

    @GET("api/games")
    suspend fun games(@Query("userId") userId: Int): List<GameCatalogItem>

    @GET("api/challenge")
    suspend fun challenge(@Query("userId") userId: Int): ChallengeStatus
}
