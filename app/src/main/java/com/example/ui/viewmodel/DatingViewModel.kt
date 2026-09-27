package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.DatingProfile
import com.example.data.model.RealDate
import com.example.data.model.UserProfile
import com.example.data.repository.DatingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DatingScreen {
    FEED,
    DATES,
    CHATS,
    COMPATIBILITY,
    PROFILE
}

class DatingViewModel(private val repository: DatingRepository) : ViewModel() {

    private val _currentScreen = MutableStateFlow(DatingScreen.FEED)
    val currentScreen: StateFlow<DatingScreen> = _currentScreen.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>("Todas")
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedProfileForDetail = MutableStateFlow<DatingProfile?>(null)
    val selectedProfileForDetail: StateFlow<DatingProfile?> = _selectedProfileForDetail.asStateFlow()

    private val _activeChatProfile = MutableStateFlow<DatingProfile?>(null)
    val activeChatProfile: StateFlow<DatingProfile?> = _activeChatProfile.asStateFlow()

    private val _matchCelebrationProfile = MutableStateFlow<DatingProfile?>(null)
    val matchCelebrationProfile: StateFlow<DatingProfile?> = _matchCelebrationProfile.asStateFlow()

    private val _proposeDateForProfile = MutableStateFlow<DatingProfile?>(null)
    val proposeDateForProfile: StateFlow<DatingProfile?> = _proposeDateForProfile.asStateFlow()

    val rawFeedProfiles: StateFlow<List<DatingProfile>> = repository.feedProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredFeedProfiles: StateFlow<List<DatingProfile>> = combine(
        rawFeedProfiles,
        _selectedCategoryFilter
    ) { profiles, category ->
        if (category == null || category == "Todas") {
            profiles
        } else {
            profiles.filter { it.idealDateCategory.contains(category, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchedProfiles: StateFlow<List<DatingProfile>> = repository.matchedProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val realDates: StateFlow<List<RealDate>> = repository.allDates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialData()
        }
    }

    fun setScreen(screen: DatingScreen) {
        _currentScreen.value = screen
        if (screen != DatingScreen.CHATS) {
            _activeChatProfile.value = null
        }
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun selectProfileForDetail(profile: DatingProfile?) {
        _selectedProfileForDetail.value = profile
    }

    fun openChatWithProfile(profile: DatingProfile) {
        _activeChatProfile.value = profile
        _currentScreen.value = DatingScreen.CHATS
        observeMessages(profile.id)
    }

    fun closeChat() {
        _activeChatProfile.value = null
    }

    private fun observeMessages(profileId: Long) {
        viewModelScope.launch {
            repository.getMessages(profileId).collect { msgs ->
                _chatMessages.value = msgs
            }
        }
    }

    fun likeProfile(profile: DatingProfile) {
        viewModelScope.launch {
            repository.matchWithProfile(profile.id)
            _matchCelebrationProfile.value = profile
            _selectedProfileForDetail.value = null
        }
    }

    fun dismissMatchCelebration() {
        _matchCelebrationProfile.value = null
    }

    fun passProfile(profile: DatingProfile) {
        viewModelScope.launch {
            repository.passProfile(profile.id)
            if (_selectedProfileForDetail.value?.id == profile.id) {
                _selectedProfileForDetail.value = null
            }
        }
    }

    fun resetFeed() {
        viewModelScope.launch {
            repository.resetFeed()
        }
    }

    fun startProposingDate(profile: DatingProfile) {
        _proposeDateForProfile.value = profile
    }

    fun dismissProposeDate() {
        _proposeDateForProfile.value = null
    }

    fun confirmProposeDate(
        title: String,
        location: String,
        dateTime: String,
        category: String,
        notes: String
    ) {
        val target = _proposeDateForProfile.value ?: return
        viewModelScope.launch {
            repository.proposeRealDate(
                profileId = target.id,
                profileName = target.name,
                profileDrawable = target.drawableResName,
                title = title,
                location = location,
                dateTimeString = dateTime,
                category = category,
                notes = notes
            )
            _proposeDateForProfile.value = null
            _currentScreen.value = DatingScreen.DATES
        }
    }

    fun sendMessage(text: String) {
        val profile = _activeChatProfile.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendUserMessage(profile.id, text.trim(), profile.name)

            // Dynamic realistic reply after short delay
            delay(1200)
            val reply = when {
                text.contains("café", ignoreCase = true) || text.contains("lugar", ignoreCase = true) ->
                    "¡Me parece genial! Conozco un rincón con un café estupendo y música suave para charlar a gusto. ¿Qué horario prefieres?"
                text.contains("hora", ignoreCase = true) || text.contains("cuándo", ignoreCase = true) || text.contains("día", ignoreCase = true) ->
                    "¡Este fin de semana tengo la tarde libre! ¿Te parece que agendemos la cita directamente en la app para fijar el plan?"
                text.contains("hola", ignoreCase = true) || text.contains("buenas", ignoreCase = true) ->
                    "¡Hola! Me alegra que conectemos. Me encanta que en CitaReal la gente realmente busque conocerse en persona. ¿Cómo va tu día?"
                else ->
                    "Totalmente de acuerdo contigo. La química de verdad solo se descubre mirando a la persona a los ojos. ¿Nos vemos pronto?"
            }
            repository.sendMatchReply(profile.id, reply, profile.name)
        }
    }

    fun sendRespectfulClosure() {
        val profile = _activeChatProfile.value ?: return
        viewModelScope.launch {
            val closureText = "Agradezco mucho el tiempo que hemos compartido charlando, pero siento que buscamos cosas distintas. ¡Te deseo lo mejor en tu búsqueda de conexiones auténticas!"
            repository.sendUserMessage(profile.id, closureText, profile.name)
            delay(1000)
            repository.sendMatchReply(
                profile.id,
                "Muchas gracias por ser tan sincero y respetuoso. ¡Te deseo mucho éxito también! Abrazo.",
                profile.name
            )
        }
    }

    fun updateSafetyProtocol(date: RealDate, isShared: Boolean) {
        viewModelScope.launch {
            repository.updateDateStatus(
                date.copy(isSafetyProtocolActive = isShared),
                date.status
            )
        }
    }

    fun cancelDate(dateId: Long) {
        viewModelScope.launch {
            repository.deleteDate(dateId)
        }
    }

    fun submitQuiz(answers: List<String>) {
        viewModelScope.launch {
            repository.recalculateCompatibility(answers)
            _currentScreen.value = DatingScreen.FEED
        }
    }

    fun saveUserProfile(name: String, bio: String, idealDate: String, nonNegotiables: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateUserProfile(
                current.copy(
                    name = name,
                    bio = bio,
                    idealDate = idealDate,
                    nonNegotiables = nonNegotiables
                )
            )
        }
    }
}

class DatingViewModelFactory(private val repository: DatingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DatingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
