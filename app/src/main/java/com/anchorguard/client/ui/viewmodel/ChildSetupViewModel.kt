package com.anchorguard.client.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.ui.theme.KidTheme

/**
 * ViewModel for the child setup flow.
 * Tracks setup progress, theme selection, and device registration state.
 */
class ChildSetupViewModel(private val store: SecureSessionStore) : ViewModel() {

    private val _step = mutableStateOf(1)
    val step: Int
        get() = _step.value

    private val _selectedTheme = mutableStateOf(KidTheme.OCEAN_BLUE)
    val selectedTheme: KidTheme
        get() = _selectedTheme.value

    private val _isPaired = mutableStateOf(false)
    val isPaired: Boolean
        get() = _isPaired.value

    private val _error = mutableStateOf<String?>(null)
    val error: String?
        get() = _error.value

    init {
        _selectedTheme.value = KidTheme.valueOf(store.getSelectedTheme().uppercase())
    }

    fun nextStep() {
        _step.value = _step.value + 1
    }

    fun previousStep() {
        _step.value = (_step.value - 1).coerceAtLeast(1)
    }

    fun selectTheme(theme: KidTheme) {
        _selectedTheme.value = theme
        store.setSelectedTheme(theme.id)
    }

    fun setPaired(paired: Boolean) {
        _isPaired.value = paired
    }

    fun setError(error: String?) {
        _error.value = error
    }

    fun reset() {
        _step.value = 1
        _selectedTheme.value = KidTheme.OCEAN_BLUE
        _isPaired.value = false
        _error.value = null
    }
}
