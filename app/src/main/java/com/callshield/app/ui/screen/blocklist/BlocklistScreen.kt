package com.callshield.app.ui.screen.blocklist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.domain.model.BlockedNumber
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.BEngelGreen
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkBorder
import com.callshield.app.ui.theme.DarkSurface
import com.callshield.app.ui.theme.DarkSurfaceVar
import com.callshield.app.ui.theme.OnDarkDisabled
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary
import com.callshield.app.ui.theme.StatusBlocked

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlocklistScreen(
    viewModel: BlocklistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Engellenen Numaralar",
                        color = OnDarkPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = DarkSurface,
                    titleContentColor = OnDarkPrimary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showAddDialog = true },
                containerColor = BEngelGreen,
                contentColor   = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Numara ekle")
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(paddingValues),
        ) {
            if (uiState.numbers.isEmpty()) {
                EmptyState(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(
                        items = uiState.numbers,
                        key   = { it.phoneNumber },
                    ) { blocked ->
                        SwipeToDeleteItem(
                            item     = blocked,
                            onDelete = { viewModel.removeNumber(blocked.phoneNumber) },
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddNumberDialog(
            onConfirm = { number, reason ->
                viewModel.addNumber(number, reason)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    item: BlockedNumber,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        },
    )

    val bgColor by animateColorAsState(
        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
            StatusBlocked.copy(alpha = 0.85f)
        else
            DarkSurfaceVar,
        label = "swipe_bg",
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        modifier = Modifier.padding(horizontal = 12.dp),
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = Color.White,
                )
            }
        },
        content = {
            ListItem(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                headlineContent = {
                    Text(
                        text = item.phoneNumber,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnDarkPrimary,
                    )
                },
                supportingContent = {
                    Text(
                        text = item.label ?: "",
                        fontSize = 12.sp,
                        color = OnDarkSecondary,
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = StatusBlocked,
                        modifier = Modifier.size(24.dp),
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = DarkSurfaceVar,
                ),
            )
        },
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = OnDarkDisabled,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Engellenen numara yok",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = OnDarkSecondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Şüpheli numaraları manuel olarak eklemek için + butonuna dokunun.",
            fontSize = 12.sp,
            color = OnDarkDisabled,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AddNumberDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var number by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = BEngelAccent,
        unfocusedBorderColor = DarkBorder,
        cursorColor          = BEngelAccent,
        focusedLabelColor    = BEngelAccent,
        unfocusedLabelColor  = OnDarkSecondary,
        focusedTextColor     = OnDarkPrimary,
        unfocusedTextColor   = OnDarkPrimary,
    )

    AlertDialog(
        onDismissRequest  = onDismiss,
        containerColor    = DarkSurfaceVar,
        titleContentColor = OnDarkPrimary,
        textContentColor  = OnDarkSecondary,
        title = { Text("Numara Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value           = number,
                    onValueChange   = { number = it },
                    label           = { Text("Telefon numarası") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier        = Modifier.fillMaxWidth(),
                    colors          = textFieldColors,
                )
                OutlinedTextField(
                    value         = reason,
                    onValueChange = { reason = it },
                    label         = { Text("Sebep (isteğe bağlı)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = textFieldColors,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(number, reason) },
                enabled = number.isNotBlank(),
            ) {
                Text("Ekle", color = BEngelAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = OnDarkSecondary)
            }
        },
    )
}
