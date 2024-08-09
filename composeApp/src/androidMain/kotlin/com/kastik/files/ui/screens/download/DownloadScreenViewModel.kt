package com.kastik.files.ui.screens.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.kastik.files.FilesApp
import com.kastik.files.nearby.managers.DownloadManager
import com.kastik.files.nearby.managers.DownloadState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DownloadScreenViewModel(downloadManager: DownloadManager) : ViewModel() {

    val state = downloadManager.downloadManagerStateFlow

    val textState: StateFlow<String> = state
        .map { uploadState -> mapStateToText(uploadState) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), mapStateToText(state.value))


    private fun mapStateToText(downloadState: DownloadState): String {
        return when (downloadState) {
            DownloadState.Failure -> "Something went wrong"
            DownloadState.FinishedDownload -> "Finished downloading"
            DownloadState.Rejected -> "The connection got Rejected"
            DownloadState.Initing -> "Waiting for device"
            DownloadState.CanceledDownload -> "Canceled download"
            is DownloadState.Extracting -> "Extracting ${downloadState.progress * 100}%"
            is DownloadState.Downloading -> "Downloading ${downloadState.progress * 100}%"
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
                return DownloadScreenViewModel(
                    DownloadManager(application as FilesApp)
                ) as T
            }
        }
    }
}