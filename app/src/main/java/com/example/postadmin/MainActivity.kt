package com.example.postadmin

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import coil.compose.rememberImagePainter
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
    val id: String = "", // Добавляем id
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val imageUrl: String = ""
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
        )
    )
}

sealed class NavRoutes(val route: String) {
    object postForm : NavRoutes("postForm")
    object Posts : NavRoutes("posts")
    object About : NavRoutes("about")
}

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this) // Initialize Firebase
        setContent {
            auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                LoginScreen(onLoginSuccess = {
                    // Когда логин успешен, вызываем этот блок кода
                    MainScreen() // Отображаем основной экран после успешной авторизации
                })
            } else {
                setContent {
                    // Вызов функции Composable
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Main() // Вызов вашей основной функции, отображающей экран
}

@Composable
fun LoginScreen(onLoginSuccess: @Composable () -> Unit) {
    // Локальные состояния для ввода пользователя
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Локальное состояние для успешного входа
    var isLoggedIn by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        // Если пользователь успешно вошел, отображаем onLoginSuccess
        onLoginSuccess()
    } else {
        // Показываем экран логина
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 30.dp)
                .fillMaxSize()
        ) {
            // Поля для ввода email и password
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
                    // Здесь выполняем проверку логина
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Если логин успешен, обновляем состояние isLoggedIn
                                isLoggedIn = true
                            } else {
                                // Показываем сообщение об ошибке
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
            startDestination = NavRoutes.postForm.route,
            modifier = Modifier.weight(1f)
        ) {
            composable(NavRoutes.postForm.route) { PostForm() }
            composable(NavRoutes.Posts.route) { Posts() }
            composable(NavRoutes.About.route) { About() }
        }
        BottomNavigationBar(navController = navController)
    }
}

@Preview
@Composable
fun Posts() {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val context = LocalContext.current

    // Функция редактирования поста
    fun editPost(post: Post, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        // Обновление данных поста в Firestore
        val postUpdates = hashMapOf<String, Any>(
            "title" to post.title,
            "description" to post.description,
            "content" to post.content,
            "imageUrl" to post.imageUrl
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

    // Функция удаления поста
    fun deletePost(post: Post) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("posts")
            .document(post.id)
            .delete()
            .addOnSuccessListener {
                posts = posts.filter { it.id != post.id } // Обновление списка постов
                Toast.makeText(context, "Пост удален успешно", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка удаления поста: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    // Загрузить посты из базы данных
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("posts")
            .get()
            .addOnSuccessListener { result ->
                val fetchedPosts = result.documents.mapNotNull { document ->
                    document.toObject(Post::class.java)?.copy(id = document.id)
                }
                posts = fetchedPosts
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка получения поста: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    // Функция для обновления поста
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

    // Функция для удаления поста
    fun handleDeletePost(post: Post) {
        deletePost(post)
    }

    // Отображение списка всех постов из БД
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        items(posts.size) { index ->
            val post = posts[index]
            PostItem(
                post = post,
                onEdit = { updatedPost -> handleEditPost(updatedPost) },
                onDelete = {  postToDelete -> handleDeletePost(postToDelete) }
            )
        }
    }
}


@Composable
fun PostItem(post: Post, onEdit: (Post) -> Unit, onDelete: (Post) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "Заголовок: ${post.title}")
        Text(text = "Описание: ${post.description}")
        Text(text = "Содержимое: ${post.content}")

        // Отобразить изображение, если URL доступен
        if (post.imageUrl.isNotEmpty()) {
            Image(
                painter = rememberImagePainter(data = Uri.parse(post.imageUrl)),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Кнопки редактирования и удаления
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { showEditDialog = true }) {
                Text(text = "Редактировать")
            }
            val errorColor = MaterialTheme.colorScheme.error
            Button(
                onClick = { onDelete(post) },
                colors = ButtonDefaults.buttonColors(containerColor = errorColor)
            ) {
                Text(text = "Удалить", color = androidx.compose.ui.graphics.Color.White)
            }
        }
        // Показываем диалог редактирования, если `showEditDialog` == true
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
        Spacer(modifier = Modifier.height(16.dp))
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
    // Главная колонка с отступом
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Элементы LazyColumn, которые отображают карточки инструкций
        item {
            InstructionCard(
                title = "1. Запуск и авторизация",
                content = "Запустите приложение на вашем устройстве. Убедитесь, что у вас есть доступ к Firebase и вы авторизованы."
            )
        }

        item {
            InstructionCard(
                title = "2. Главное меню и навигация",
                content = "Главное меню содержит три раздела: Добавление поста, Список постов и О приложении. Навигация осуществляется через нижнюю панель."
            )
        }

        item {
            InstructionCard(
                title = "3. Добавление поста",
                content = "Перейдите в раздел Добавление поста. Заполните поля: Заголовок, Описание и Содержимое. Выберите изображение и нажмите 'Добавить запись'."
            )
        }

        item {
            InstructionCard(
                title = "4. Просмотр постов",
                content = "Перейдите в раздел Список постов, чтобы увидеть все добавленные записи. Доступны кнопки Редактировать и Удалить."
            )
        }

        item {
            InstructionCard(
                title = "5. Редактирование поста",
                content = "В разделе Список постов нажмите кнопку Редактировать. Внесите изменения и нажмите Сохранить."
            )
        }

        item {
            InstructionCard(
                title = "6. Удаление поста",
                content = "В разделе Список постов выберите пост и нажмите Удалить. Подтвердите удаление в диалоговом окне."
            )
        }

        item {
            InstructionCard(
                title = "7. О приложении",
                content = "Перейдите в раздел О приложении, чтобы просмотреть информацию об авторе и версии приложения."
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

@Composable
fun EditPostDialog(post: Post, onDismiss: () -> Unit, onSave: (Post) -> Unit) {
    var title by remember { mutableStateOf(TextFieldValue(post.title)) }
    var description by remember { mutableStateOf(TextFieldValue(post.description)) }
    var content by remember { mutableStateOf(TextFieldValue(post.content)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val updatedPost = post.copy(
                    title = title.text,
                    description = description.text,
                    content = content.text
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
            }
        }
    )
}

@Preview
@Composable
fun PostForm() {
    var title by remember { mutableStateOf(TextFieldValue()) }
    var description by remember { mutableStateOf(TextFieldValue()) }
    var content by remember { mutableStateOf(TextFieldValue()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    // Получаем контекст для Toast
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Поле для ввода названия
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок") },
            modifier = Modifier.fillMaxWidth()
        )

        // Поле для ввода описания
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )

        // Поле для ввода содержания
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Содержимое") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка для выбора изображения
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выберите изображение")
        }

        // Отображение выбранного изображения
        imageUri?.let {
            Image(
                painter = rememberImagePainter(data = it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка для загрузки поста
        Button(
            onClick = {
                if (title.text.isNotEmpty() && description.text.isNotEmpty() && content.text.isNotEmpty() && imageUri != null) {
                    isLoading = true
                    uploadPost(
                        title.text, description.text, content.text, imageUri!!, context,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(
                                context,
                                "Запись добавлена успешно !",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            isLoading = false
                            Toast.makeText(context, "Ошибка: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Пожалуйста заполните поля", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Загрузка..." else "Добавить запись")
        }
    }
}

fun uploadPost(
    title: String,
    description: String,
    content: String,
    imageUri: Uri,
    context: android.content.Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val storageRef = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
    val firestore = FirebaseFirestore.getInstance()

    // Загрузка изображения в Firebase Storage
    storageRef.putFile(imageUri)
        .addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Добавление данных поста в Firestore
                val post = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "content" to content,
                    "imageUrl" to uri.toString()
                )
                firestore.collection("posts")
                    .add(post)
                    .addOnSuccessListener {
                        onSuccess()  // Успешное добавление поста
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