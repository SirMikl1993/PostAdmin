package com.example.postadmin.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postadmin.data.models.Category
import com.example.postadmin.data.models.Post
import com.example.postadmin.data.repository.CategoryRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostDialog(post: Post, onDismiss: () -> Unit, onSave: (Post) -> Unit) {
    var title by remember { mutableStateOf(TextFieldValue(post.title)) }
    var description by remember { mutableStateOf(TextFieldValue(post.description)) }
    var content by remember { mutableStateOf(TextFieldValue(post.content)) }
    var categoryId by remember { mutableStateOf(post.categoryId) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoadingCategories by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categoryRepository = CategoryRepository()

    LaunchedEffect(Unit) {
        categoryRepository.fetchCategories(
            onSuccess = { fetchedCategories ->
                categories = fetchedCategories
                isLoadingCategories = false
            },
            onFailure = { error ->
                errorMessage = error
                isLoadingCategories = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val updatedPost = Post(
                        id = post.id,
                        title = title.text,
                        description = description.text,
                        content = content.text,
                        imageUrl = post.imageUrl,
                        categoryId = categoryId
                    )
                    onSave(updatedPost)
                },
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 80.dp, max = 120.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Сохранить",
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 80.dp, max = 120.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Отмена",
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Содержимое") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (isLoadingCategories) {
                    Text(
                        text = "Загрузка категорий...",
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    var selectedCategory by remember { mutableStateOf(categories.find { it.id == categoryId }?.name ?: "Выберите категорию") }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Категория") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Выбрать категорию"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                if (categories.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Нет категорий") },
                                        onClick = { expanded = false }
                                    )
                                } else {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.name) },
                                            onClick = {
                                                selectedCategory = category.name
                                                categoryId = category.id
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}