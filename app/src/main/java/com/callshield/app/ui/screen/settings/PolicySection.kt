package com.callshield.app.ui.screen.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary

@Composable
internal fun PolicySection(title: String, body: String) {
    Text(
        text       = title,
        fontSize   = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color      = OnDarkPrimary,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text       = body,
        fontSize   = 13.sp,
        color      = OnDarkSecondary,
        lineHeight = 20.sp,
    )
    Spacer(Modifier.height(20.dp))
}
