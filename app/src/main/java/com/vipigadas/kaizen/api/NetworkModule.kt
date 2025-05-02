package com.vipigadas.kaizen.di

import com.vipigadas.kaizen.BuildConfig
import com.vipigadas.kaizen.api.SportsApiService
import com.vipigadas.kaizen.api.SportsApiServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Singleton
import kotlin.math.pow

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Constants for network timeouts
    private const val CONNECTION_TIMEOUT = 15_000L // 15 seconds
    private const val REQUEST_TIMEOUT = 15_000L // 15 seconds
    private const val SOCKET_TIMEOUT = 15_000L // 15 seconds

    // Base URL constants
    private const val BASE_URL = "https://ios-kaizen.github.io"

    /**
     * Provides Kotlinx.Serialization JSON instance with configured options
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        prettyPrint = BuildConfig.DEBUG
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        allowSpecialFloatingPointValues = true
        allowStructuredMapKeys = true
        explicitNulls = false
    }

    /**
     * Provides HttpClient with all necessary configurations and plugins
     */
    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient {
        return HttpClient(Android) {
            // Engine configuration
            engine {
                connectTimeout = CONNECTION_TIMEOUT.toInt()
                socketTimeout = SOCKET_TIMEOUT.toInt()
            }

            // Timeouts
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT
                connectTimeoutMillis = CONNECTION_TIMEOUT
                socketTimeoutMillis = SOCKET_TIMEOUT
            }

            // Logging (only in debug builds)
            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Timber.tag("KtorClient").d(message)
                        }
                    }
                    level = LogLevel.ALL
                }
            }

            // Retry failed requests
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
                retryIf { _, response ->
                    !response.status.isSuccess()
                }
                delayMillis { retry ->
                    // Exponential backoff with jitter
                    val base = 1000L * (2.0.pow(retry.toDouble())).toLong()
                    (base * (0.5 + Math.random() * 0.5)).toLong()
                }
            }

            // Content negotiation for automatic serialization/deserialization
            install(ContentNegotiation) {
                json(json)
            }

            // Set default request parameters
            install(DefaultRequest) {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                header("X-Client-Platform", "Android")
                header("X-App-Version", BuildConfig.VERSION_NAME)
            }

            // HTTP Caching
            install(HttpCache)

            // Cookie support
            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
            }

            // Request and response observer for analytics or debugging
            install(ResponseObserver) {
                onResponse { response ->
                    if (BuildConfig.DEBUG) {
                        Timber.tag("KtorResponse").d("HTTP status: ${response.status.value}")
                    }
                }
            }

            // Authentication if needed (configured for future use)
            install(Auth) {
                bearer {
                    // Implement token loading logic as needed
                    loadTokens {
                        // This would come from your secure storage
                        // For now, returning null (no authentication)
                        null
                    }

                    refreshTokens {
                        // Implement token refresh logic
                        // For the assignment, this can be a placeholder
                        BearerTokens("", "")
                    }
                }
            }

            // Handle exceptions
            expectSuccess = true

            HttpResponseValidator {
                validateResponse { response ->
                    val statusCode = response.status.value

                    when (statusCode) {
                        in 300..399 -> throw RedirectResponseException(
                            response,
                            "Redirect response: ${response.status}"
                        )

                        in 400..499 -> throw ClientRequestException(
                            response,
                            "Client request error: ${response.status}"
                        )

                        in 500..599 -> throw ServerResponseException(
                            response,
                            "Server error: ${response.status}"
                        )
                    }

                    if (statusCode >= 600) {
                        throw ResponseException(
                            response,
                            "Unexpected status code: ${response.status}"
                        )
                    }
                }

                handleResponseExceptionWithRequest { cause, request ->
                    Timber.e(cause, "Exception during request: ${request.url}")
                }
            }
        }
    }

    /**
     * Provides SportsApiService implementation
     */
    @Provides
    @Singleton
    fun provideSportsApiService(client: HttpClient): SportsApiService {
        return SportsApiServiceImpl(client)
    }
}