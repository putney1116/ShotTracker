package com.example.shottracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.repository.ClubRepository
import com.example.shottracker.domain.repository.RoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val activeRound: Round? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val roundRepository: RoundRepository,
    private val clubRepository: ClubRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        initializeClubs()
        loadActiveRound()
    }

    private fun initializeClubs() {
        viewModelScope.launch {
            clubRepository.initializeDefaultClubs()
        }
    }

    private fun loadActiveRound() {
        viewModelScope.launch {
            roundRepository.getActiveRound().collect { round ->
                _uiState.value = _uiState.value.copy(
                    activeRound = round,
                    isLoading = false
                )
            }
        }
    }
}
