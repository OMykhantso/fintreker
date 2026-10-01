package com.example.expensetracker

import com.example.expensetracker.data.local.RateDao
import com.example.expensetracker.data.remote.NbuApi
import com.example.expensetracker.data.remote.NbuRateDto
import com.example.expensetracker.data.repository.RatesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RatesRepositoryTest {
    private val dao = mockk<RateDao>(relaxed = true).also { every { it.observeAll() } returns flowOf(emptyList()) }
    private val api = mockk<NbuApi>()

    @Test fun `refresh saves rates to cache on success`() = runTest {
        coEvery { api.getRate("USD") } returns listOf(NbuRateDto("USD", 41.2, "01.10.2026"))
        coEvery { api.getRate("EUR") } returns listOf(NbuRateDto("EUR", 44.8, "01.10.2026"))

        assertTrue(RatesRepository(api, dao).refresh())
        coVerify(exactly = 1) { dao.upsertAll(match { it.size == 2 }) }
    }

    @Test fun `refresh offline returns false and keeps cache untouched`() = runTest {
        coEvery { api.getRate(any()) } throws IOException("no internet")

        assertFalse(RatesRepository(api, dao).refresh())
        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }

    @Test fun `zero rate from server is ignored`() = runTest {
        coEvery { api.getRate(any()) } returns listOf(NbuRateDto("USD", 0.0, "01.10.2026"))

        assertFalse(RatesRepository(api, dao).refresh())
        coVerify(exactly = 0) { dao.upsertAll(any()) }
    }
}
