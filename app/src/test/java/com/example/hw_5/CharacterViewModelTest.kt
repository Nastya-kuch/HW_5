package com.example.hw_5

import com.example.hw_5.data.model.Character
import com.example.hw_5.data.repository.CharacterRepository
import com.example.hw_5.data.repository.SearchCacheRepository
import com.example.hw_5.data.repository.PageInfo
import com.example.hw_5.ui.viewmodel.CharacterViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: CharacterRepository
    private lateinit var cacheRepository: SearchCacheRepository
    private lateinit var viewModel: CharacterViewModel

    private val fakeCharacters = listOf(
        Character(1, "Rick Sanchez", "Alive", "Human", "", "Male", "Earth", "Citadel", 51),
        Character(2, "Morty Smith", "Alive", "Human", "", "Male", "Earth", "Earth", 51)
    )

    @Before
    fun setup() {
        repository = mockk()
        cacheRepository = mockk()
        coEvery { cacheRepository.getLastSearchResult() } returns null
        coEvery { cacheRepository.saveSearchResult(any(), any()) } returns Unit
    }

    @Test
    fun `initial state is loading`() = runTest {
        coEvery { repository.getCharactersPage(1) } coAnswers {
            delay(1000)
            fakeCharacters
        }
        coEvery { repository.getPagesInfo() } returns PageInfo(42, true)

        viewModel = CharacterViewModel(repository, cacheRepository)

        assertTrue(viewModel.uiState.isLoading)
        assertTrue(viewModel.uiState.characters.isEmpty())
    }

    @Test
    fun `loadFirstPage success sets characters`() = runTest {
        coEvery { repository.getCharactersPage(1) } returns fakeCharacters
        coEvery { repository.getPagesInfo() } returns PageInfo(42, true)

        viewModel = CharacterViewModel(repository, cacheRepository)
        advanceUntilIdle()

        assertEquals(fakeCharacters, viewModel.uiState.characters)
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun `loadFirstPage error with no cache shows error message`() = runTest {
        coEvery { repository.getCharactersPage(1) } throws RuntimeException("No internet")

        viewModel = CharacterViewModel(repository, cacheRepository)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isLoading)
        assertTrue(viewModel.uiState.characters.isEmpty())
    }

    @Test
    fun `retry after error loads data successfully`() = runTest {
        coEvery { repository.getCharactersPage(1) } throws RuntimeException("No internet") andThen fakeCharacters
        coEvery { repository.getPagesInfo() } returns PageInfo(42, true)

        viewModel = CharacterViewModel(repository, cacheRepository)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.errorMessage)

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(fakeCharacters, viewModel.uiState.characters)
        assertNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun `search with empty result sets isEmptySearchResult true`() = runTest {
        coEvery { repository.getCharactersPage(1) } returns fakeCharacters
        coEvery { repository.getPagesInfo() } returns PageInfo(42, true)
        coEvery { repository.searchCharacters("xyz") } returns emptyList()

        viewModel = CharacterViewModel(repository, cacheRepository)
        advanceUntilIdle()

        viewModel.updateSearchQuery("xyz")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.isEmptySearchResult)
        assertTrue(viewModel.uiState.characters.isEmpty())
        assertNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun `repeated search for same query does not duplicate characters`() = runTest {
        coEvery { repository.getCharactersPage(1) } returns fakeCharacters
        coEvery { repository.getPagesInfo() } returns PageInfo(42, true)
        coEvery { repository.searchCharacters("Rick") } returns listOf(fakeCharacters[0])

        viewModel = CharacterViewModel(repository, cacheRepository)
        advanceUntilIdle()

        viewModel.updateSearchQuery("Rick")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.characters.size)
        val firstCharacter = viewModel.uiState.characters[0]

        viewModel.updateSearchQuery("Rick")
        advanceUntilIdle()


        assertEquals(1, viewModel.uiState.characters.size)
        assertEquals(firstCharacter.id, viewModel.uiState.characters[0].id)
        assertEquals(firstCharacter.name, viewModel.uiState.characters[0].name)
    }

    @Test
    fun `retry calls repository again`() = runTest {
        coEvery { repository.getCharactersPage(1) } throws RuntimeException("fail") andThen fakeCharacters
        coEvery { repository.getPagesInfo() } returns PageInfo(1, false)

        viewModel = CharacterViewModel(repository, cacheRepository)
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.getCharactersPage(1) }
    }
}