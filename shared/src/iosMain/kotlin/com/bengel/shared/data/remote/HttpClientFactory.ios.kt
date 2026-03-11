package com.bengel.shared.data.remote

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

actual fun createHttpClient(): HttpClient = buildHttpClient(Darwin.create())
