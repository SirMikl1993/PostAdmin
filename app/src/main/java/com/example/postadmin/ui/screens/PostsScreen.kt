package com.example.postadmin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.postadmin.data.models.Post
import com.example.postadmin.data.repository.PostRepository
import com.example.postadmin.ui.components.PostItem

@Composable
fun PostsScreen() {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val context = LocalContext.current
    val postRepository = PostRepository()

    LaunchedEffect(Unit) {
        postRepository.fetchPosts(
            onSuccess = { fetchedPosts ->
                posts = fetchedPosts
            },
            onFailure = { error ->
                // Обработка ошибки (например, через Toast)
            }
        )
    }

    fun handleEditPost(updatedPost: Post) {
        postRepository.editPost(
            post = updatedPost,
            onSuccess = {
                posts = posts.map { if (it.id == updatedPost.id) updatedPost else it }
            },
            onFailure = { error ->
                // Обработка ошибки
            }
        )
    }

    fun handleDeletePost(post: Post) {
        postRepository.deletePost(
            post = post,
            context = context,
            onSuccess = {
                posts = posts.filter { it.id != post.id }
            },
            onFailure = { error ->
                // Обработка ошибки
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            PostItem(
                post = post,
                onEdit = { updatedPost -> handleEditPost(updatedPost) },
                onDelete = { postToDelete -> handleDeletePost(postToDelete) }
            )
        }
    }
}