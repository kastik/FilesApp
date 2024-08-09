package com.kastik.files.ui.screens.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.kastik.files.FilesApp
import com.kastik.files.nearby.managers.UploadManager
import com.kastik.files.nearby.managers.UploadState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class UploadScreenViewModel(
    uploadManager: UploadManager,
) : ViewModel() {

    val state = uploadManager.uploadManagerStateFlow

    val textState: StateFlow<String> = state
        .map { uploadState -> mapStateToText(uploadState) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), mapStateToText(state.value))


    private fun mapStateToText(uploadState: UploadState): String {
        return when (uploadState) {
            UploadState.Failure -> "Something went wrong"
            UploadState.FinishedUpload -> "Finished uploading"
            UploadState.Rejected -> "The connection got Rejected"
            UploadState.Initing -> "Initing..."
            else -> ""
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras,
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                @Suppress("UNCHECKED_CAST")
                return UploadScreenViewModel(
                    UploadManager(application as FilesApp)
                ) as T
            }
        }
    }
}