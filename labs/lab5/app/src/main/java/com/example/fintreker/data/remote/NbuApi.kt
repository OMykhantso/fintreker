package com.example.fintreker.data.remote

import com.example.fintreker.data.local.RateEntity
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

/**
 * Елемент відповіді API НБУ:
 * `[{"r030":840,"txt":"Долар США","rate":41.2,"cc":"USD","exchangedate":"01.10.2026"}, ...]`.
 * Усі поля nullable: Gson не поважає non-null типи Kotlin, тому перевіряємо самі.
 */
data class NbuRateDto(
    @SerializedName("r030") val numericCode: Int?,
    @SerializedName("txt") val name: String?,
    @SerializedName("rate") val rate: Double?,
    @SerializedName("cc") val code: String?,
    @SerializedName("exchangedate") val exchangeDate: String?
)

interface NbuApi {
    @GET("NBUStatService/v1/statdirectory/exchange?json")
    suspend fun getExchangeRates(): List<NbuRateDto>
}

/** Безпечний мапер DTO -> Entity: некоректні записи (без коду/курсу, курс <= 0) відкидаються. */
fun NbuRateDto.toEntity(updatedAt: Long): RateEntity? {
    val currency = code?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: return null
    val value = rate?.takeIf { it.isFinite() && it > 0.0 } ?: return null
    return RateEntity(
        code = currency,
        rate = value,
        exchangeDate = exchangeDate.orEmpty(),
        updatedAt = updatedAt
    )
}
