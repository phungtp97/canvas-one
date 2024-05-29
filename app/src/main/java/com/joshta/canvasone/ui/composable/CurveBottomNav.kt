
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

@Composable
fun CurvedBottomBar(
    modifier: Modifier = Modifier,
    curveRadius: Float = 140f,
    curvePercent: Float = 0.3f,
    primaryContent: @Composable BoxScope.() -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Shadowed(
            modifier = modifier,
            color = Color.Black.copy(alpha = 0.1f),
            offsetX = 0.dp,
            offsetY = (-2f).dp,
            blurRadius = 2.dp
        ) {
            BoxWithConstraints(modifier = modifier) {
                val width = constraints.maxWidth.toFloat() / (1 - curvePercent)
                val height = constraints.maxHeight.toFloat() * (1 - curvePercent)
                Log.d("CurvedBottomBar", "width: $width, height: $height, curveWidth: $curveRadius")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height.dp)
                        .clip(shape = CurvedBottomShape(curveRadius, curvePercent))
                        .background(Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        content = content
                    )
                }
            }
        }
        Box(modifier = modifier.offset(y= (-32).dp), contentAlignment = Alignment.Center) {
            primaryContent()
        }
    }
}

class CurvedBottomShape(
    private val curveRadius: Float,
    private val curvePercent: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width / 2 - curveRadius * 2, 0f)
            cubicTo(
                size.width / 2 - curveRadius / 1.5f, -1f,
                size.width / 2 - curveRadius * 1.2f, curveRadius - 5f,
                size.width / 2, curveRadius
            )
            cubicTo(
                size.width / 2 + curveRadius * 1.2f, curveRadius - 5f,
                size.width / 2 + curveRadius / 1.5f, -1f,
                size.width / 2 + curveRadius * 2, 0f
            )
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            lineTo(0f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun Shadowed(
    modifier: Modifier,
    color: Color,
    offsetX: Dp,
    offsetY: Dp,
    blurRadius: Dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val offsetXPx = with(density) { offsetX.toPx() }.toInt()
    val offsetYPx = with(density) { offsetY.toPx() }.toInt()
    val blurRadiusPx = ceil(with(density) {
        blurRadius.toPx()
    }).toInt()

    // Modifier to render the content in the shadow color, then
    // blur it by blurRadius
    val shadowModifier = Modifier
        .drawWithContent {
            val matrix = shadowColorMatrix(color)
            val filter = ColorFilter.colorMatrix(matrix)
            val paint = Paint().apply {
                colorFilter = filter
            }
            drawIntoCanvas { canvas ->
                canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
                drawContent()
                canvas.restore()
            }
        }
        .blur(radius = blurRadius, BlurredEdgeTreatment.Unbounded)
        .padding(all = blurRadius) // Pad to prevent clipping blur

    // Layout based solely on the content, placing shadow behind it
    Layout(modifier = modifier, content = {
        // measurables[0] = content, measurables[1] = shadow
        content()
        Box(modifier = shadowModifier) { content() }
    }) { measurables, constraints ->
        // Allow shadow to go beyond bounds without affecting layout
        val contentPlaceable = measurables[0].measure(constraints)
        val shadowPlaceable = measurables[1].measure(
            Constraints(
                maxWidth = contentPlaceable.width + blurRadiusPx * 2,
                maxHeight = contentPlaceable.height + blurRadiusPx * 2
            )
        )
        layout(width = contentPlaceable.width, height = contentPlaceable.height) {
            shadowPlaceable.placeRelative(
                x = offsetXPx - blurRadiusPx,
                y = offsetYPx - blurRadiusPx
            )
            contentPlaceable.placeRelative(x = 0, y = 0)
        }
    }
}

// Return a color matrix with which to paint our content
// as a shadow of the given color
private fun shadowColorMatrix(color: Color): ColorMatrix {
    return ColorMatrix().apply {
        set(0, 0, 0f) // Do not preserve original R
        set(1, 1, 0f) // Do not preserve original G
        set(2, 2, 0f) // Do not preserve original B

        set(0, 4, color.red * 255) // Use given color's R
        set(1, 4, color.green * 255) // Use given color's G
        set(2, 4, color.blue * 255) // Use given color's B
        set(3, 3, color.alpha) // Multiply by given color's alpha
    }
}