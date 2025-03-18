package com.example.postadmin.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.postadmin.ui.navigation.BottomNavigationBar
import com.example.postadmin.ui.navigation.NavRoutes

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Column(Modifier.padding(8.dp)) {
        NavHost(
            navController,
            startDestination = NavRoutes.PostForm.route,
            modifier = Modifier.weight(1f)
        ) {
            composable(NavRoutes.PostForm.route) { PostFormScreen() }
            composable(NavRoutes.Posts.route) { PostsScreen() }
            composable(NavRoutes.About.route) { AboutScreen() }
            composable(NavRoutes.CategoryManager.route) { CategoryManagerScreen() }
            composable(NavRoutes.FilteredPosts.route) { FilteredPostsScreen() }
        }
        BottomNavigationBar(navController = navController)
    }
}