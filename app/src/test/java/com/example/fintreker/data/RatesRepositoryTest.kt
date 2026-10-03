package com.example.fintreker.data

import com.example.fintreker.data.local.RateDao
import com.example.fintreker.data.local.RateEntity
import com.example.fintreker.data.remote.NbuApi
import com.example.fintreker.data.remote.NbuRateDto
import com.example.fintreker.data.repository.RatesRepository
import com.example.fintreker.data.repository.SyncResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/** Offline-First: мережеві збої не кидають винятків і не псують кеш. */
class RatesRepositoryTest {

    private val api = mockk<NbuApi>()
    private val dao = mockk<RateDao>()
    private val now = 1_790_000_000_000L
    private lateinit var repository: RatesRepository

    private fun dto(code: String?, rate: Double?) =
        NbuRateDto(numericCode = 1, name = "name", rate = rate, code = code, exchangeDate = "01.10.2026")

    @Before
    fun setUp() {
        every { dao.observeRates() } returns flowOf(emptyList())
        coEvery { dao.upsertAll(any()) } just runs
        repository = RatesRepository(api, dao) { now }
    }

    @Test
    fun `stores only USD and EUR from the full NBU list`() = runTest {
        coEvery { api.getExchangeRates() } returns listOf(
            dto("USD", 41.5),
            dto("PLN", 10.2),
            dto("EUR", 48.0),
            dto("GBP", 55.0)
        )
        val saved = slot<List<RateEntity>>()
        coEvery { dao.upsertAll(capture(saved)) } just runs

        val result = repository.refresh()

        assertEquals(SyncResult.Success, result)
        assertEquals(
            listOf(
                RateEntity("USD", 41.5, "01.10.2026", now),
                RateEntity("EUR", 48.0, "01.10.2026", now)
            ),
            saved.captured
        )
    }

    @Test
    fun `invalid records are dropped`() = runTest {
        coEvery { api.getExchangeRates() } returns listOf(
            dto("USD", 41.5),
            dto("EUR", 0.0), // нульовий курс
            dto(null, 10.0)  // без коду
        )
        val saved = slot<List<RateEntity>>()
        coEvery { dao.upsertAll(capture(saved)) } just runs

        repository.refresh()

        assertEquals(listOf("USD"), saved.captured.map { it.code })
    }

    @Test
    fun `no connection returns Failure and leaves the cache untouched`() = runTest {
        coEvery { api.getExchangeRates() } throws IOException("Unable to resolve host")

        val result = repository.refresh()

        assertEquals(SyncResult.Failure("Немає з'єднання з мережею"), result)
        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }

    @Test
    fun `timeout is treated like a network failure`() = runTest {
        coEvery { api.getExchangeRates() } throws SocketTimeoutException("timeout")

        val result = repository.refresh()

        assertEquals(SyncResult.Failure("Немає з'єднання з мережею"), result)
    }

    @Test
    fun `server error returns Failure with the http code`() = runTest {
        val response = Response.error<List<NbuRateDto>>(503, "".toResponseBody(null))
        coEvery { api.getExchangeRates() } throws HttpException(response)

        val result = repository.refresh()

        assertEquals(SyncResult.Failure("Помилка сервера НБУ (503)"), result)
        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }

    @Test
    fun `empty response is a Failure and does not wipe the cache`() = runTest {
        coEvery { api.getExchangeRates() } returns emptyList()

        val result = repository.refresh()

        assertTrue(result is SyncResult.Failure)
        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }

    @Test
    fun `response without USD and EUR is a Failure`() = runTest {
        coEvery { api.getExchangeRates() } returns listOf(dto("PLN", 10.0))

        assertTrue(repository.refresh() is SyncResult.Failure)
        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }

    @Test
    fun `unexpected parsing error is reported with its message`() = runTest {
        coEvery { api.getExchangeRates() } throws IllegalStateException("Expected BEGIN_ARRAY")

        assertEquals(SyncResult.Failure("Expected BEGIN_ARRAY"), repository.refresh())
    }

    @Test
    fun `cancellation is propagated and not swallowed`() = runTest {
        coEvery { api.getExchangeRates() } throws CancellationException("cancelled")

        try {
            repository.refresh()
            fail("CancellationException must propagate")
        } catch (e: CancellationException) {
            assertEquals("cancelled", e.message)
        }
    }

    @Test
    fun `rates flow is the database flow`() = runTest {
        val cached = listOf(RateEntity("USD", 41.0, "30.09.2026", 1L))
        every { dao.observeRates() } returns flowOf(cached)

        val fresh = RatesRepository(api, dao) { now }

        var collected: List<RateEntity>? = null
        fresh.rates.collect { collected = it }
        assertEquals(cached, collected)
    }
}
