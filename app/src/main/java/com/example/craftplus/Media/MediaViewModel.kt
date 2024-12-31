package com.example.craftplus.Media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaViewModel(
    private val mediaReader: MediaReader
): ViewModel() {

    var files by mutableStateOf(listOf<MediaFile>())
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            files = mediaReader.getAllMediaFiles()
        }
    }
}