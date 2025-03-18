package com.example.postadmin.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.postadmin.data.models.Post
import com.example.postadmin.data.repository.CategoryRepository

@Composable
fun PostItem(post: Post, onEdit: (Post) -> Unit, onDelete: (Post) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }
    val categoryRepository = CategoryRepository()

    LaunchedEffect(post.categoryId) {
        categoryRepository.fetchCategoryName(
            categoryId = post.categoryId,
            onSuccess = { name -> categoryName = name },
            onFailure = { categoryName = "Неизвестно" }
        )
    }

    Column(modifier = Modifier.padding(4.dp)) {
        Text(text = "Заголовок: ${post.title}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Описание: ${post.description}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Содержимое: ${post.content}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Категория: $categoryName", style = MaterialTheme.typography.bodyMedium)

        if (post.imageUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(post.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .height(36.dp)
                    .widthIn(min = 80.dp, max = 120.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Редактировать",
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
            val errorColor = MaterialTheme.colorScheme.error
            Button(
                onClick = { onDelete(post) },
                colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                modifier = Modifier
                    .height(36.dp)
                    .widthIn(min = 80.dp, max = 120.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Удалить",
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
        if (showEditDialog) {
            EditPostDialog(
                post = post,
                onDismiss = { showEditDialog = false },
                onSave = { updatedPost ->
                    onEdit(updatedPost)
                    showEditDialog = false
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}