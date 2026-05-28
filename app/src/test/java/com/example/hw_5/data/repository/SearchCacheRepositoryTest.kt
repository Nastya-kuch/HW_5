package com.example.hw_5.data.repository

import com.example.hw_5.data.local.CharacterDao
import com.example.hw_5.data.model.Character
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SearchCacheRepositoryTest {

    private val dao: CharacterDao = mockk()
    private lateinit var cacheRepository: SearchCacheRepository

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)
        coEvery { android.util.Log.e(any(), any()) } returns 0
        coEvery { android.util.Log.d(any(), any()) } returns 0

        cacheRepository = SearchCacheRepository(dao)
    }

    @Test
    fun `saveSearchResult deletes previous entries before insert to avoid duplicates`() = runTest {
        val query = "Rick"
        val characters = listOf(
            Character(1, "Rick", "Alive", "Human", "", "Male", "Earth", "Citadel", 10)
        )

        coEvery { dao.deleteByQuery(query) } returns Unit
        coEvery { dao.insertAll(any()) } returns Unit

        cacheRepository.saveSearchResult(query, characters)

        coVerify(exactly = 1) { dao.deleteByQuery(query) }
        coVerify(exactly = 1) { dao.insertAll(any()) }
    }
}