package com.example.smartreciperecommenderapp

import com.example.smartreciperecommenderapp.data.model.RecipeDetailDao
import com.example.smartreciperecommenderapp.data.model.RecipeDetailEntity
import com.example.smartreciperecommenderapp.data.repository.RecipeDetailRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class RecipeDetailRepositoryTest {

    @Mock
    private lateinit var mockDao: RecipeDetailDao

    private lateinit var repository: RecipeDetailRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = RecipeDetailRepository(mockDao)
    }

    @Test
    fun `getRecipeDetailsFromDB returns null when dao returns null`() = runBlocking {
        // Given dao returns null
        whenever(mockDao.getRecipeDetail(123L)).thenReturn(null)

        // When
        val result = repository.getRecipeDetailsFromDB(123L)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getRecipeDetailsFromDB returns mapped model when dao returns entity`(): Unit = runBlocking {
        // Given dao returns a RecipeDetailEntity
        val testEntity = RecipeDetailEntity(
            recipeId = 456L,
            name = "Test Recipe",
            description = "Test Description",
            servings = 4.0,
            prepTime = 10,
            cookTime = 20,
            gramsPerPortion = 100.0,
            rating = 5,
            categoriesJson = """{"recipe_category":[{"recipe_category_name":"Dessert"}]}""",
            typesJson = """{"recipe_type":["Main Dish"]}""",
            servingSizesJson = """{"serving":{"serving_size":"100g","calories":100}}""",
            ingredientsJson = """{"ingredient":[{"food_name":"Sugar"}]}""",
            directionsJson = """{"direction":[{"direction_description":"Mix thoroughly"}]}""",
            imageUrl = "http://example.com/image.jpg"
        )
        whenever(mockDao.getRecipeDetail(456L)).thenReturn(testEntity)

        // When
        val result = repository.getRecipeDetailsFromDB(456L)

        // Then
        assertThat(result).isNotNull()
        result?.let {
            assertThat(it.recipeId).isEqualTo(456L)
            assertThat(it.name).isEqualTo("Test Recipe")
            assertThat(it.description).isEqualTo("Test Description")
            assertThat(it.servings).isEqualTo(4.0)
            assertThat(it.prepTime).isEqualTo(10)
            assertThat(it.cookTime).isEqualTo(20)
            assertThat(it.gramsPerPortion).isEqualTo(100.0)
            assertThat(it.rating).isEqualTo(5)
            assertThat(it.categoriesJson).contains("Dessert")
            assertThat(it.typesJson).contains("Main Dish")
            assertThat(it.servingSizesJson).contains("100g")
            assertThat(it.ingredientsJson).contains("Sugar")
            assertThat(it.directionsJson).contains("Mix thoroughly")
            assertThat(it.imageUrl).isEqualTo("http://example.com/image.jpg")
        }
    }
}
