package com.example.smartreciperecommenderapp

import com.example.smartreciperecommenderapp.data.model.RecipeDao
import com.example.smartreciperecommenderapp.data.model.RecipeEntity
import com.example.smartreciperecommenderapp.data.repository.RecipeRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class RecipesRepositoryTest {

    @Mock
    private lateinit var mockDao: RecipeDao

    private lateinit var repository: RecipeRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = RecipeRepository(mockDao)
    }

    @Test
    fun `getAllRecipesFromDB returns empty list when dao returns empty list`() = runBlocking {
        whenever(mockDao.getAllRecipes()).thenReturn(emptyList())

        val recipes = repository.getAllRecipesFromDB()

        assertThat(recipes).isEmpty()
    }

    @Test
    fun `getAllRecipesFromDB returns mapped list when dao returns entities`() = runBlocking {
        val entities = listOf(
            RecipeEntity(
                id = 1L,
                name = "Test Recipe",
                description = "A delicious test recipe",
                imageUrl = "http://example.com/image.jpg",
                calories = "200",
                carbohydrate = "20g",
                fat = "10g",
                protein = "5g",
                ingredients = listOf("Egg", "Flour"),
                types = listOf("Breakfast"),
                originIngredient = "Flour"
            )
        )
        whenever(mockDao.getAllRecipes()).thenReturn(entities)

        val recipes = repository.getAllRecipesFromDB()

        assertThat(recipes).hasSize(1)
        val recipe = recipes[0]
        assertThat(recipe.id).isEqualTo(1L)
        assertThat(recipe.name).isEqualTo("Test Recipe")
        assertThat(recipe.description).isEqualTo("A delicious test recipe")
        assertThat(recipe.imageUrl).isEqualTo("http://example.com/image.jpg")
        assertThat(recipe.calories).isEqualTo("200")
        assertThat(recipe.carbohydrate).isEqualTo("20g")
        assertThat(recipe.fat).isEqualTo("10g")
        assertThat(recipe.protein).isEqualTo("5g")
        assertThat(recipe.ingredients).containsExactly("Egg", "Flour")
        assertThat(recipe.types).containsExactly("Breakfast")
        assertThat(recipe.originIngredient).isEqualTo("Flour")
    }
}
