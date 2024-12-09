package com.example.smartreciperecommenderapp.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [IngredientEntity::class],
    version = 2,  // 更新到最新版本
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库实例
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2)  // 注册迁移逻辑
                    .fallbackToDestructiveMigration() // 若迁移失败则重建数据库（仅开发阶段）
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * 定义迁移逻辑：从版本 1 到 2
         */
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ingredient ADD COLUMN instanceId INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
