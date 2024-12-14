package com.example.smartreciperecommenderapp

import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.example.smartreciperecommenderapp.data.model.IngredientDao
import com.example.smartreciperecommenderapp.data.model.IngredientEntity
import com.example.smartreciperecommenderapp.data.repository.FirebaseIngredientService
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class IngredientsRepositoryTest {

    @Mock
    private lateinit var mockDao: IngredientDao

    @Mock
    private lateinit var mockFirebaseService: FirebaseIngredientService

    private lateinit var repository: IngredientRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = IngredientRepository(mockDao, mockFirebaseService)
    }

    @Test
    fun `getAllIngredientsFromRoom returns empty list when dao is empty`() = runBlocking {
        // Given dao returns empty list
        whenever(mockDao.getAllIngredients()).thenReturn(emptyList())

        // When
        val ingredients = repository.getAllIngredientsFromRoom()

        // Then
        assertThat(ingredients).isEmpty()
    }

    @Test
    fun `getAllIngredientsFromRoom returns mapped list when dao returns entities`() = runBlocking {
        // Given dao returns some IngredientEntities
        val entityList = listOf(
            IngredientEntity(
                instanceId = 1,
                id = 101,
                name = "Tomato",
                quantity = 5.0,
                unit = "pieces",
                category = "Vegetable",
                expiryDate = null,
                imageUrl = null,
                calories = null,
                fat = null,
                deleted = false,
                pendingSync = false
            ),
            IngredientEntity(
                instanceId = 2,
                id = 202,
                name = "Milk",
                quantity = 2.0,
                unit = "liters",
                category = "Dairy",
                expiryDate = null,
                imageUrl = "http://example.com/milk.jpg",
                calories = 50.0,
                fat = 1.5,
                deleted = false,
                pendingSync = false
            )
        )
        whenever(mockDao.getAllIngredients()).thenReturn(entityList)

        // When
        val ingredients = repository.getAllIngredientsFromRoom()

        // Then: verify mapping correctness
        assertThat(ingredients).hasSize(2)

        val first = ingredients[0]
        assertThat(first.instanceId).isEqualTo(1)
        assertThat(first.name).isEqualTo("Tomato")
        assertThat(first.quantity).isEqualTo(5.0)
        assertThat(first.unit).isEqualTo("pieces")
        assertThat(first.category).isEqualTo("Vegetable")
        assertThat(first.imageUrl).isNull()
        assertThat(first.calories).isNull()
        assertThat(first.fat).isNull()

        val second = ingredients[1]
        assertThat(second.instanceId).isEqualTo(2)
        assertThat(second.name).isEqualTo("Milk")
        assertThat(second.quantity).isEqualTo(2.0)
        assertThat(second.unit).isEqualTo("liters")
        assertThat(second.category).isEqualTo("Dairy")
        assertThat(second.imageUrl).isEqualTo("http://example.com/milk.jpg")
        assertThat(second.calories).isEqualTo(50.0)
        assertThat(second.fat).isEqualTo(1.5)
    }
}
