package com.example.postadmin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.postadmin.ui.components.InstructionCard

@Composable
fun AboutScreen() {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
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