package com.example.fintreker.data.repository

import com.example.fintreker.data.local.RateDao
import com.example.fintreker.data.local.RateEntity
import com.example.fintreker.data.remote.NbuApi
import com.example.fintreker.data.remote.toEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

sealed interface SyncResult {
    data object Success : SyncResult
    data class Failure(val message: String) : SyncResult
}

/**
 * Offline-First репозиторій курсів валют (Лаб 4).
 * UI читає лише [rates] (Room = Single Source of Truth); [refresh] оновлює кеш з мережі,
 * а помилки мережі не кидає, а повертає як [SyncResult.Failure] — додаток працює з кешу.
 */
class RatesRepository(
    private val api: NbuApi,
    private val dao: RateDao,
    private val clock: () -> Long = System::currentTimeMillis
) {
    val rates: Flow<List<RateEntity>> = dao.observeRates()

    suspend fun refresh(): SyncResult = try {
        val now = clock()
        val entities = api.getExchangeRates()
            .mapNotNull { it.toEntity(now) }
            .filter { it.code in SUPPORTED_CODES }

        if (entities.isEmpty()) {
            SyncResult.Failure("НБУ не повернув курсів USD/EUR")
        } else {
            dao.upsertAll(entities)
            SyncResult.Success
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        SyncResult.Failure("Немає з'єднання з мережею")
    } catch (e: HttpException) {
        SyncResult.Failure("Помилка сервера НБУ (${e.code()})")
    } catch (e: Exception) {
        SyncResult.Failure(e.message ?: "Невідома помилка")
    }

    companion object {
        const val USD = "USD"
        const val EUR = "EUR"
        val SUPPORTED_CODES = setOf(USD, EUR)
    }
}
