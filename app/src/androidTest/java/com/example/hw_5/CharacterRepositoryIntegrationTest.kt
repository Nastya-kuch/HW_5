package com.example.hw_5

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.hw_5.data.api.ApiService
import com.example.hw_5.data.api.CharacterFromApi
import com.example.hw_5.data.api.CharacterResponse
import com.example.hw_5.data.api.Info
import com.example.hw_5.data.api.OriginFromApi
import com.example.hw_5.data.api.LocationFromApi
import com.example.hw_5.data.local.AppDatabase
import com.example.hw_5.data.repository.CharacterRepository
import com.example.hw_5.data.repository.SearchCacheRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CharacterRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var cacheRepository: SearchCacheRepository
    private lateinit var apiService: ApiService
    private lateinit var repository: CharacterRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        apiService = mockk()
        cacheRepository = SearchCacheRepository(database.characterDao())
        repository = CharacterRepository(apiService)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun repositoryFetchesFromApiAndSavesToCache() = runTest {
        val apiCharacter = CharacterFromApi(
            id = 1,
            name = "Rick",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            origin = OriginFromApi("Earth", ""),
            location = LocationFromApi("Citadel", ""),
            episode = listOf("e1")
        )
        val response = CharacterResponse(
            info = Info(1, 1, null, null),
            results = listOf(apiCharacter)
        )
        coEvery { apiService.getCharacters(1) } returns response
        val characters = repository.getCharactersPage(1)
        cacheRepository.saveSearchResult("", characters)
        val cached = cacheRepository.getSearchResult("")
        assertEquals(1, cached?.size)
        assertEquals("Rick", cached?.first()?.name)
        assertEquals(1, cached?.first()?.id)
    }
}