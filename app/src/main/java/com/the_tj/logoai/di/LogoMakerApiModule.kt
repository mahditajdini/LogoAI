package com.the_tj.logoai.di

import com.the_tj.logoai.api.LogoMakerApiServices
import com.the_tj.logoai.utils.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.the_tj.logoai.utils.APIKEY
import com.the_tj.logoai.utils.BASE_URL
import com.the_tj.logoai.utils.NAMED_BODY
import com.the_tj.logoai.utils.NAMED_HEADER
import com.the_tj.logoai.utils.NETWORK_TIMEOUT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.http2.StreamResetException
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LogoMakerApiModule {

    @Provides
    @Singleton
    fun provideBaseUrl() = BASE_URL

    @Provides
    @Singleton
    fun provideConnectionTimeout() = NETWORK_TIMEOUT

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    @Named(NAMED_HEADER)
    fun provideHeaderInterceptor() = Interceptor { chain ->
        var request = chain.request()
        request = request.newBuilder()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $APIKEY")
            .build()
        try {
            chain.proceed(request)

        }catch (e: StreamResetException){
            e.printStackTrace()
            return@Interceptor Response.Builder()
                .code(500)
                .message("Stream reset by server or client")
                .build()
        }
    }

    @Provides
    @Singleton
    @Named(NAMED_BODY)
    fun provideBodyInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideClient(
        time: Long,
        @Named(NAMED_HEADER) header: Interceptor,
        @Named(NAMED_BODY) body: HttpLoggingInterceptor
    ) = OkHttpClient.Builder()
        .addInterceptor(header)
        .addInterceptor(body)
        .connectTimeout(time, TimeUnit.SECONDS)
        .readTimeout(time, TimeUnit.SECONDS)
        .writeTimeout(time, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String, gson: Gson, client: OkHttpClient): LogoMakerApiServices =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(LogoMakerApiServices::class.java)
}