package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AladhanResponse(
    val code: Int,
    val data: AladhanData?
)

@JsonClass(generateAdapter = true)
data class AladhanData(
    val timings: Timings,
    val date: AladhanDate? = null
)

@JsonClass(generateAdapter = true)
data class AladhanDate(
    val readable: String,
    val timestamp: String,
    val hijri: HijriDate? = null
)

@JsonClass(generateAdapter = true)
data class HijriDate(
    val date: String,
    val format: String,
    val day: String,
    val month: HijriMonth,
    val year: String
)

@JsonClass(generateAdapter = true)
data class HijriMonth(
    val number: Int,
    val en: String,
    val ar: String
)

@JsonClass(generateAdapter = true)
data class Timings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Sunset: String,
    val Maghrib: String,
    val Isha: String,
    val Imsak: String,
    val Midnight: String
)
