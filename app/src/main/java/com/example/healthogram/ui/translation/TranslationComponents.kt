package com.example.healthogram.ui.translation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramColors
import com.example.healthogram.translation.CaptionFontSize
import com.example.healthogram.translation.CaptionSettings
import com.example.healthogram.translation.LanguageItem
import com.example.healthogram.translation.TranslationCustomFunctions

/**
 * HEALTHOGRAM STEP 12: TRANSLATION REUSABLE COMPONENTS
 *
 * Material 3 accessible, polished UI components for:
 * - Message translation cards
 * - Voice transcription & translation controls
 * - Live caption overlay during RTC calls
 * - Call translation control panel
 * - Medical interpretation disclaimers
 */

/**
 * Inline Translate Button on Message Bubble
 */
@Composable
fun TranslateButton(
    isTranslating: Boolean,
    isTranslated: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.testTag("translate_button"),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Translate,
            contentDescription = "Translate message",
            modifier = Modifier.size(16.dp),
            tint = HealthogramColors.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isTranslating) "Translating..." else if (isTranslated) "Hide" else "Translate",
            style = MaterialTheme.typography.labelSmall,
            color = HealthogramColors.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Card displaying translated message alongside or underneath original
 */
@Composable
fun TranslatedMessageCard(
    originalText: String,
    translatedText: String,
    sourceLanguage: String,
    targetLanguage: String,
    isHealthcareContext: Boolean,
    onCopyTranslation: () -> Unit,
    onHideTranslation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .testTag("translated_message_card"),
        shape = RoundedCornerShape(12.dp),
        color = HealthogramColors.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramColors.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Header with language flags and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${TranslationCustomFunctions.getLanguageFlag(sourceLanguage)} → ${TranslationCustomFunctions.getLanguageFlag(targetLanguage)}",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Translated (${targetLanguage.uppercase()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = HealthogramColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCopyTranslation,
                        modifier = Modifier.size(28.dp).testTag("copy_translation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Translation",
                            tint = HealthogramColors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onHideTranslation,
                        modifier = Modifier.size(28.dp).testTag("hide_translation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hide Translation",
                            tint = HealthogramColors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Translated text body
            Text(
                text = translatedText,
                style = MaterialTheme.typography.bodyMedium,
                color = HealthogramColors.onSurface,
                fontWeight = FontWeight.Medium
            )

            // Contextual Healthcare disclaimer if applicable
            if (isHealthcareContext) {
                Spacer(modifier = Modifier.height(6.dp))
                MedicalTranslationDisclaimer()
            }
        }
    }
}

/**
 * Medical Interpretation Legal Disclaimer Card
 */
@Composable
fun MedicalTranslationDisclaimer(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HealthogramColors.amber500.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.WarningAmber,
            contentDescription = "Medical Translation Disclaimer",
            tint = HealthogramColors.amber500,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = TranslationCustomFunctions.getMedicalDisclaimerText(),
            style = MaterialTheme.typography.labelSmall,
            color = HealthogramColors.onSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
    }
}

/**
 * Live Caption Overlay during Audio/Video Calls
 */
@Composable
fun LiveCaptionOverlay(
    captions: List<Pair<String, String>>, // Pair(original, translated)
    captionSettings: CaptionSettings = CaptionSettings(),
    showTranslatedOnly: Boolean = false,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = captions.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("live_caption_overlay"),
            shape = RoundedCornerShape(14.dp),
            color = Color.Black.copy(alpha = captionSettings.backgroundColorAlpha)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HealthogramColors.emerald500)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE CAPTIONS (AI TRANSLATED)",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Captions",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Display recent caption lines
                val recentCaptions = captions.takeLast(3)
                val fontSizeSp = when (captionSettings.fontSize) {
                    CaptionFontSize.SMALL -> 13.sp
                    CaptionFontSize.MEDIUM -> 15.sp
                    CaptionFontSize.LARGE -> 18.sp
                    CaptionFontSize.EXTRA_LARGE -> 21.sp
                }

                recentCaptions.forEach { (original, translated) ->
                    if (!showTranslatedOnly) {
                        Text(
                            text = original,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = fontSizeSp * 0.9f,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    Text(
                        text = translated,
                        color = Color.White,
                        fontSize = fontSizeSp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * Call Translation Control Bar inside Active Audio/Video Call
 */
@Composable
fun CallTranslationControlBar(
    isTranslationActive: Boolean,
    targetLanguage: String,
    onToggleTranslation: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("call_translation_control_bar"),
        shape = RoundedCornerShape(20.dp),
        color = HealthogramColors.surfaceVariant.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramColors.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onToggleTranslation() }
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Toggle Translation",
                    tint = if (isTranslationActive) HealthogramColors.primary else HealthogramColors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTranslationActive) "Live Translation ON" else "Translation OFF",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isTranslationActive) HealthogramColors.primary else HealthogramColors.onSurfaceVariant
                )
            }

            if (isTranslationActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(HealthogramColors.primary.copy(alpha = 0.12f))
                        .clickable { onOpenLanguagePicker() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${TranslationCustomFunctions.getLanguageFlag(targetLanguage)} ${targetLanguage.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = HealthogramColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Language",
                        tint = HealthogramColors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
