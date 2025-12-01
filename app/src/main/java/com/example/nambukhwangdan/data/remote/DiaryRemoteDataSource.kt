package com.example.nambukhwangdan.data.remote

import com.example.nambukhwangdan.model.Diary
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class DiaryRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeDiaries(userId: String): Flow<List<Diary>> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection("users")
            .document(userId)
            .collection("diaries")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val diaries = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Diary::class.java)?.copy(id = document.id)
                } ?: emptyList()

                trySend(diaries)
            }

        awaitClose { registration.remove() }
    }
}
