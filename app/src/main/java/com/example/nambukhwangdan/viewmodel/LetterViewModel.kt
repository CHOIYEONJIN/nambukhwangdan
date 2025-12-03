package com.example.nambukhwangdan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nambukhwangdan.data.repository.LetterRepository
import com.example.nambukhwangdan.model.Letter.Letter
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LetterViewModel @Inject constructor(
    private val repo: LetterRepository
) : ViewModel() {

    enum class ReceiverOption { FUTURE_SELF, RANDOM_ANONYMOUS }

    private val auth = FirebaseAuth.getInstance()
    private val userId: String?
        get() = auth.currentUser?.uid

    val letterContent = MutableStateFlow("")
    private val _receiverOption = MutableStateFlow(ReceiverOption.FUTURE_SELF)
    val receiverOption = _receiverOption

    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis = _selectedDateMillis

    private val _selectedHour = MutableStateFlow(23)
    private val _selectedMinute = MutableStateFlow(0)
    val selectedHour = _selectedHour
    val selectedMinute = _selectedMinute

    val scheduledAt = combine(_selectedDateMillis, _selectedHour, _selectedMinute) { date, hour, minute ->
        val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, System.currentTimeMillis())

    private val _sendInProgress = MutableStateFlow(false)
    val sendInProgress = _sendInProgress

    val receiverName = receiverOption.combine(scheduledAt) { option, _ ->
        when (option) {
            ReceiverOption.FUTURE_SELF -> "미래의 나"
            ReceiverOption.RANDOM_ANONYMOUS -> "익명의 누군가"
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "미래의 나")

    val allLetters = repo.observeLetters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        repo.startSync()
    }

    fun updateContent(text: String) {
        letterContent.value = text
    }

    fun setReceiver(option: ReceiverOption) {
        _receiverOption.value = option
    }

    fun setSelectedDate(millis: Long) {
        _selectedDateMillis.value = millis
    }

    fun setSelectedTime(hour: Int, minute: Int) {
        _selectedHour.value = hour
        _selectedMinute.value = minute
    }

    fun sendLetter(onComplete: (String) -> Unit = {}) {
        val now = System.currentTimeMillis()
        val scheduledAtValue = scheduledAt.value
        val baseLetter = Letter(
            id = UUID.randomUUID().toString(),
            content = letterContent.value,
            createdAt = now,
            updatedAt = now,
            scheduledAt = scheduledAtValue,
            senderId = userId ?: "",
            senderName = auth.currentUser?.displayName ?: "익명",
            replyToId = null,
            anonymous = _receiverOption.value == ReceiverOption.RANDOM_ANONYMOUS
        )

        viewModelScope.launch {
            _sendInProgress.value = true
            try {
                if (_receiverOption.value == ReceiverOption.RANDOM_ANONYMOUS) {
                    val id = repo.scheduleAnonymousDelivery(baseLetter)
                    if (id == null) {
                        _sendInProgress.value = false
                        return@launch
                    }
                } else {
                    repo.sendScheduledLetterToSelf(baseLetter)
                }
                onComplete(baseLetter.id)
                clearStates()
            } finally {
                _sendInProgress.value = false
            }
        }
    }

    fun toggleLike(id: String) = viewModelScope.launch { repo.toggleLike(id) }

    fun deleteLetter(id: String) = viewModelScope.launch { repo.deleteLetter(id) }

    private fun clearStates() {
        letterContent.value = ""
        _receiverOption.value = ReceiverOption.FUTURE_SELF
        _selectedHour.value = 23
        _selectedMinute.value = 0
    }
}
