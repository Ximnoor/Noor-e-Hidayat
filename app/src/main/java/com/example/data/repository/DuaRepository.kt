package com.example.data.repository

import com.example.data.local.Dua
import com.example.data.local.DuaDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DuaRepository(private val duaDao: DuaDao) {

    val allDuas: Flow<List<Dua>> = duaDao.getAllDuas()

    fun getDuasByCategory(category: String): Flow<List<Dua>> = duaDao.getDuasByCategory(category)

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        duaDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun populateInitialDataIfNeeded() {
        withContext(Dispatchers.IO) {
            if (duaDao.getDuaCount() == 0) {
                val initialDuas = listOf(
                    Dua(
                        category = "Morning",
                        title = "Waking up",
                        arabicText = "الْحَمْدُ للهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
                        translation = "All praise is for Allah who gave us life after having taken it from us and unto Him is the resurrection.",
                        reference = "Bukhari"
                    ),
                    Dua(
                        category = "Morning",
                        title = "Morning Supplication",
                        arabicText = "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
                        translation = "O Allah, by You we enter the morning and by You we enter the evening, by You we live and by You we die, and to You is the Final Return.",
                        reference = "Tirmidhi"
                    ),
                    Dua(
                        category = "Evening",
                        title = "Evening Supplication",
                        arabicText = "اللَّهُمَّ بِكَ أَمْسَيْنَا وَبِكَ أَصْبَحْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ الْمَصِيرُ",
                        translation = "O Allah, by You we enter the evening and by You we enter the morning, by You we live and by You we die, and to You is the Return.",
                        reference = "Tirmidhi"
                    ),
                    Dua(
                        category = "Sleep",
                        title = "Before Sleeping",
                        arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                        translation = "In Your name O Allah, I live and die.",
                        reference = "Bukhari"
                    ),
                    Dua(
                        category = "Daily",
                        title = "Before Eating",
                        arabicText = "بِسْمِ اللهِ",
                        translation = "In the Name of Allah.",
                        reference = "Abu Dawud"
                    ),
                    Dua(
                        category = "Daily",
                        title = "After Eating",
                        arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
                        translation = "All praise is due to Allah who fed me this and provided it for me without any might nor power from myself.",
                        reference = "Tirmidhi"
                    ),
                    Dua(
                        category = "Protection",
                        title = "Leaving the Home",
                        arabicText = "بِسْمِ اللهِ، تَوَكَّلْتُ عَلَى اللهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللهِ",
                        translation = "In the Name of Allah, I have placed my trust in Allah, there is no might and no power except by Allah.",
                        reference = "Abu Dawud"
                    ),
                    Dua(
                        category = "Daily",
                        title = "Entering the Home",
                        arabicText = "بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى رَبِّنَا تَوَكَّلْنَا",
                        translation = "In the name of Allah we enter, and in the name of Allah we leave, and upon our Lord we place our trust.",
                        reference = "Abu Dawud"
                    ),
                    Dua(
                        category = "Daily",
                        title = "Before Drinking Water",
                        arabicText = "بِسْمِ اللهِ",
                        translation = "In the Name of Allah.",
                        reference = "Tirmidhi"
                    ),
                    Dua(
                        category = "Daily",
                        title = "After Drinking Water",
                        arabicText = "الْحَمْدُ لِلَّهِ",
                        translation = "All praise is due to Allah.",
                        reference = "Tirmidhi"
                    ),
                    Dua(
                        category = "Restroom",
                        title = "Entering Restroom",
                        arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبْثِ وَالْخَبَائِثِ",
                        translation = "O Allah, I seek refuge in You from the male and female evil and jinn.",
                        reference = "Bukhari"
                    ),
                    Dua(
                        category = "Restroom",
                        title = "Leaving Restroom",
                        arabicText = "غُفْرَانَكَ",
                        translation = "I ask You (Allah) for forgiveness.",
                        reference = "Abu Dawud"
                    )
                )
                duaDao.insertAll(initialDuas)
            }
        }
    }
}
