package com.example.discover

data class Hidayath(
    val arabic: String,
    val translation: String,
    val reference: String
)

data class RoutineItem(
    val timeOfDay: String,
    val title: String,
    val description: String,
    val iconId: Int // Since we don't want to use drawables, we will map this to Material Icons in the UI
)

data class Hadees(
    val text: String,
    val reference: String
)

data class Teaching(
    val title: String,
    val description: String
)

data class HistoricalPlace(
    val id: String,
    val name: String,
    val location: String,
    val imageUrl: String,
    val story: String
)
