package com.example.todolist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todolist.ui.home.HomeScreen
import com.example.todolist.ui.settings.SettingsScreen
import com.example.todolist.ui.task.TaskFormScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")

    object TaskForm : Screen("task?id={id}") {
        fun route(id: Long? = null) = if (id != null) "task?id=$id" else "task"
    }
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddTask     = { navController.navigate(Screen.TaskForm.route()) },
                onTaskClick   = { id -> navController.navigate(Screen.TaskForm.route(id)) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route     = Screen.TaskForm.route,
            arguments = listOf(
                navArgument("id") {
                    type         = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStack ->
            val taskId = backStack.arguments?.getLong("id")?.takeIf { it != -1L }
            TaskFormScreen(
                taskId         = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
