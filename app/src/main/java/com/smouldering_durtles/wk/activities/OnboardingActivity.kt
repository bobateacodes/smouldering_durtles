package com.smouldering_durtles.wk.activities
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingActivity : ViewModel() {
    private val _state = MutableStateFlow(FirstState())
    val state = _state.asStateFlow()
}

class FirstState