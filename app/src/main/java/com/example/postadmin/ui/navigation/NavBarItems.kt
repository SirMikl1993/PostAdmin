package com.example.postadmin.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import com.example.postadmin.data.models.BarItem

object NavBarItems {
    val BarItems = listOf(
        BarItem(
            title = "Добавление поста",
            image = Icons.Filled.Home,
            route = "postForm"
        ),
        BarItem(
            title = "Список постов",
            image = Icons.Filled.Face,
            route = "posts"
        ),
        BarItem(
            title = "О приложении",
            image = Icons.Filled.Info,
            route = "about"
        ),
        BarItem(
            title = "Управление категориями",
            image = Icons.Filled.Category,
            route = "categoryManager"
        ),
        BarItem(
            title = "Фильтрация и сортировка",
            image = Icons.Filled.Info,
            route = "filteredPosts"
        )
    )
}