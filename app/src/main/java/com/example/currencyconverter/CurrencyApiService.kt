package com.example.currencyconverter

import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {
    @GET("v2/currency/list")
    suspend fun getCurrencies(
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): CurrencyListResponse

    @GET("v2/currency/convert")
    suspend fun convertCurrency(
        @Query("api_key") apiKey: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double,
        @Query("format") format: String = "json"
    ): ConversionResponse
}