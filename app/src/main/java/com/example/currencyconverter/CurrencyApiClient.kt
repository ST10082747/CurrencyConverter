package com.example.currencyconverter

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CurrencyApiClient {
    private const val BASE_URL = "https://api.getgeoapi.com/"
    private const val API_KEY = "b9472c0af8bbfe6537352dc461377cb776ed8753"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("OkHttp", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: CurrencyApiService = retrofit.create(CurrencyApiService::class.java)

    suspend fun getCurrencies(): CurrencyListResponse {
        val response = apiService.getCurrencies(API_KEY)
        Log.d("CurrencyApiClient", "getCurrencies response: $response")
        return response
    }

    suspend fun convertCurrency(from: String, to: String, amount: Double): ConversionResponse {
        val response = apiService.convertCurrency(API_KEY, from, to, amount)
        Log.d("CurrencyApiClient", "convertCurrency response: $response")
        return response
    }
}