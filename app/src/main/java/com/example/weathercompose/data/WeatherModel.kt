package com.example.weathercompose.data

data class WeatherModel(
    val city: String,
    val time: String,
    val currentTemp: String,
    val condition: String,//погодные условия
    val icon: String,
    val maxTemp: String,
    val minTemp: String,
    val hours: String
)