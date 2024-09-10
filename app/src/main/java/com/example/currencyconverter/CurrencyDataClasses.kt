package com.example.currencyconverter

data class CurrencyListResponse(
    val status: String,
    val currencies: Map<String, String>
)

data class ConversionResponse(
    val status: String,
    val updated_date: String,
    val base_currency_code: String,
    val amount: Double,
    val base_currency_name: String,
    val rates: Map<String, CurrencyRate>
)

data class CurrencyRate(
    val currency_name: String,
    val rate: String,
    val rate_for_amount: String
)