package com.example.smartreciperecommenderapp.ui.ProfileScreen

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.smartreciperecommenderapp.data.model.AppDatabase
import com.example.smartreciperecommenderapp.data.repository.UserRepository
import com.example.smartreciperecommenderapp.ui.navigation.NavGraph


class ProfileFragment : Fragment() {
    /*

    private val userRepository by lazy { UserRepository() }
    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(
            this,
            ProfileViewModelFactory(userRepository)
        )[ProfileViewModel::class.java]
    }

    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                // 使用 NavController 进行导航
                val navController = rememberNavController()

                database = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "smart_recipe_db"
                ).build()

                val categoriesViewModel = CategoriesViewModel(database.categoryDao())


                // 提供 ProfileScreen 所需的 ViewModel 和导航控制器
                CompositionLocalProvider {
                    NavGraph(
                        profileViewModel = profileViewModel,
                        navController = navController,
                        categories = categories
                    )
                }
            }
        }
    }

     */
}
