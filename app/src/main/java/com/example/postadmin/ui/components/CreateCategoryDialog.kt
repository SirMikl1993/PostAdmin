package com.example.postadmin.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                },
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 80.dp, max = 120.dp),
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
        }
    )
}