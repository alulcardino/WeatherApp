package com.romanmikhailenko.weaterapp.data.model.current

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)