package com.example.postadmin.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.postadmin.data.models.Category
import com.example.postadmin.data.repository.CategoryRepository
import com.example.postadmin.data.repository.PostRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFormScreen() {
    var title by remember { mutableStateOf(TextFieldValue()) }
    var description by remember { mutableStateOf(TextFieldValue()) }
    var content by remember { mutableStateOf(TextFieldValue()) }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var categoryId by remember { mutableStateOf("") }
    var newCategoryName by remember { mutableStateOf("") }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingCategories by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val postRepository = PostRepository()
    val categoryRepository = CategoryRepository()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

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

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
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

        Spacer(modifier = Modifier.height(16.dp))

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
                            DropdownMenuItem(
                                text = { Text("Создать новую категорию") },
                                onClick = {
                                    expanded = false
                                    showCreateCategoryDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Выберите изображение",
                maxLines = 1,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.text.isNotEmpty() && description.text.isNotEmpty() && content.text.isNotEmpty() && imageUri != null && categoryId.isNotEmpty()) {
                    isLoading = true
                    postRepository.uploadPost(
                        title.text, description.text, content.text, imageUri!!, categoryId,
                        onSuccess = {
                            isLoading = false
                            title = TextFieldValue()
                            description = TextFieldValue()
                            content = TextFieldValue()
                            imageUri = null
                            categoryId = ""
                        },
                        onFailure = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                } else {
                    errorMessage = "Пожалуйста, заполните все поля, включая категорию"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isLoading) "Загрузка..." else "Добавить запись",
                maxLines = 1,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }

    if (showCreateCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCreateCategoryDialog = false },
            title = { Text("Создать новую категорию") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Название категории") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotEmpty()) {
                            categoryRepository.createCategory(
                                newCategoryName,
                                onSuccess = {
                                    categoryId = categories.lastOrNull()?.id ?: categoryId
                                    showCreateCategoryDialog = false
                                    newCategoryName = ""
                                },
                                onFailure = { error ->
                                    errorMessage = error
                                }
                            )
                        }
                    },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Создать",
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCreateCategoryDialog = false },
                    modifier = Modifier.height(48.dp),
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
            }
        )
    }
}