package com.example.postadmin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.postadmin.data.models.Category
import com.example.postadmin.data.models.Post
import com.example.postadmin.data.repository.CategoryRepository
import com.example.postadmin.data.repository.PostRepository
import com.example.postadmin.ui.components.PostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredPostsScreen() {
    val context = LocalContext.current
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var allPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var titleSearchQuery by remember { mutableStateOf("") }
    var sortCriteria by remember { mutableStateOf<String?>(null) }

    val postRepository = PostRepository()
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

    LaunchedEffect(Unit) {
        postRepository.fetchPosts(
            onSuccess = { fetchedPosts ->
                allPosts = fetchedPosts
                applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                    posts = filteredPosts
                }
            },
            onFailure = { error ->
                // Обработка ошибки
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Фильтрация и сортировка",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .align(Alignment.CenterHorizontally)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
                applyFiltersAndSort(allPosts, newQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                    posts = filteredPosts
                }
            },
            label = { Text("Поиск (заголовок/описание)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = titleSearchQuery,
                onValueChange = { newQuery -> titleSearchQuery = newQuery },
                label = { Text("Поиск по заголовку") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search by title") },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                        posts = filteredPosts
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .height(56.dp)
                    .widthIn(min = 80.dp, max = 120.dp)
                    .align(Alignment.CenterVertically),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Найти",
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        var expandedCategory by remember { mutableStateOf(false) }
        var selectedCategoryName by remember { mutableStateOf("Все категории") }

        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedCategoryName,
                onValueChange = { },
                readOnly = true,
                label = { Text("Категория") },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = "Category") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select category"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .menuAnchor()
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Все категории", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        selectedCategoryName = "Все категории"
                        selectedCategoryId = null
                        expandedCategory = false
                        applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, null, sortCriteria) { filteredPosts ->
                            posts = filteredPosts
                        }
                    }
                )
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            selectedCategoryName = category.name
                            selectedCategoryId = category.id
                            expandedCategory = false
                            applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, category.id, sortCriteria) { filteredPosts ->
                                posts = filteredPosts
                            }
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    sortCriteria = "date_asc"
                    applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                        posts = filteredPosts
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Дата (возр.)",
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
            Button(
                onClick = {
                    sortCriteria = "date_desc"
                    applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                        posts = filteredPosts
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Дата (убыв.)",
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
            Button(
                onClick = {
                    sortCriteria = "title_asc"
                    applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                        posts = filteredPosts
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "A-Z",
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
            Button(
                onClick = {
                    sortCriteria = "title_desc"
                    applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                        posts = filteredPosts
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Z-A",
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                PostCard(post = post, onEdit = { updatedPost ->
                    postRepository.editPost(
                        post = updatedPost,
                        onSuccess = {
                            allPosts = allPosts.map { if (it.id == updatedPost.id) updatedPost else it }
                            applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                                posts = filteredPosts
                            }
                        },
                        onFailure = { error ->
                            // Обработка ошибки
                        }
                    )
                }, onDelete = { postToDelete ->
                    postRepository.deletePost(
                        post = postToDelete,
                        context = context,
                        onSuccess = {
                            allPosts = allPosts.filter { it.id != postToDelete.id }
                            applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                                posts = filteredPosts
                            }
                        },
                        onFailure = { error ->
                            // Обработка ошибки
                        }
                    )
                })
            }
        }
    }
}

fun applyFiltersAndSort(
    postsToFilter: List<Post>,
    query: String,
    titleQuery: String,
    categoryId: String?,
    sortCriteria: String?,
    onResult: (List<Post>) -> Unit
) {
    var filteredPosts = postsToFilter.toList()

    if (categoryId != null) {
        filteredPosts = filteredPosts.filter { it.categoryId == categoryId }
    }

    if (query.isNotEmpty()) {
        filteredPosts = filteredPosts.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
    }

    if (titleQuery.isNotEmpty()) {
        filteredPosts = filteredPosts.filter {
            it.title.contains(titleQuery, ignoreCase = true)
        }
    }

    filteredPosts = when (sortCriteria) {
        "date_asc" -> filteredPosts.sortedBy { it.timestamp }
        "date_desc" -> filteredPosts.sortedByDescending { it.timestamp }
        "title_asc" -> filteredPosts.sortedBy { it.title }
        "title_desc" -> filteredPosts.sortedByDescending { it.title }
        else -> filteredPosts
    }

    onResult(filteredPosts)
}