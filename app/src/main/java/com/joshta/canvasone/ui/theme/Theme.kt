package com.joshta.canvasone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppTypo(
    val fontFamily: FontFamily = FontFamily.SansSerif,
    val h1: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        color = Primary
    ),
    val h2: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = Primary
    ),
    val h3: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = Primary
    ),
    val h4: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Primary
    ),
    val h5: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Primary
    ),
    val h6: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Primary
    ),
    val subtitle1: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = Primary
    ),
    val subtitle2: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Primary
    ),
    val body1: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = Primary
    ),
    val body2: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Primary
    ),
    val button: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Primary
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = Primary
    ),
    val overline: TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = Primary
    )
)

data class AppColor(
    val primary: Color = Color(0xFF4D869C),
    val primaryVariant: Color = Color(0xFF316C6C),
    val secondary: Color = Color(0xFFA8DADC),
    val secondaryVariant: Color = Color(0xFF7CA8A4),
    val background: Color = Color(0xFFEAECEE),
    val darkBackground: Color = Color(0xFF121212), // Dark background color
    val surface: Color = Color(0xFFFFFFFF),
    val error: Color = Color(0xFFB00020),
    val onPrimary: Color = Color(0xFFFFFFFF),
    val onSecondary: Color = Color(0xFF000000),
    val onBackground: Color = Color(0xFF000000),
    val onSurface: Color = Color(0xFF000000),
    val onError: Color = Color(0xFFFFFFFF)
)

data class AppShape(
    val small: Shape = RoundedCornerShape(4.dp),
    val medium: Shape = RoundedCornerShape(6.dp),
    val large: Shape = RoundedCornerShape(8.dp)
)

data class AppShadow(
    val card: Dp = 1.dp,
    val dialog: Dp = 2.dp,
    val drawer: Dp = 3.dp
)

data class AppSpacing(
    val tiny: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp
)

val LocalAppTypo = staticCompositionLocalOf { AppTypo() }

val LocalAppColor = staticCompositionLocalOf { AppColor() }

val LocalAppShape = staticCompositionLocalOf { AppShape() }

val LocalAppShadow = staticCompositionLocalOf { AppShadow() }

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }


@Composable
fun CanvasOneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    val typo = AppTypo()
    val color = AppColor()
    val shape = AppShape()
    val shadow = AppShadow()
    val spacing = AppSpacing()

    CompositionLocalProvider(
        LocalAppTypo provides typo,
        LocalAppColor provides color,
        LocalAppShape provides shape,
        LocalAppShadow provides shadow,
        LocalAppSpacing provides spacing
    ) {
        content.invoke()
    }
}

object AppTheme {
    val typo: AppTypo
        @Composable get() = LocalAppTypo.current
    val color: AppColor
        @Composable get() = LocalAppColor.current
    val shape: AppShape
        @Composable get() = LocalAppShape.current
    val shadow: AppShadow
        @Composable get() = LocalAppShadow.current
    val spacing: AppSpacing
        @Composable get() = LocalAppSpacing.current
}