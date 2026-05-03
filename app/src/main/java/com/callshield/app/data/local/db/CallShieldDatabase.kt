package com.callshield.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.callshield.app.data.local.db.dao.BlockEventDao
import com.callshield.app.data.local.db.dao.BlockedNumberDao
import com.callshield.app.data.local.db.dao.BlockedSmsDao
import com.callshield.app.data.local.db.dao.NumberQueryCacheDao
import com.callshield.app.data.local.db.dao.PhishingUrlDao
import com.callshield.app.data.local.db.dao.TrustedNumberDao
import com.callshield.app.data.local.db.entity.BlockEventEntity
import com.callshield.app.data.local.db.entity.BlockedNumberEntity
import com.callshield.app.data.local.db.entity.BlockedSmsEntity
import com.callshield.app.data.local.db.entity.NumberQueryCacheEntity
import com.callshield.app.data.local.db.entity.PhishingUrlEntity
import com.callshield.app.data.local.db.entity.TrustedNumberEntity

@Database(
    entities = [
        BlockedNumberEntity::class,
        BlockEventEntity::class,
        NumberQueryCacheEntity::class,
        TrustedNumberEntity::class,
        PhishingUrlEntity::class,
        BlockedSmsEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class CallShieldDatabase : RoomDatabase() {

    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun blockEventDao(): BlockEventDao
    abstract fun numberQueryCacheDao(): NumberQueryCacheDao
    abstract fun trustedNumberDao(): TrustedNumberDao
    abstract fun phishingUrlDao(): PhishingUrlDao
    abstract fun blockedSmsDao(): BlockedSmsDao

    companion object {
        // Fix #15: blocked_numbers tablosuna riskLevel + source eklendi.
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE blocked_numbers ADD COLUMN riskLevel TEXT NOT NULL DEFAULT 'UNKNOWN'")
                database.execSQL("ALTER TABLE blocked_numbers ADD COLUMN source TEXT NOT NULL DEFAULT 'manual'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE block_events ADD COLUMN sender TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS blocked_sms (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sender TEXT NOT NULL,
                        body TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        riskLevel TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        // Fix #11: Version 1 → 2: block_events tablosu eklendi.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS block_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        riskLevel TEXT NOT NULL,
                        category TEXT NOT NULL,
                        senderHash INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS phishing_urls (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        url TEXT NOT NULL,
                        sender TEXT NOT NULL,
                        snippet TEXT NOT NULL,
                        detectedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS trusted_numbers (
                        phoneNumber TEXT NOT NULL PRIMARY KEY,
                        label       TEXT NOT NULL DEFAULT '',
                        addedAt     INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

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
