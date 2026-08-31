package com.example.discover

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DiscoverViewModel : ViewModel() {

    private val _historicalPlaces = MutableStateFlow(
        listOf(
            HistoricalPlace(
                id = "1",
                name = "Masjid al-Haram",
                location = "Mecca, Saudi Arabia",
                imageUrl = "https://images.unsplash.com/photo-1565552645632-d725f8bfc19a?q=80&w=800",
                story = "The Great Mosque of Mecca, enclosing the Kaaba. It is the holiest site in Islam and the primary destination for the Hajj and Umrah pilgrimages."
            ),
            HistoricalPlace(
                id = "2",
                name = "Al-Masjid an-Nabawi",
                location = "Medina, Saudi Arabia",
                imageUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?q=80&w=800",
                story = "The Prophet's Mosque, built by the Islamic Prophet Muhammad. It is the second holiest site in Islam and contains his final resting place."
            ),
            HistoricalPlace(
                id = "3",
                name = "Al-Aqsa Mosque",
                location = "Jerusalem",
                imageUrl = "https://images.unsplash.com/photo-1626081395988-124b17f8a7d5?q=80&w=800",
                story = "The third holiest site in Islam. Muslims believe that Prophet Muhammad was transported here from Mecca during the Night Journey (Isra and Mi'raj)."
            ),
            HistoricalPlace(
                id = "4",
                name = "Quba Mosque",
                location = "Medina, Saudi Arabia",
                imageUrl = "https://images.unsplash.com/photo-1596773539879-c5c6f3ccf8f5?q=80&w=800",
                story = "The first mosque in the world, built by the Prophet Muhammad upon his arrival in Medina during the emigration (Hijrah)."
            )
        )
    )
    val historicalPlaces: StateFlow<List<HistoricalPlace>> = _historicalPlaces.asStateFlow()

    private val _hidayath = MutableStateFlow(
        Hidayath(
            arabic = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
            translation = "Whoever travels a path in search of knowledge, Allah will make easy for him a path to Paradise.",
            reference = "Sahih Muslim"
        )
    )
    val hidayath: StateFlow<Hidayath> = _hidayath.asStateFlow()

    private val _dailyRoutine = MutableStateFlow(
        listOf(
            RoutineItem("Early Morning", "Fajr & Waking Up", "Recite waking up Dua, perform Wudu with mindfulness, and pray Fajr.", 1),
            RoutineItem("Morning", "Morning Adhkar", "Recite the morning supplications to seek protection and blessings for the day.", 2),
            RoutineItem("Mid-Day", "Dhuhr & Honesty", "Pray Dhuhr and ensure your work or studies are done with honesty and excellence.", 3),
            RoutineItem("Afternoon", "Asr & Evening Adhkar", "Pray Asr, take a moment to reflect, and recite evening supplications.", 4),
            RoutineItem("Evening", "Maghrib & Family", "Pray Maghrib, break fast (if fasting), and spend quality time with family.", 5),
            RoutineItem("Night", "Isha & Sleep", "Pray Isha, recite the sleeping Dua, and sleep early to wake up fresh for Fajr.", 6)
        )
    )
    val dailyRoutine: StateFlow<List<RoutineItem>> = _dailyRoutine.asStateFlow()

    private val _hadeesCollection = MutableStateFlow(
        listOf(
            Hadees(
                text = "Actions are according to intentions, and everyone will get what was intended.",
                reference = "Sahih al-Bukhari 1"
            ),
            Hadees(
                text = "The best among you are those who have the best manners and character.",
                reference = "Sahih al-Bukhari 6029"
            ),
            Hadees(
                text = "None of you truly believes until he loves for his brother what he loves for himself.",
                reference = "Sahih Muslim 45"
            ),
            Hadees(
                text = "A good word is charity.",
                reference = "Sahih al-Bukhari 2989"
            )
        )
    )
    val hadeesCollection: StateFlow<List<Hadees>> = _hadeesCollection.asStateFlow()

    private val _teachings = MutableStateFlow(
        listOf(
            Teaching(
                title = "Honesty & Integrity",
                description = "A Muslim must be truthful in speech and honest in trade, fulfilling trusts and promises."
            ),
            Teaching(
                title = "Respect for Parents",
                description = "Showing kindness, respect, and lowering the wing of humility to one's parents is highly emphasized."
            ),
            Teaching(
                title = "Kindness to Neighbors",
                description = "Ensuring the safety, comfort, and well-being of neighbors, regardless of their background."
            ),
            Teaching(
                title = "Seeking Knowledge",
                description = "Seeking beneficial knowledge is an obligation upon every Muslim."
            )
        )
    )
    val teachings: StateFlow<List<Teaching>> = _teachings.asStateFlow()
}
