package com.example.hw_5.data.repository

import com.example.hw_5.data.api.ApiService
import com.example.hw_5.data.api.CharacterFromApi
import com.example.hw_5.data.api.CharacterResponse
import com.example.hw_5.data.api.Info
import com.example.hw_5.data.api.OriginFromApi
import com.example.hw_5.data.api.LocationFromApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterRepositoryTest {

    private val apiService: ApiService = mockk()
    private val repository = CharacterRepository(apiService)

    @Test
    fun `getCharactersPage converts API response correctly`() = runTest {
        val apiCharacter = CharacterFromApi(
            id = 1,
            name = "Rick",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            origin = OriginFromApi("Earth", ""),
            location = LocationFromApi("Citadel", ""),
            episode = listOf("ep1", "ep2")
        )
        val response = CharacterResponse(
            info = Info(1, 1, null, null),
            results = listOf(apiCharacter)
        )
        coEvery { apiService.getCharacters(page = 1) } returns response

        val result = repository.getCharactersPage(1)

        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals(1, id)
            assertEquals("Rick", name)
            assertEquals("Alive", status)
            assertEquals("Human", species)
            assertEquals("", type)
            assertEquals("Male", gender)
            assertEquals("Earth", origin)
            assertEquals("Citadel", location)
            assertEquals(2, episodeCount)
        }
    }
}