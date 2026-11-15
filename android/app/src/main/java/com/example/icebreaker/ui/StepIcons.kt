package com.example.icebreaker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Groups3
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * shared/icebreakers.json の steps[].symbol は iOS の SF Symbols 名。
 * Android では見た目の意味が近い Material Icons に置き換える。
 * 対応表に無い名前は星アイコンで代用する（JSON に新しい symbol を足したらここにも追加）。
 */
object StepIcons {
    val map: Map<String, ImageVector> = mapOf(
        "arrow.left.arrow.right" to Icons.Filled.SwapHoriz,
        "arrow.right.circle.fill" to Icons.Filled.ArrowCircleRight,
        "arrow.triangle.2.circlepath" to Icons.Filled.Autorenew,
        "bolt.fill" to Icons.Filled.Bolt,
        "brain.head.profile" to Icons.Filled.Psychology,
        "bubble.left.and.bubble.right.fill" to Icons.Filled.Forum,
        "building.2.fill" to Icons.Filled.Apartment,
        "chair.fill" to Icons.Filled.Chair,
        "checkmark.seal.fill" to Icons.Filled.Verified,
        "circle.dashed" to Icons.Filled.TripOrigin,
        "envelope.fill" to Icons.Filled.Email,
        "figure.handball" to Icons.Filled.SportsHandball,
        "figure.run" to Icons.AutoMirrored.Filled.DirectionsRun,
        "figure.stand" to Icons.Filled.Accessibility,
        "figure.walk" to Icons.AutoMirrored.Filled.DirectionsWalk,
        "flag.fill" to Icons.Filled.Flag,
        "hand.point.right.fill" to Icons.Filled.TouchApp,
        "hand.point.up.left.fill" to Icons.Filled.TouchApp,
        "hand.raised.fill" to Icons.Filled.PanTool,
        "hand.raised.fingers.spread.fill" to Icons.Filled.BackHand,
        "hand.thumbsup.fill" to Icons.Filled.ThumbUp,
        "hands.and.sparkles.fill" to Icons.Filled.Handshake,
        "hands.clap.fill" to Icons.Filled.Celebration,
        "lightbulb.fill" to Icons.Filled.Lightbulb,
        "magnifyingglass" to Icons.Filled.Search,
        "megaphone.fill" to Icons.Filled.Campaign,
        "music.note" to Icons.Filled.MusicNote,
        "pawprint.fill" to Icons.Filled.Pets,
        "pencil.and.scribble" to Icons.Filled.Draw,
        "person.2.fill" to Icons.Filled.People,
        "person.3.fill" to Icons.Filled.Groups,
        "person.3.sequence.fill" to Icons.Filled.Groups3,
        "person.wave.2.fill" to Icons.Filled.RecordVoiceOver,
        "photo.fill" to Icons.Filled.Photo,
        "photo.on.rectangle.angled" to Icons.Filled.PhotoLibrary,
        "plus.circle.fill" to Icons.Filled.AddCircle,
        "questionmark.bubble.fill" to Icons.Filled.QuestionAnswer,
        "ruler.fill" to Icons.Filled.Straighten,
        "sparkles" to Icons.Filled.AutoAwesome,
        "speaker.slash.fill" to Icons.Filled.MicOff,
        "square.grid.3x3.fill" to Icons.Filled.GridOn,
        "star.fill" to Icons.Filled.Star,
        "text.bubble.fill" to Icons.Filled.Sms,
        "timer" to Icons.Filled.Timer,
        "trophy.fill" to Icons.Filled.EmojiEvents,
        "tshirt.fill" to Icons.Filled.Checkroom,
    )

    val fallback: ImageVector = Icons.Filled.Star

    fun forSymbol(symbol: String): ImageVector = map[symbol] ?: fallback
}
