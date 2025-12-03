package com.example.nambukhwangdan.data

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class FirebaseFunctionsSource {

    private val functions = FirebaseFunctions.getInstance("asia-northeast3")
    suspend fun pickRandomUser(): String {
        val result = functions
            .getHttpsCallable("pickRandomUser")
            .call()
            .await()

        val data = result.data as Map<*, *>
        return data["receiverId"] as String
    }
}
