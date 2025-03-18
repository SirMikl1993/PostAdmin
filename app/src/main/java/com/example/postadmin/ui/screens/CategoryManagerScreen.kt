package com.example.postadmin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postadmin.data.models.Category
import com.example.postadmin.data.repository.CategoryRepository
import com.example.postadmin.ui.components.CreateCategoryDialog

@Composable
fun CategoryManagerScreen() {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val categoryRepository = CategoryRepository()

    LaunchedEffect(Unit) {
        categoryRepository.fetchCategories(
            onSuccess = { fetchedCategories ->
                categories = fetchedCategories
            },
            onFailure = { error ->
                // Обработка ошибки
            }
        )
    }

    fun createCategory(name: String) {
        categoryRepository.createCategory(
            name = name,
            onSuccess = {
                categoryRepository.fetchCategories(
                    onSuccess = { fetchedCategories ->
                        categories = fetchedCategories
                    },
                    onFailure = { error ->
                        // Обработка ошибки
                    }
                )
            },
            onFailure = { error ->
                // Обработка ошибки
            }
        )
    }

    fun editCategory(category: Category) {
        categoryRepository.editCategory(
            category = category,
            onSuccess = {
                categoryRepository.fetchCategories(
                    onSuccess = { fetchedCategories ->
                        categories = fetchedCategories
                    },
                    onFailure = { error ->
                        // Обработка ошибки
                    }
                )
            },
            onFailure = { error ->
                // Обработка ошибки
            }
        )
    }

    fun deleteCategory(categoryId: String) {
        categoryRepository.deleteCategory(
            categoryId = categoryId,
            onSuccess = {
                categoryRepository.fetchCategories(
                    onSuccess = { fetchedCategories ->
                        categories = fetchedCategories
                    },
                    onFailure = { error ->
                        // Обработка ошибки
                    }
                )
            },
            onFailure = { error ->
                // Обработка ошибки
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Управление категориями",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Создать новую категорию",
                maxLines = 1,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (category == categories.firstOrNull()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { editCategory(category) },
                                modifier = Modifier
                                    .height(36.dp)
                                    .widthIn(min = 80.dp, max = 120.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                                onClick = { deleteCategory(category.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .height(36.dp)
                                    .widthIn(min = 80.dp, max = 120.dp),
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
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateCategoryDialog(
                onDismiss = { showCreateDialog = false },
                onSave = { name ->
                    createCategory(name)
                    showCreateDialog = false
                }
            )
        }
    }
}