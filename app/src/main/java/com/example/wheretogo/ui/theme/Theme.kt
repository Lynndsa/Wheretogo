package com.example.wheretogo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

)

// Заголовок экрана
val TextHeadStyle = TextStyle(
    textAlign = TextAlign.Center,
    fontSize = 30.sp,
    color = ButtonColor,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Bold
)

// Лейблы полей ввода
val TextLabelStyle = TextStyle(
    fontSize = 17.sp,
    color = ButtonColor,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Bold
)

// Текст внутри главной кнопки
val ButtonTextStyle = TextStyle(
    color = BackgroundColor,
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold
)

// Текст ссылок/подсказок снизу
val LinkTextStyle = TextStyle(
    textAlign = TextAlign.Center,
    fontSize = 15.sp,
    color = ButtonColor,
    textDecoration = TextDecoration.Underline
)
val InputFieldShape = RoundedCornerShape(50.dp)

object AppDimens {
    val ScreenPaddingTop: Dp = 50.dp
    val ScreenPaddingHorizontal: Dp = 5.dp

    val TitleBottomSpacer: Dp = 90.dp
    val FieldSpacing: Dp = 16.dp
    val ButtonTopSpacer: Dp = 120.dp
    val LinkTopSpacer: Dp = 10.dp

    val ButtonHeight: Dp = 60.dp
    val ButtonHorizontalPadding: Dp = 30.dp
}
// Стилизация полей ввода
@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ButtonColor,
    unfocusedBorderColor = ButtonColor,
    cursorColor = ButtonColor,
    focusedTextColor = ButtonColor
)

// Стилизация основной кнопки
@Composable
fun primaryButtonColors() = ButtonDefaults.buttonColors(
    containerColor = ButtonColor,
    contentColor = BackgroundColor
)



@Composable
fun WhereToGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}