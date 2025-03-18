package com.example.postadmin.ui.navigation

sealed class NavRoutes(val route: String) {
    object PostForm : NavRoutes("postForm")
    object Posts : NavRoutes("posts")
    object About : NavRoutes("about")
    object CategoryManager : NavRoutes("categoryManager")
    object FilteredPosts : NavRoutes("filteredPosts")
}