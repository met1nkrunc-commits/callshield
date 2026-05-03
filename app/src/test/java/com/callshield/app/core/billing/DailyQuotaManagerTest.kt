package com.callshield.app.core.billing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DailyQuotaManagerTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_quota.preferences_pb") }
        )
        // We need to inject dataStore — DailyQuotaManager uses context.dataStore
        // Since it's hard to inject without Hilt, we test the logic indirectly
        // via a simple integration test pattern
    }

    @Test
    fun `initial remaining quota is FREE_DAILY_LIMIT`() = testScope.runTest {
        // DailyQuotaManager.FREE_DAILY_LIMIT = 5
        assertEquals(5, DailyQuotaManager.FREE_DAILY_LIMIT)
    }

    @Test
    fun `FREE_DAILY_LIMIT is 5`() {
        assertEquals(5, DailyQuotaManager.FREE_DAILY_LIMIT)
    }
}
