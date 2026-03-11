package com.callshield.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.callshield.app.data.local.db.dao.BlockEventDao
import com.callshield.app.data.local.db.dao.BlockedNumberDao
import com.callshield.app.data.local.db.dao.NumberQueryCacheDao
import com.callshield.app.data.local.db.entity.BlockEventEntity
import com.callshield.app.data.local.db.entity.BlockedNumberEntity
import com.callshield.app.data.local.db.entity.NumberQueryCacheEntity

@Database(
    entities = [
        BlockedNumberEntity::class,
        BlockEventEntity::class,
        NumberQueryCacheEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class CallShieldDatabase : RoomDatabase() {

    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun blockEventDao(): BlockEventDao
    abstract fun numberQueryCacheDao(): NumberQueryCacheDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS number_query_cache (
                        normalizedNumber TEXT NOT NULL PRIMARY KEY,
                        spamScore        INTEGER NOT NULL,
                        fraudScore       INTEGER NOT NULL,
                        isSpam           INTEGER NOT NULL,
                        isFraud          INTEGER NOT NULL,
                        carrier          TEXT,
                        lineType         TEXT,
                        cachedAt         INTEGER NOT NULL,
                        source           TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
