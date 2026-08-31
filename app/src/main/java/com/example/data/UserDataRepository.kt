package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val name: String? = null,
    val lastReadSurah: Int = 1,
    val lastReadAyah: Int = 1
)

class UserDataRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun saveReadingProgress(surah: Int, ayah: Int) {
        val user = auth.currentUser ?: return
        val profileUpdate = mapOf(
            "lastReadSurah" to surah,
            "lastReadAyah" to ayah
        )
        try {
            usersCollection.document(user.uid).update(profileUpdate).await()
        } catch (e: Exception) {
            // Document might not exist, create it
            usersCollection.document(user.uid).set(
                UserProfile(uid = user.uid, name = user.displayName, lastReadSurah = surah, lastReadAyah = ayah)
            ).await()
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        return try {
            val snapshot = usersCollection.document(user.uid).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)
            } else {
                val newProfile = UserProfile(uid = user.uid, name = user.displayName)
                usersCollection.document(user.uid).set(newProfile).await()
                newProfile
            }
        } catch (e: Exception) {
            null
        }
    }
}
