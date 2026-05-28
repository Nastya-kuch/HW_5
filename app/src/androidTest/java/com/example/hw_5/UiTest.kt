package com.example.hw_5

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw_5.data.model.Character
import com.example.hw_5.data.repository.CharacterRepository
import com.example.hw_5.data.repository.PageInfo
import com.example.hw_5.data.repository.SearchCacheRepository
import com.example.hw_5.ui.screens.ListScreen
import com.example.hw_5.ui.viewmodel.CharacterViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeCharacters = listOf(
        Character(1, "Rick Sanchez", "Alive", "Human", "", "Male", "Earth", "Citadel", 51)
    )

    @Test
    fun error_thenRetry_showsData() {
        val repository = mockk<CharacterRepository>()
        val cacheRepository = mockk<SearchCacheRepository>()

        coEvery { repository.getCharactersPage(1) } throws
                RuntimeException("No internet") andThen fakeCharacters
        coEvery { repository.getPagesInfo() } returns PageInfo(1, false)
        coEvery { cacheRepository.getLastSearchResult() } returns null
        coEvery { cacheRepository.saveSearchResult(any(), any()) } returns Unit

        val viewModel = CharacterViewModel(repository, cacheRepository)

        composeTestRule.setContent {
            ListScreen(
                uiState = viewModel.uiState,
                onSearchQueryChange = {},
                onCharacterClick = {},
                onRetry = { viewModel.retry() },
                onLoadNextPage = {}
            )
        }

        composeTestRule.waitUntil(5000) {
            composeTestRule
                .onAllNodesWithText("Повторить")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithText("Повторить").assertIsDisplayed()
        composeTestRule.onNodeWithText("Повторить").performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule
                .onAllNodesWithText("Rick Sanchez")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithText("Rick Sanchez").assertIsDisplayed()
    }
}