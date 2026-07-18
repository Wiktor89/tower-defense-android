package ru.games.platform.data.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val gson = Gson()

    fun create(baseUrl: String): GamesApi {
        val root = baseUrl.trim().trimEnd('/') + "/"
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(root)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GamesApi::class.java)
    }

    fun errorMessage(err: Throwable): String {
        if (err is HttpException) {
            val raw = err.response()?.errorBody()?.string()
            if (!raw.isNullOrBlank()) {
                return try {
                    gson.fromJson(raw, JsonObject::class.java)?.get("error")?.asString ?: raw
                } catch (_: Exception) {
                    raw
                }
            }
            return "Ошибка сервера: ${err.code()}"
        }
        return err.message ?: "Сетевая ошибка"
    }
}
