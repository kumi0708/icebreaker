package com.example.icebreaker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 共有元アプリ（Gemini Canvas 版）と iOS 版 Theme.swift の配色をなぞったテーマ */
object Palette {
    val ink = Color(0xFF1F2937)
    val cream = Color(0xFFFFFBEB)
    val dot = Color(0x8CFCD34D)
    val orange = Color(0xFFF97316)
    val orangeLight = Color(0xFFFB923C)
    val orangeDeep = Color(0xFFC2410C)
    val yellow = Color(0xFFFDE047)
    val pinkChip = Color(0xFFFBCFE8)
    val teal = Color(0xFF14B8A6)
    val indigo = Color(0xFF6366F1)
    val indigoDeep = Color(0xFF1E1B4B)
    val indigoSoft = Color(0xFFEEF2FF)
    val indigoBorder = Color(0xFFC7D2FE)
    val violet = Color(0xFF8B5CF6)
    val violetSoft = Color(0xFFF5F3FF)
    val gray = Color(0xFF4B5563)
    val divider = Color(0xFFD1D5DB)
    val amberSoft = Color(0xFFFFFBEB)
    val amberBorder = Color(0xFFFDE68A)
    val tipBackground = Color(0xFFFEF3C7)

    data class Panel(val background: Color, val border: Color, val title: Color)

    val peoplePanel = Panel(Color(0xFFEFF6FF), Color(0xFFBFDBFE), Color(0xFF1E40AF))
    val audiencePanel = Panel(Color(0xFFFDF2F8), Color(0xFFFBCFE8), Color(0xFF9D174D))
    val venuePanel = Panel(Color(0xFFF0FDF4), Color(0xFFBBF7D0), Color(0xFF166534))

    /** ステップのイラスト背景（ピンク・青・黄色の順） */
    val stepBackgrounds = listOf(Color(0xFFFCE7F3), Color(0xFFE0E7FF), Color(0xFFFEF9C3))
    val stepSymbolColors = listOf(Color(0xFFE11D48), Color(0xFF4F46E5), Color(0xFFD97706))
}

fun popText(size: TextUnit, weight: FontWeight = FontWeight.ExtraBold, color: Color = Palette.ink) =
    TextStyle(fontSize = size, fontWeight = weight, color = color, lineHeight = size * 1.35f)

/** 水玉模様の背景 */
fun Modifier.dottedBackground(): Modifier = this
    .background(Palette.cream)
    .drawBehind {
        val spacing = 22.dp.toPx()
        val r = 1.5.dp.toPx()
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawCircle(Palette.dot, radius = r, center = Offset(x + r, y + r))
                x += spacing
            }
            y += spacing
        }
    }

/** 太い枠線＋ずらした影の「ポップ」なカード */
@Composable
fun PopCard(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    radius: Dp = 24.dp,
    shadowOffset: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Box(modifier.padding(end = shadowOffset, bottom = shadowOffset)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(shadowOffset, shadowOffset)
                .background(Palette.ink, shape),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .background(background, shape)
                .border(3.dp, Palette.ink, shape),
            content = content,
        )
    }
}

/** 押すと沈むポップなボタン */
@Composable
fun PopButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fill: Brush = Brush.linearGradient(listOf(Palette.orangeLight, Palette.orange)),
    shadow: Color = Palette.ink,
    textColor: Color = Color.White,
    fontSize: TextUnit = 17.sp,
    verticalPadding: Dp = 12.dp,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(50)
    Box(modifier.padding(bottom = 5.dp)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(y = if (pressed) 1.dp else 5.dp)
                .background(shadow, shape),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .offset(y = if (pressed) 4.dp else 0.dp)
                .background(fill, shape, alpha = if (enabled) 1f else 0.6f)
                .border(3.dp, Palette.ink, shape)
                .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
                .padding(vertical = verticalPadding, horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, style = popText(fontSize, FontWeight.ExtraBold, textColor))
        }
    }
}

fun solid(color: Color): Brush = Brush.linearGradient(listOf(color, color))

/** 小さなラベル（目安時間・盛り上がり度など） */
@Composable
fun Chip(text: String, background: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Text(
        text,
        style = popText(12.sp),
        modifier = modifier
            .background(background, shape)
            .border(2.dp, Palette.ink, shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** プルダウン風の選択ボタン（元アプリの select をなぞる） */
@Composable
fun <T> SelectBox(
    selected: T,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 15.sp,
    shape: Shape = RoundedCornerShape(14.dp),
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.wrapContentSize(Alignment.TopStart)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.White, shape)
                .border(2.5.dp, Palette.ink, shape)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label(selected), style = popText(fontSize), modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Palette.ink)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            (if (option == selected) "✓ " else "") + label(option),
                            style = popText(15.sp, FontWeight.Bold),
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}
