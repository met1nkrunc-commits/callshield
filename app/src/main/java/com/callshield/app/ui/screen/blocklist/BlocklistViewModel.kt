package com.callshield.app.ui.screen.blocklist

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.domain.model.BlockedNumber
import com.callshield.app.domain.repository.BlockedNumberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class BlocklistUiState(
    val numbers: List<BlockedNumber> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class BlocklistViewModel @Inject constructor(
    private val repository: BlockedNumberRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlocklistUiState())
    val uiState: StateFlow<BlocklistUiState> = _uiState.asStateFlow()

    init {
        repository.getAll()
            .onEach { list -> _uiState.update { it.copy(numbers = list, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    fun addNumber(phoneNumber: String, label: String) {
        if (phoneNumber.isBlank()) return
        viewModelScope.launch {
            repository.addNumber(
                BlockedNumber(
                    id = 0,
                    phoneNumber = phoneNumber.trim(),
                    label = label.ifBlank { "Manuel eklendi" },
                    createdAt = System.currentTimeMillis(),
                    isManual = true,
                )
            )
        }
    }

    fun removeNumber(phoneNumber: String) {
        viewModelScope.launch {
            repository.removeNumber(phoneNumber)
        }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            val numbers = repository.getAll().first()
            if (numbers.isEmpty()) return@launch

            val csv = buildString {
                appendLine("Numara,Etiket,Eklenme Tarihi,Manuel")
                numbers.forEach { n ->
                    val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(n.createdAt))
                    appendLine("${n.phoneNumber},${n.label ?: ""},${date},${if (n.isManual) "Evet" else "Hayır"}")
                }
            }

            val file = File(context.cacheDir, "callshield_engelliler.csv")
            file.writeText(csv)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Callshield Engellenen Numaralar")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Dışa Aktar"))
        }
    }
}
