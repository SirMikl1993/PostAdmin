package com.example.postadmin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {
    private val _counterState = mutableIntStateOf(0)
    var counterState by _counterState
}