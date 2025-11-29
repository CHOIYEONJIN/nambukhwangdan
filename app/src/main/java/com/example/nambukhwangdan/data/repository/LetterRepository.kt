package com.example.nambukhwangdan.data.repository
import com.example.nambukhwangdan.data.local.LetterDao
import com.example.nambukhwangdan.model.Letter.Letter
import com.example.nambukhwangdan.model.Letter.toEntity
import com.example.nambukhwangdan.model.Letter.toLetter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// LetterRepository.kt
@Singleton
class LetterRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val letterDao: LetterDao

) {
    // LetterRepository.kt
    fun getAllLetters(): Flow<List<Letter>> =
        letterDao.getAllLetters().map { list ->
            list.map { it.toLetter() }   // ←🔥 Mapper 적용!!
        }


    suspend fun insertLetter(letter: Letter) {
        letterDao.insertLetter(letter.toEntity())
    }

    suspend fun deleteLetter(id: String) = letterDao.deleteLetter(id)
    suspend fun toggleLike(id: String) = letterDao.toggleLike(id)
    suspend fun saveLetterToFirestore(letter: Letter, userId: String): Boolean = try {
        firestore.collection("users")
            .document(userId)
            .collection("letters")
            .document(letter.id)
            .set(letter)
            .await()
        true
    } catch (e: Exception) {
        false
    }
    // LetterRepository.kt
    suspend fun deleteLetterFromFirestore(id: String,userId:String): Boolean = try {
        firestore.collection("users")       // 🔥 요거 바꿔야 함
            .document(userId)
            .collection("letters")
            .document(id)
            .delete()
            .await()
        true
    } catch (e: Exception) {
        false
    }


}
