package com.example.hw_5

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw_5.data.local.AppDatabase
import com.example.hw_5.data.local.CharacterDao
import com.example.hw_5.data.local.CharacterEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CharacterDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.characterDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadCharacters() = runTest {
        val entities = listOf(
            CharacterEntity(
                searchQuery = "Rick",
                characterId = 1,
                name = "Rick Sanchez",
                status = "Alive",
                species = "Human",
                type = "",
                gender = "Male",
                origin = "Earth",
                location = "Citadel",
                episodeCount = 51
            )
        )

        dao.insertAll(entities)
        val result = dao.getByQuery("Rick")

        assertEquals(1, result.size)
        assertEquals("Rick Sanchez", result[0].name)
        assertEquals(1, result[0].characterId)
    }

    @Test
    fun insertSameQueryTwiceNoDuplicates() = runTest {
        val entity = CharacterEntity(
            searchQuery = "Rick",
            characterId = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            origin = "Earth",
            location = "Citadel",
            episodeCount = 51
        )

        dao.insertAll(listOf(entity))
        dao.deleteByQuery("Rick")
        dao.insertAll(listOf(entity))

        val result = dao.getByQuery("Rick")
        assertEquals(1, result.size)
    }

    @Test
    fun getLastQueryReturnsCorrectQuery() = runTest {
        dao.insertAll(listOf(
            CharacterEntity(
                searchQuery = "Morty",
                characterId = 2,
                name = "Morty Smith",
                status = "Alive",
                species = "Human",
                type = "",
                gender = "Male",
                origin = "Earth",
                location = "Earth",
                episodeCount = 51
            )
        ))

        val lastQuery = dao.getLastQuery()
        assertEquals("Morty", lastQuery)
    }
}