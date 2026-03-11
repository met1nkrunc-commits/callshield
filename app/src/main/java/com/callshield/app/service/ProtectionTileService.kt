package com.callshield.app.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.callshield.app.data.repository.ProtectionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class ProtectionTileService : TileService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TileEntryPoint {
        fun protectionRepository(): ProtectionRepository
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val repo by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            TileEntryPoint::class.java,
        ).protectionRepository()
    }

    override fun onStartListening() {
        super.onStartListening()
        scope.launch {
            val enabled = repo.isEnabled.first()
            updateTile(enabled)
        }
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            repo.toggle()
            val enabled = repo.isEnabled.first()
            updateTile(enabled)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun updateTile(enabled: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Callshield"
        tile.contentDescription = if (enabled) "Koruma aktif" else "Koruma kapalı"
        tile.updateTile()
    }
}
