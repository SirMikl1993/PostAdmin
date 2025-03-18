package com.example.postadmin.data.repository

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.postadmin.data.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PostRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    fun fetchPosts(onSuccess: (List<Post>) -> Unit, onFailure: (String) -> Unit) {
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
                onSuccess(fetchedPosts)
            }
            .addOnFailureListener { e ->
                onFailure("Ошибка получения постов: ${e.message}")
            }
    }

    fun editPost(post: Post, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
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

    fun deletePost(post: Post, context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("posts")
            .document(post.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Пост удален успешно", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure("Ошибка удаления поста: ${e.message}")
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
        val imageRef = storageRef.child("images/${UUID.randomUUID()}")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val post = hashMapOf(
                        "title" to title,
                        "description" to description,
                        "content" to content,
                        "imageUrl" to uri.toString(),
                        "categoryId" to categoryId,
                        "timestamp" to System.currentTimeMillis()
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
}