package com.example.smartreciperecommenderapp.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [IngredientEntity::class],
    version = 2,  // Updated to the latest version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retrieve the database instance.
         * If the database does not exist yet, create it using the Room builder
         * and apply any necessary migrations.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2)  // Register migration logic
                    .fallbackToDestructiveMigration() // Rebuild the database if migration fails (development only)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Define migration logic from version 1 to 2.
         * Here we add a new column 'instanceId' to the 'ingredient' table.
         */
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add a new non-nullable column 'instanceId' with default value 0
                database.execSQL("ALTER TABLE ingredient ADD COLUMN instanceId INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
