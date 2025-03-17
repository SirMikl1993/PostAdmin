package com.example.postadmin

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


data class BarItem(
    val title: String,
    val image: ImageVector,
    val route: String
)

data class Post(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val categoryId: String = "",
    val timestamp: Long = 0L // Время создания поста (в миллисекундах)
)

data class Category(
    val id: String = "",
    val name: String = ""
)

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
            title = "Фильтрация и сортировка", // Новая вкладка
            image = Icons.Filled.Info, // Можно заменить на другой подходящий значок
            route = "filteredPosts"
        )
    )
}

sealed class NavRoutes(val route: String) {
    object PostForm : NavRoutes("postForm")
    object Posts : NavRoutes("posts")
    object About : NavRoutes("about")
    object CategoryManager : NavRoutes("categoryManager")
    object FilteredPosts : NavRoutes("filteredPosts") // Новый маршрут
}

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                LoginScreen(onLoginSuccess = {
                    MainScreen()
                })
            } else {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Main()
}

@Preview(showBackground = true)
@Composable
fun CategoryListScreen() {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    fun fetchCategoriesWithListener() {
        firestore.collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val categoryList = it.documents.mapNotNull { document ->
                        document.toObject(Category::class.java)?.let { category ->
                            Category(id = document.id, name = category.name)
                        }
                    }
                    categories = categoryList
                }
            }
    }

    LaunchedEffect(Unit) {
        fetchCategoriesWithListener()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Список категорий",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }
            }
            if (categories.isEmpty()) {
                item {
                    Text(
                        text = "Категории не найдены",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: @Composable () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    var isLoggedIn by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        onLoginSuccess()
    } else {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 30.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Логин") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                isLoggedIn = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Ошибка входа: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Войти")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Main() {
    val navController = rememberNavController()
    Column(Modifier.padding(8.dp)) {
        NavHost(
            navController,
            startDestination = NavRoutes.PostForm.route,
            modifier = Modifier.weight(1f)
        ) {
            composable(NavRoutes.PostForm.route) { PostForm() }
            composable(NavRoutes.Posts.route) { Posts() }
            composable(NavRoutes.About.route) { About() }
            composable(NavRoutes.CategoryManager.route) { CategoryManager() }
            composable(NavRoutes.FilteredPosts.route) { FilteredPostsScreen() } // Новый экран
        }
        BottomNavigationBar(navController = navController)
    }
}

@Preview
@Composable
fun Posts() {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { result ->
                val fetchedPosts = result.documents.mapNotNull { document ->
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        Post(
                            id = document.id,
                            title = post.title,
                            description = post.description,
                            content = post.content,
                            imageUrl = post.imageUrl,
                            categoryId = post.categoryId,
                            timestamp = post.timestamp
                        )
                    } else {
                        null
                    }
                }
                posts = fetchedPosts
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка получения поста: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun handleEditPost(updatedPost: Post) {
        editPost(
            post = updatedPost,
            onSuccess = {
                posts = posts.map { if (it.id == updatedPost.id) updatedPost else it }
                Toast.makeText(context, "Пост обновлен успешно", Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun handleDeletePost(post: Post) {
        deletePost(post, context)
        posts = posts.filter { it.id != post.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
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
    val firestore = FirebaseFirestore.getInstance()

    // Загрузка категорий
    LaunchedEffect(Unit) {
        firestore.collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Ошибка загрузки категорий: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val categoryList = it.documents.mapNotNull { document ->
                        document.toObject(Category::class.java)?.let { category ->
                            Category(id = document.id, name = category.name)
                        }
                    }
                    categories = categoryList
                }
            }
    }

    // Загрузка всех постов
    LaunchedEffect(Unit) {
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { result ->
                val fetchedPosts = result.documents.mapNotNull { document ->
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        Post(
                            id = document.id,
                            title = post.title,
                            description = post.description,
                            content = post.content,
                            imageUrl = post.imageUrl,
                            categoryId = post.categoryId,
                            timestamp = post.timestamp
                        )
                    } else {
                        null
                    }
                }
                allPosts = fetchedPosts
                applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                    posts = filteredPosts
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка получения постов: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    .width(80.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text("Найти", color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        // Выбор категории
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
                    .menuAnchor() // Этот модификатор должен быть доступен после обновления
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
                    .padding(end = 4.dp)
            ) {
                Text("Дата (возр.)", color = MaterialTheme.colorScheme.onPrimary)
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
                    .padding(end = 4.dp)
            ) {
                Text("Дата (убыв.)", color = MaterialTheme.colorScheme.onPrimary)
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
                    .padding(end = 4.dp)
            ) {
                Text("A-Z", color = MaterialTheme.colorScheme.onPrimary)
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
                    .height(48.dp)
            ) {
                Text("Z-A", color = MaterialTheme.colorScheme.onPrimary)
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
                PostCard(
                    post = post,
                    onEdit = { updatedPost ->
                        editPost(
                            post = updatedPost,
                            onSuccess = {
                                allPosts = allPosts.map { if (it.id == updatedPost.id) updatedPost else it }
                                applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                                    posts = filteredPosts
                                }
                                Toast.makeText(context, "Пост обновлен успешно", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onDelete = { postToDelete ->
                        deletePost(postToDelete, context)
                        allPosts = allPosts.filter { it.id != postToDelete.id }
                        applyFiltersAndSort(allPosts, searchQuery, titleSearchQuery, selectedCategoryId, sortCriteria) { filteredPosts ->
                            posts = filteredPosts
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PostCard(post: Post, onEdit: (Post) -> Unit, onDelete: (Post) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()
    var categoryName by remember { mutableStateOf("") }

    LaunchedEffect(post.categoryId) {
        if (post.categoryId.isNotEmpty()) {
            firestore.collection("categories")
                .document(post.categoryId)
                .get()
                .addOnSuccessListener { document ->
                    categoryName = document.getString("name") ?: "Неизвестно"
                }
                .addOnFailureListener { }
        }
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
                        .padding(end = 4.dp)
                ) {
                    Text("Редактировать", color = MaterialTheme.colorScheme.onPrimary)
                }
                Button(
                    onClick = { onDelete(post) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .height(40.dp)
                        .weight(1f)
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.onError)
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

fun applyFiltersAndSort(
    postsToFilter: List<Post>,
    query: String,
    titleQuery: String,
    categoryId: String?,
    sortCriteria: String?,
    onResult: (List<Post>) -> Unit
) {
    var filteredPosts = postsToFilter.toList()

    // Фильтрация по категории
    if (categoryId != null) {
        filteredPosts = filteredPosts.filter { it.categoryId == categoryId }
    }

    // Фильтрация по общему поиску (заголовок и описание)
    if (query.isNotEmpty()) {
        filteredPosts = filteredPosts.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
    }

    // Фильтрация по заголовку
    if (titleQuery.isNotEmpty()) {
        filteredPosts = filteredPosts.filter {
            it.title.contains(titleQuery, ignoreCase = true)
        }
    }

    // Сортировка
    filteredPosts = when (sortCriteria) {
        "date_asc" -> filteredPosts.sortedBy { it.timestamp }
        "date_desc" -> filteredPosts.sortedByDescending { it.timestamp }
        "title_asc" -> filteredPosts.sortedBy { it.title }
        "title_desc" -> filteredPosts.sortedByDescending { it.title }
        else -> filteredPosts
    }

    onResult(filteredPosts)
}


fun editPost(post: Post, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    val postUpdates = hashMapOf<String, Any>(
        "title" to post.title,
        "description" to post.description,
        "content" to post.content,
        "imageUrl" to post.imageUrl,
        "categoryId" to post.categoryId,
        "timestamp" to post.timestamp
    )
    firestore.collection("posts")
        .document(post.id)
        .update(postUpdates)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onFailure("Ошибка обновления поста: ${e.message}")
        }
}

fun deletePost(post: Post, context: Context) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("posts")
        .document(post.id)
        .delete()
        .addOnSuccessListener {
            Toast.makeText(context, "Пост удален успешно", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Ошибка удаления поста: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

@Composable
fun PostItem(post: Post, onEdit: (Post) -> Unit, onDelete: (Post) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()
    var categoryName by remember { mutableStateOf("") }

    LaunchedEffect(post.categoryId) {
        if (post.categoryId.isNotEmpty()) {
            firestore.collection("categories")
                .document(post.categoryId)
                .get()
                .addOnSuccessListener { document ->
                    categoryName = document.getString("name") ?: "Неизвестно"
                }
                .addOnFailureListener { }
        }
    }

    Column(modifier = Modifier.padding(4.dp)) { // Уменьшаем отступы
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
                    .height(150.dp), // Уменьшаем высоту изображения
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp)) // Уменьшаем Spacer
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.height(36.dp) // Уменьшаем высоту кнопки
            ) {
                Text(text = "Редактировать")
            }
            val errorColor = MaterialTheme.colorScheme.error
            Button(
                onClick = { onDelete(post) },
                colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                modifier = Modifier.height(36.dp) // Уменьшаем высоту кнопки
            ) {
                Text(text = "Удалить", color = androidx.compose.ui.graphics.Color.White)
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
        Spacer(modifier = Modifier.height(8.dp)) // Уменьшаем Spacer
    }
}

@Composable
fun InstructionCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InstructionScreen() {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            InstructionCard(
                title = "1. Запуск и авторизация",
                content = "Запустите приложение на устройстве. Введите email и пароль для авторизации через Firebase. После успешного входа вы попадете в главное меню."
            )
        }
        item {
            InstructionCard(
                title = "2. Главное меню и навигация",
                content = "Главное меню содержит разделы: 'Добавление поста', 'Список постов', 'Фильтрация и сортировка', 'О приложении', 'Управление категориями' и 'Список категорий'. Переключайтесь между ними с помощью нижней навигационной панели."
            )
        }
        item {
            InstructionCard(
                title = "3. Добавление поста",
                content = "Перейдите в 'Добавление поста'. Введите заголовок, описание и содержимое поста. Выберите категорию из списка или создайте новую. Загрузите изображение и нажмите 'Добавить запись'."
            )
        }
        item {
            InstructionCard(
                title = "4. Просмотр постов",
                content = "В разделе 'Список постов' отображаются все записи в виде карточек. Вы можете прокручивать список, чтобы просмотреть заголовки, описания, содержимое и изображения постов."
            )
        }
        item {
            InstructionCard(
                title = "5. Редактирование поста",
                content = "В 'Списке постов' или 'Фильтрации и сортировке' найдите нужный пост и нажмите 'Редактировать'. Обновите заголовок, описание, содержимое или категорию, затем нажмите 'Сохранить'."
            )
        }
        item {
            InstructionCard(
                title = "6. Удаление поста",
                content = "Выберите пост в 'Списке постов' или 'Фильтрации и сортировке' и нажмите 'Удалить'. Подтвердите удаление во всплывающем диалоговом окне. Пост будет удален из базы данных."
            )
        }
        item {
            InstructionCard(
                title = "7. О приложении",
                content = "В разделе 'О приложении' вы найдете инструкции по использованию приложения, информацию о версии и авторе. Используйте этот раздел для ознакомления с функционалом."
            )
        }
        item {
            InstructionCard(
                title = "8. Фильтрация и сортировка",
                content = "Перейдите в 'Фильтрация и сортировка'. Выполните поиск по заголовку или описанию, выберите категорию или используйте кнопки сортировки: по дате (возрастание/убывание) или по названию (A-Z/Z-A)."
            )
        }
        item {
            InstructionCard(
                title = "9. Управление категориями",
                content = "В разделе 'Управление категориями' создавайте новые категории, редактируйте существующие или удаляйте ненужные. Используйте кнопки 'Создать', 'Редактировать' и 'Удалить' для управления."
            )
        }
        item {
            InstructionCard(
                title = "10. Список категорий",
                content = "Раздел 'Список категорий' отображает все категории в виде списка. Прокручивайте, чтобы увидеть доступные категории, созданные в приложении."
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun About() {
    InstructionScreen()
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        NavBarItems.BarItems.forEach { navItem ->
            NavigationBarItem(
                selected = currentRoute == navItem.route,
                onClick = {
                    navController.navigate(navItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = navItem.image,
                        contentDescription = navItem.title
                    )
                },
                label = {
                    Text(text = navItem.title, textAlign = TextAlign.Center)
                }
            )
        }
    }
}

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

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    // Используем addSnapshotListener для динамического обновления
    LaunchedEffect(Unit) {
        firestore.collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorMessage = "Ошибка загрузки категорий: ${e.message}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    isLoadingCategories = false
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val categoryList = it.documents.mapNotNull { document ->
                        document.toObject(Category::class.java)?.let { category ->
                            Category(id = document.id, name = category.name)
                        }
                    }
                    categories = categoryList
                    isLoadingCategories = false
                    println("Loaded categories: ${categoryList.map { it.name }}") // Для отладки
                }
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val updatedPost = Post(
                    id = post.id,
                    title = title.text,
                    description = description.text,
                    content = content.text,
                    imageUrl = post.imageUrl,
                    categoryId = categoryId
                )
                onSave(updatedPost)
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PostForm() {
    var title by remember { mutableStateOf(TextFieldValue()) }
    var description by remember { mutableStateOf(TextFieldValue()) }
    var content by remember { mutableStateOf(TextFieldValue()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var categoryId by remember { mutableStateOf("") }
    var newCategoryName by remember { mutableStateOf("") }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingCategories by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    LaunchedEffect(Unit) {
        firestore.collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorMessage = "Ошибка загрузки категорий: ${e.message}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    isLoadingCategories = false
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val categoryList = it.documents.mapNotNull { document ->
                        document.toObject(Category::class.java)?.let { category ->
                            Category(id = document.id, name = category.name)
                        }
                    }
                    categories = categoryList
                    isLoadingCategories = false
                    println("Loaded categories in PostForm: ${categoryList.map { it.name }}") // Для отладки
                }
            }
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выберите изображение")
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
                    uploadPost(
                        title.text, description.text, content.text, imageUri!!, categoryId,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Запись добавлена успешно!", Toast.LENGTH_SHORT).show()
                            title = TextFieldValue()
                            description = TextFieldValue()
                            content = TextFieldValue()
                            imageUri = null
                            categoryId = ""
                        },
                        onFailure = { error ->
                            isLoading = false
                            Toast.makeText(context, "Ошибка: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Пожалуйста, заполните все поля, включая категорию", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Загрузка..." else "Добавить запись")
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
                            val newCategory = hashMapOf("name" to newCategoryName)
                            firestore.collection("categories")
                                .add(newCategory)
                                .addOnSuccessListener { documentReference ->
                                    categoryId = documentReference.id
                                    categories = categories + Category(id = documentReference.id, name = newCategoryName)
                                    showCreateCategoryDialog = false
                                    newCategoryName = ""
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Ошибка создания категории: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                Button(onClick = { showCreateCategoryDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

fun uploadPost(
    title: String,
    description: String,
    content: String,
    imageUri: Uri,
    categoryId: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val storageRef = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
    val firestore = FirebaseFirestore.getInstance()

    storageRef.putFile(imageUri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val post = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "content" to content,
                    "imageUrl" to uri.toString(),
                    "categoryId" to categoryId,
                    "timestamp" to System.currentTimeMillis() // Добавляем текущую временную метку
                )
                firestore.collection("posts")
                    .add(post)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure("Ошибка загрузки поста: ${e.message}")
                    }
            }
        }
        .addOnFailureListener { e ->
            onFailure("Ошибка загрузки изображения: ${e.message}")
        }
}

@Composable
fun CreateCategoryDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var categoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать категорию") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Название категории") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotEmpty()) {
                        onSave(categoryName)
                    }
                }
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun EditCategoryDialog(category: Category, onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    var categoryName by remember { mutableStateOf(category.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать категорию") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Название категории") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotEmpty()) {
                        onSave(Category(id = category.id, name = categoryName))
                    }
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryManager() {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    fun fetchCategories() {
        firestore.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                val categoryList = result.documents.mapNotNull { document ->
                    document.toObject(Category::class.java)?.let { category ->
                        Category(id = document.id, name = category.name)
                    }
                }
                categories = categoryList
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка загрузки категорий: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    LaunchedEffect(Unit) {
        fetchCategories()
    }

    fun createCategory(name: String) {
        val newCategory = hashMapOf("name" to name)
        firestore.collection("categories")
            .add(newCategory)
            .addOnSuccessListener {
                fetchCategories()
                Toast.makeText(context, "Категория создана", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка создания категории: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun editCategory(category: Category) {
        firestore.collection("categories")
            .document(category.id)
            .update("name", category.name)
            .addOnSuccessListener {
                fetchCategories()
                Toast.makeText(context, "Категория обновлена", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка обновления категории: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteCategory(categoryId: String) {
        firestore.collection("categories")
            .document(categoryId)
            .delete()
            .addOnSuccessListener {
                fetchCategories()
                Toast.makeText(context, "Категория удалена", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка удаления категории: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Управление категориями",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать новую категорию")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Редактировать", color = MaterialTheme.colorScheme.onPrimary)
                            }
                            Button(
                                onClick = { deleteCategory(category.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Удалить", color = MaterialTheme.colorScheme.onError)
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