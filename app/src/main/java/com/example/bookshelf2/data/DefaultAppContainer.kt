package com.example.bookshelf2.data

import com.example.bookshelf2.network.BookShelfApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

interface AppContainer {
    val bookshelfRepository: BookshelfRepository
}

class DefaultAppContainer: AppContainer {
    private val baseUrl = "https://openlibrary.org/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
//        .addInterceptor { chain ->
//            val original = chain.request()
//            val originalHttpUrl = original.url
//
//            val url = originalHttpUrl.newBuilder()
//                .build()
//
//            val requestBuilder = original.newBuilder()
//                .url(url)
//
//            val request = requestBuilder.build()
//            chain.proceed(request)
//        }
        .addInterceptor(loggingInterceptor)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(okHttpClient)
        .baseUrl(baseUrl)
        .build()

    private val retrofitService: BookShelfApiService by lazy {
        retrofit.create(BookShelfApiService::class.java)
    }


    override val bookshelfRepository: BookshelfRepository by lazy {
       NetworkBookShelfRepository(retrofitService)
    }
}