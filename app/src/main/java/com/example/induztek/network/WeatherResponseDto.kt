package com.example.induztek.network

import com.google.gson.annotations.SerializedName

data class WeatherResponseDto(
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("current")
    val current: CurrentWeatherDto? = null,
)

data class CurrentWeatherDto(
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("temperature_2m")
    val temperature2m: Double? = null,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: Int? = null,
    @SerializedName("weather_code")
    val weatherCode: Int? = null,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: Double? = null,
)
