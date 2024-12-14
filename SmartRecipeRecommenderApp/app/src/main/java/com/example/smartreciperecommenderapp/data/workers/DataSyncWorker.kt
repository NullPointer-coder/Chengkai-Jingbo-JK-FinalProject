package com.example.smartreciperecommenderapp.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val ingredientRepository: IngredientRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                ingredientRepository.syncIngredients()
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }
}
