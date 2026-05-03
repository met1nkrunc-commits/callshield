package com.callshield.app.ui.screen.trusted

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VerifiedUser
import com.callshield.app.ui.component.EmptyState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.domain.repository.TrustedNumber
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.BEngelGreen
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkSurface
import com.callshield.app.ui.theme.DarkSurfaceVar
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedNumbersScreen(
    onBack: () -> Unit,
    viewModel: TrustedNumbersViewModel = hiltViewModel(),
) {
    val numbers by viewModel.numbers.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text("Güvenilir Numaralar", color = OnDarkPrimary, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 20.sp, color = OnDarkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = BEngelGreen,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle", tint = DarkBg)
            }
        },
    ) { padding ->
        if (numbers.isEmpty()) {
            EmptyState(
                icon = Icons.Default.VerifiedUser,
                title = "Güvenilir numara yok",
                subtitle = "Güvendiğin numaraları ekle,\nCallshield onları hiçbir zaman engellemez",
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg)
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(Modifier.height(12.dp)) }
                items(numbers, key = { it.phoneNumber }) { item ->
                    TrustedNumberRow(
                        number = item,
                        onDelete = { viewModel.remove(item.phoneNumber) },
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showDialog) {
        AddTrustedNumberDialog(
            onDismiss = { showDialog = false },
            onConfirm = { number, label ->
                viewModel.add(number, label)
                showDialog = false
            },
        )
    }
}

@Composable
private fun TrustedNumberRow(number: TrustedNumber, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BEngelAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = BEngelAccent, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = number.phoneNumber,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = OnDarkPrimary,
            )
            if (number.label.isNotBlank()) {
                Text(text = number.label, fontSize = 12.sp, color = OnDarkSecondary)
            }
            Text(
                text = "Eklendi: ${formatDate(number.addedAt)}",
                fontSize = 11.sp,
                color = OnDarkSecondary,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = OnDarkSecondary)
        }
    }
}

@Composable
private fun AddTrustedNumberDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var number by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Güvenilir Numara Ekle", color = OnDarkPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Telefon numarası", color = OnDarkSecondary) },
                    placeholder = { Text("05XX XXX XX XX", color = OnDarkSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnDarkPrimary,
                        unfocusedTextColor = OnDarkPrimary,
                        focusedBorderColor = BEngelAccent,
                        unfocusedBorderColor = OnDarkSecondary,
                    ),
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Etiket (isteğe bağlı)", color = OnDarkSecondary) },
                    placeholder = { Text("Aile, İş, ...", color = OnDarkSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnDarkPrimary,
                        unfocusedTextColor = OnDarkPrimary,
                        focusedBorderColor = BEngelAccent,
                        unfocusedBorderColor = OnDarkSecondary,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (number.isNotBlank()) onConfirm(number, label) }) {
                Text("Ekle", color = BEngelGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = OnDarkSecondary)
            }
        },
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
