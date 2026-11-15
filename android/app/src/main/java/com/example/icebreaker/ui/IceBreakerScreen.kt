package com.example.icebreaker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.icebreaker.model.Audience
import com.example.icebreaker.model.Icebreaker
import com.example.icebreaker.model.PeopleCount
import com.example.icebreaker.model.Recommendation
import com.example.icebreaker.model.Venue

@Composable
fun IceBreakerScreen(model: AppViewModel) {
    val scroll = rememberScrollState()
    var resultTop by remember { mutableIntStateOf(0) }

    // 結果を出したら結果の位置まで自動スクロール（元アプリと同じ動き）
    LaunchedEffect(model.resultRevision) {
        if (model.resultRevision > 0) scroll.animateScrollTo(resultTop)
    }

    Box(
        Modifier
            .fillMaxSize()
            .dottedBackground(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .verticalScroll(scroll)
                .safeDrawingPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Header()
            ConditionCard(model)
            if (model.ai.status.isAvailable) AiConsultCard(model)
            if (model.current != null) {
                Box(Modifier.onGloballyPositioned { resultTop = it.positionInParent().y.toInt() }) {
                    ResultSection(model)
                }
            }
            FooterNote(model.ai.status.isAvailable, model.ai.status.message)
        }
    }

    model.aiErrorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { model.aiErrorMessage = null },
            confirmButton = { TextButton(onClick = { model.aiErrorMessage = null }) { Text("OK") } },
            title = { Text("AIからのお知らせ") },
            text = { Text(message) },
        )
    }
}

// MARK: - ヘッダー

@Composable
private fun Header() {
    Column(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val badgeShape = RoundedCornerShape(50)
        Text(
            "🎉 場の空気を一瞬でほぐす！",
            style = popText(13.sp),
            modifier = Modifier
                .rotate(-2f)
                .background(Palette.yellow, badgeShape)
                .border(2.5.dp, Palette.ink, badgeShape)
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )
        Box {
            // 元アプリの見出しの「ずらした影」
            Text(
                "アイスブレイク\n提案メーカー",
                style = popText(34.sp, FontWeight.Black, Palette.orangeDeep.copy(alpha = 0.35f)),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 2.dp, top = 2.dp),
            )
            Text(
                "アイスブレイク\n提案メーカー",
                style = popText(34.sp, FontWeight.Black, Palette.orange),
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "条件を選ぶだけで、イラスト付きのわかりやすい進行手順を提案するよ！",
            style = popText(13.sp, FontWeight.Bold, Palette.gray),
            textAlign = TextAlign.Center,
        )
    }
}

// MARK: - 条件入力

@Composable
private fun ConditionCard(model: AppViewModel) {
    PopCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("🔍 どんな集まりですか？", style = popText(20.sp))
            ConditionPanel("👥 人数は？", Palette.peoplePanel) {
                SelectBox(model.conditions.people, PeopleCount.entries, { it.label }, {
                    model.conditions = model.conditions.copy(people = it)
                })
            }
            ConditionPanel("🎈 参加者の属性は？", Palette.audiencePanel) {
                SelectBox(model.conditions.audience, Audience.entries, { it.label }, {
                    model.conditions = model.conditions.copy(audience = it)
                })
            }
            ConditionPanel("🏢 会場の広さは？", Palette.venuePanel) {
                SelectBox(model.conditions.venue, Venue.entries, { it.label }, {
                    model.conditions = model.conditions.copy(venue = it)
                })
            }
            PopButton(
                "✨ アイディアを出す！",
                onClick = model::generate,
                shadow = Palette.orangeDeep,
                fontSize = 20.sp,
                verticalPadding = 16.dp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun ConditionPanel(title: String, style: Palette.Panel, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .background(style.background, shape)
            .border(2.dp, style.border, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = popText(15.sp, color = style.title))
        content()
    }
}

// MARK: - AIに相談（AI が使えるときだけ表示）

@Composable
private fun AiConsultCard(model: AppViewModel) {
    PopCard(background = Palette.violetSoft) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🤖 AIに相談して条件を決める", style = popText(17.sp, color = Palette.indigoDeep))
            Text("集まりの様子を書くと、AIが条件を読み取って提案します。", style = popText(13.sp, FontWeight.Bold, Palette.gray))
            OutlinedTextField(
                value = model.situationText,
                onValueChange = { model.situationText = it },
                placeholder = { Text("例：来週の新人研修。20人くらいで会議室。初対面同士") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Palette.ink,
                    unfocusedBorderColor = Palette.ink,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            val busy = model.busy == AppViewModel.AiBusy.SITUATION
            PopButton(
                if (busy) "AIが考え中…" else "💬 AIに相談する",
                onClick = model::askAi,
                fill = solid(Palette.violet),
                enabled = model.busy == null && model.situationText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            model.situationAdvice?.let {
                Text(
                    "💡 $it",
                    style = popText(14.sp, FontWeight.Bold, Palette.indigoDeep),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                )
            }
        }
    }
}

// MARK: - 結果

@Composable
private fun ResultSection(model: AppViewModel) {
    val current = model.current ?: return
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "💡 おすすめのアイスブレイク",
                    style = popText(19.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${model.currentIndex + 1} / ${model.recommendations.size} 件",
                    style = popText(12.sp, FontWeight.Bold, Palette.gray),
                )
            }
            Spacer(Modifier.size(8.dp))
            PopButton(
                "🔄 別のアイディア",
                onClick = model::showNext,
                fill = solid(Palette.teal),
                fontSize = 14.sp,
                verticalPadding = 9.dp,
            )
        }
        IdeaCard(model, current)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IdeaCard(model: AppViewModel, recommendation: Recommendation) {
    val item = recommendation.icebreaker
    PopCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Chip("⏱️ 目安時間：${item.minutes}", Palette.yellow)
                    Chip("✨ 盛り上がり度 ${stars(item.excitement)}", Palette.pinkChip)
                }
                MatchBadge(recommendation)
                Text(item.title, style = popText(24.sp, FontWeight.Black))
                Text(item.summary, style = popText(14.sp, FontWeight.Bold, Palette.gray))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Work, contentDescription = null, tint = Palette.gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(item.preparation, style = popText(13.sp, FontWeight.Bold, Palette.gray))
                }
            }
            DashedDivider()
            TopicPanel(model)
            StepsSection(model)
        }
    }
}

private fun stars(value: Int): String {
    val v = value.coerceIn(1, 5)
    return "★".repeat(v) + "☆".repeat(5 - v)
}

@Composable
private fun MatchBadge(recommendation: Recommendation) {
    val perfect = recommendation.isPerfect
    val fg = if (perfect) Color(0xFF166534) else Color(0xFF9A3412)
    val bg = if (perfect) Color(0xFFDCFCE7) else Color(0xFFFFEDD5)
    Row(
        Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (perfect) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.size(4.dp))
        Text(recommendation.matchLabel, style = popText(12.sp, color = fg))
    }
}

@Composable
private fun DashedDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(2.dp)
            .drawBehind {
                drawLine(
                    Palette.divider,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = size.height,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())),
                )
            },
    )
}

// MARK: - お題

@Composable
private fun TopicPanel(model: AppViewModel) {
    val topics = model.topics
    Column(
        Modifier
            .fillMaxWidth()
            .background(Palette.indigoSoft, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("🎯 使う「お題・ルール」を選ぼう！", style = popText(16.sp, color = Palette.indigoDeep))
        PopButton(
            "🎲 ランダムにお題を変える！",
            onClick = model::randomTopic,
            fill = solid(Palette.indigo),
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("現在の選択お題", style = popText(12.sp, color = Palette.indigo))
                if (model.currentTopic?.isAi == true) {
                    Spacer(Modifier.size(6.dp))
                    Chip("✨ AI作成", Palette.violetSoft)
                }
            }
            Text(
                model.currentTopic?.text.orEmpty(),
                style = popText(22.sp, FontWeight.Black),
                textAlign = TextAlign.Center,
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .border(2.dp, Palette.indigoBorder, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("📋 リストから選ぶ", style = popText(13.sp, color = Palette.indigoDeep))
            SelectBox(
                selected = model.topicIndex.coerceIn(0, (topics.size - 1).coerceAtLeast(0)),
                options = topics.indices.toList(),
                label = { i -> "${i + 1}. ${topics.getOrNull(i)?.let { it.text + if (it.isAi) "（AI）" else "" }.orEmpty()}" },
                onSelect = model::selectTopic,
                fontSize = 14.sp,
                shape = RoundedCornerShape(12.dp),
            )
        }
        if (model.ai.status.isAvailable) {
            PopButton(
                if (model.busy == AppViewModel.AiBusy.TOPICS) "AIがお題を考え中…" else "✨ AIでこの集まり向けのお題を追加",
                onClick = model::addAiTopics,
                fill = solid(Color.White),
                textColor = Palette.violet,
                fontSize = 15.sp,
                verticalPadding = 10.dp,
                enabled = model.busy == null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// MARK: - 進め方

@Composable
private fun StepsSection(model: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("📖 わかりやすい進め方（3ステップ）", style = popText(17.sp))
        if (model.ai.status.isAvailable) {
            val title = when {
                model.busy == AppViewModel.AiBusy.GUIDE -> "AIがアレンジ中…"
                model.showAdaptedGuide -> "↩︎ 元の説明に戻す"
                else -> "✨ AIでこの参加者向けの説明にアレンジ"
            }
            PopButton(
                title,
                onClick = model::toggleAdaptedGuide,
                fill = solid(Color.White),
                textColor = Palette.violet,
                fontSize = 15.sp,
                verticalPadding = 10.dp,
                enabled = model.busy == null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AnimatedVisibility(model.showAdaptedGuide) {
            Chip("✨ AIが「${model.resultConditions.audience.label}」向けにアレンジ", Palette.violetSoft)
        }
        model.displayedSteps.forEachIndexed { index, step -> StepCard(index, step) }
        if (model.displayedHostTip.isNotEmpty()) HostTip(model.displayedHostTip)
    }
}

@Composable
private fun StepCard(index: Int, step: Icebreaker.Step) {
    val bg = Palette.stepBackgrounds[index % Palette.stepBackgrounds.size]
    val fg = Palette.stepSymbolColors[index % Palette.stepSymbolColors.size]
    val cardShape = RoundedCornerShape(20.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .background(Palette.amberSoft, cardShape)
            .border(2.dp, Palette.amberBorder, cardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // STEP バッジ（オレンジ＋太枠＋ずらし影）
        Box(Modifier.padding(end = 2.dp, bottom = 3.dp)) {
            val pill = RoundedCornerShape(50)
            Box(Modifier.matchParentSize().padding(start = 2.dp, top = 3.dp).background(Palette.ink, pill))
            Text(
                "STEP ${index + 1}",
                style = popText(13.sp, FontWeight.Black, Color.White),
                modifier = Modifier
                    .background(Palette.orange, pill)
                    .border(2.5.dp, Palette.ink, pill)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        val frame = RoundedCornerShape(14.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(frame)
                .background(bg)
                .border(2.5.dp, Palette.ink, frame),
            contentAlignment = Alignment.Center,
        ) {
            Icon(StepIcons.forSymbol(step.symbol), contentDescription = null, tint = fg, modifier = Modifier.size(52.dp))
        }
        Text(step.title, style = popText(17.sp, FontWeight.Black))
        Text(step.description, style = popText(14.sp, FontWeight.Bold, Palette.gray))
    }
}

@Composable
private fun HostTip(text: String) {
    Box(Modifier.padding(top = 10.dp)) {
        Text(
            "🎤 $text",
            style = popText(14.sp, FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.tipBackground, RoundedCornerShape(16.dp))
                .padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 14.dp),
        )
        Text(
            "司会のコツ",
            style = popText(11.sp, color = Color.White),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp)
                .background(Palette.orange, RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

// MARK: - フッター

@Composable
private fun FooterNote(aiAvailable: Boolean, aiMessage: String?) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.8f), shape)
            .border(2.dp, Color(0xFFE5E7EB), shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val style = popText(12.sp, FontWeight.Bold, Palette.gray)
        Text("🔒 安心設計：入力した条件などの個人情報は保存・送信しません。", style = style)
        if (aiAvailable) {
            Text("🤖 AI機能はすべてこの端末の中で処理します。", style = style)
        } else if (aiMessage != null) {
            Text("ℹ️ $aiMessage", style = style)
        }
    }
}
