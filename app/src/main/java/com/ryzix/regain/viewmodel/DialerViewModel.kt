package com.ryzix.regain.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DialerViewModel : ViewModel() {

    private val _dialInput = MutableStateFlow("")
    val dialInput: StateFlow<String> = _dialInput.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun appendDigit(digit: String) {
        _dialInput.value += digit
    }

    fun deleteDigit() {
        val v = _dialInput.value
        if (v.isNotEmpty()) _dialInput.value = v.dropLast(1)
    }

    fun clearDial() {
        _dialInput.value = ""
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }
}
