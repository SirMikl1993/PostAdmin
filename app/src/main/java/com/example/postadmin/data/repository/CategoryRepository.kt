package com.example.postadmin.data.repository

import com.example.postadmin.data.models.Category
import com.google.firebase.firestore.FirebaseFirestore

class CategoryRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchCategories(onSuccess: (List<Category>) -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onFailure("Ошибка загрузки категорий: ${e.message}")
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val categoryList = it.documents.mapNotNull { document ->
                        document.toObject(Category::class.java)?.let { category ->
                            Category(id = document.id, name = category.name)
                        }
                    }
                    onSuccess(categoryList)
                }
            }
    }

    fun fetchCategoryName(categoryId: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        if (categoryId.isNotEmpty()) {
            firestore.collection("categories")
                .document(categoryId)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name") ?: "Неизвестно"
                    onSuccess(name)
                }
                .addOnFailureListener { onFailure() }
        }
    }

    fun createCategory(name: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val newCategory = hashMapOf("name" to name)
        firestore.collection("categories")
            .add(newCategory)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure("Ошибка создания категории: ${e.message}")
            }
    }

    fun editCategory(category: Category, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("categories")
            .document(category.id)
            .update("name", category.name)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure("Ошибка обновления категории: ${e.message}")
            }
    }

    fun deleteCategory(categoryId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore.collection("categories")
            .document(categoryId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure("Ошибка удаления категории: ${e.message}")
            }
    }
}