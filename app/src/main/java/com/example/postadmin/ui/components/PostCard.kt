package com.example.postadmin.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.postadmin.data.models.Post
import com.example.postadmin.data.repository.CategoryRepository

@Composable
fun PostCard(post: Post, onEdit: (Post) -> Unit, onDelete: (Post) -> Unit) {
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Заголовок: ${post.title}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Описание: ${post.description}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Содержимое: ${post.content}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Категория: $categoryName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (post.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(post.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showEditDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .height(40.dp)
                        .weight(1f)
                        .padding(end = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Редактировать",
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
                Button(
                    onClick = { onDelete(post) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .height(40.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Удалить",
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onError,
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
        }
    }
}