package com.example.wheretogo.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wheretogo.ui.theme.ButtonColor
import com.example.wheretogo.ui.theme.MenuItemColor
import com.example.wheretogo.ui.theme.MenuPanelColor

data class MenuItemData(val title: String, val route: String)

private val menuItems = listOf(
    MenuItemData("Профиль", Destination.Profile.route),
    MenuItemData("Достопримечательности", Destination.Places.route),
    MenuItemData("Хочу сходить", Destination.Wishlist.route),
    MenuItemData("Карта", Destination.Map.route),
    MenuItemData("Мои маршруты", Destination.MyRoutes.route),
    MenuItemData("Готовые маршруты", Destination.ReadyRoutes.route),
    MenuItemData("Настройки", Destination.Settings.route),
)

private val addRouteItem = MenuItemData("Добавить маршрут", Destination.AddRoute.route)

@Composable
fun SideMenu(
    visible: Boolean,
    onDismiss: () -> Unit,
    onItemClick: (route: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Системная кнопка «Назад» закрывает меню
    BackHandler(enabled = visible, onBack = onDismiss)

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ButtonColor.copy(alpha = 0.25f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
            )
        }

        // Сама панель меню
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it }),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.48f)
                    .background(MenuPanelColor)
                    .statusBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(start = 4.dp,  bottom = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Закрыть меню",
                        tint = ButtonColor,
                        modifier = Modifier.size(35.dp)
                    )
                }

                menuItems.forEach { item ->
                    SideMenuButton(item.title) { onItemClick(item.route) }
                }

                Spacer(Modifier.height(8.dp))

                SideMenuButton(addRouteItem.title) { onItemClick(addRouteItem.route) }
            }
        }
    }
}

@Composable
private fun SideMenuButton(
    title: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = MenuItemColor,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                color = ButtonColor,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}