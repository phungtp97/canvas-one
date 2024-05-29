package com.joshta.canvasone.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.joshta.canvasone.ui.theme.AppTheme

@Composable
fun PenSelector(
    size: Dp,
    initStroke: Dp,
    initStability: Int,
    onStrokeChange: (Dp) -> Unit,
    onStabilityChange: (Int) -> Unit,
    expanded: Boolean,
    controller: ColorPickerController,
    onExpandedChange: (Boolean) -> Unit,
    onColorChange: (Color) -> Unit
) {
    var strokeSliderValue by remember { mutableStateOf(initStroke) }
    var stabilitySliderValue by remember { mutableStateOf(initStability) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        offset = DpOffset((-size / 2), 0.dp),
    ) {

        Row(verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.width(94.dp)) {
                Text(
                    "Color: ",
                    style = AppTheme.typo.h4,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                )
            }
            ColorPicker(controller)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(94.dp)) {
                Text(
                    "Size: ",
                    style = AppTheme.typo.h4,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
            Slider(
                modifier = Modifier
                    .width(200.dp)
                    .height(44.dp)
                    .padding(horizontal = 10.dp),
                value = strokeSliderValue.value,
                onValueChange = { newValue ->
                    strokeSliderValue = newValue.dp
                    onStrokeChange(newValue.dp)
                },
                valueRange = 0f..9f,
                steps = 0,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(94.dp)) {
                Text(
                    "Stability: ",
                    style = AppTheme.typo.h4,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
            Slider(
                modifier = Modifier
                    .width(200.dp)
                    .height(44.dp)
                    .padding(horizontal = 10.dp),
                value = stabilitySliderValue.toFloat(),
                onValueChange = { newValue ->
                    stabilitySliderValue = newValue.toInt()
                    onStabilityChange(newValue.toInt())
                },
                valueRange = 0f..10f,
                steps = 9,
            )
        }
    }
}

@Composable
fun ColorPicker(controller: ColorPickerController) {

    Column(
        modifier = Modifier
            .height(240.dp)
            .padding(all = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlphaTile(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp)),
                selectedColor = controller.selectedColor.value,
                controller = controller
            )
        }
        HsvColorPicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(4.dp),
            controller = controller,
            onColorChanged = {}
        )
        AlphaSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .height(24.dp),
            controller = controller,
            tileOddColor = Color.White,
            tileEvenColor = Color.Black
        )
        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .height(24.dp),
            initialColor = controller.selectedColor.value,
            controller = controller,
        )
    }
}