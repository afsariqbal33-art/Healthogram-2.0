package com.example.healthogram.ui.translation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramColors
import com.example.healthogram.translation.*

/**
 * HEALTHOGRAM STEP 12: TRANSLATION PAGES SUITE
 *
 * Implements:
 * 1. TranslationSettingsPage
 * 2. LanguageSelectionPage
 * 3. TranslationPreferencesPage / CaptionSettingsPage
 * 4. TranslationConsentDialog
 * 5. TranslationHistoryPage
 * 6. TranslationUsagePage
 */

/**
 * Main Translation Settings Page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSettingsPage(
    settings: UserTranslationSettings,
    onUpdateSettings: (UserTranslationSettings) -> Unit,
    onNavigateToLanguageSelect: (isSource: Boolean) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToUsage: () -> Unit,
    onNavigateToCaptions: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConsentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Translation & Languages", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HealthogramColors.surface,
                    titleContentColor = HealthogramColors.onSurface
                )
            )
        },
        modifier = modifier.testTag("translation_settings_page")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Language vs Communication Language Explanatory Banner (Section 4)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HealthogramColors.primary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramColors.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Language Architecture Info",
                            tint = HealthogramColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "App Language vs Communication Language",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HealthogramColors.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "App Language controls Healthogram UI buttons and navigation. Communication Language controls real-time translation of messages, voice notes, and calls.",
                                style = MaterialTheme.typography.bodySmall,
                                color = HealthogramColors.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Language Selection Section
            item {
                Text(
                    text = "TARGET & SOURCE LANGUAGES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = HealthogramColors.onSurfaceVariant
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramColors.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Target Language Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToLanguageSelect(false) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Translate Everything To", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Preferred Target Language", style = MaterialTheme.typography.bodySmall, color = HealthogramColors.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${TranslationCustomFunctions.getLanguageFlag(settings.targetLanguage)} ${settings.targetLanguage.uppercase()}",
                                    fontWeight = FontWeight.Bold,
                                    color = HealthogramColors.primary
                                )
                                Icon(Icons.Default.ChevronRight, contentDescription = "Select Target Language", tint = HealthogramColors.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = HealthogramColors.outlineVariant)

                        // Auto Detect Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Automatic Language Detection", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Automatically detects source language of messages", style = MaterialTheme.typography.bodySmall, color = HealthogramColors.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.autoDetectLanguage,
                                onCheckedChange = { onUpdateSettings(settings.copy(autoDetectLanguage = it)) },
                                modifier = Modifier.testTag("auto_detect_switch")
                            )
                        }
                    }
                }
            }

            // Communication Channels Section
            item {
                Text(
                    text = "FEATURE CONTROLS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = HealthogramColors.onSurfaceVariant
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramColors.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Text Translation Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Text Message Translation", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Show translate option on received chat messages", style = MaterialTheme.typography.bodySmall, color = HealthogramColors.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.textTranslationEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(textTranslationEnabled = it)) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = HealthogramColors.outlineVariant)

                        // Voice Translation Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Voice Message Translation", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Transcribe and translate incoming voice notes", style = MaterialTheme.typography.bodySmall, color = HealthogramColors.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.voiceTranslationEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(voiceTranslationEnabled = it)) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = HealthogramColors.outlineVariant)

                        // Call Translation (High complexity, optional, requires consent)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Live Call Translation & Captions", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Real-time subtitles during audio & video calls", style = MaterialTheme.typography.bodySmall, color = HealthogramColors.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.callTranslationEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        showConsentDialog = true
                                    } else {
                                        onUpdateSettings(settings.copy(callTranslationEnabled = false, captionTranslationEnabled = false))
                                    }
                                }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = HealthogramColors.outlineVariant)

                        // Translated Audio (Optional generic TTS, defaults to OFF)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Translated Voice Output", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Generate synthesized neutral voice for translations", style = MaterialTheme.typography.bodySmall, color = HealthogramColors.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.translatedAudioEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(translatedAudioEnabled = it)) }
                            )
                        }
                    }
                }
            }

            // Display Preferences
            item {
                Text(
                    text = "DISPLAY PREFERENCES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = HealthogramColors.onSurfaceVariant
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramColors.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Show Original Message", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = settings.showOriginalText,
                                onCheckedChange = { onUpdateSettings(settings.copy(showOriginalText = it)) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = HealthogramColors.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Translate Incoming Automatically", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = settings.translateIncomingMessages,
                                onCheckedChange = { onUpdateSettings(settings.copy(translateIncomingMessages = it)) }
                            )
                        }
                    }
                }
            }

            // Quick Links: Caption Styling, History, Usage
            item {
                Text(
                    text = "ACCESSIBILITY & USAGE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = HealthogramColors.onSurfaceVariant
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramColors.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToCaptions() }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ClosedCaption, contentDescription = null, tint = HealthogramColors.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Live Caption Accessibility Settings", fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramColors.onSurfaceVariant)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = HealthogramColors.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToHistory() }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, tint = HealthogramColors.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Translation History", fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramColors.onSurfaceVariant)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = HealthogramColors.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToUsage() }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DataUsage, contentDescription = null, tint = HealthogramColors.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Usage & Cost Control Dashboard", fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramColors.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Explicit Call Translation Consent Dialog (Section 23)
    if (showConsentDialog) {
        TranslationConsentDialog(
            onConfirm = {
                onUpdateSettings(settings.copy(callTranslationEnabled = true, captionTranslationEnabled = true))
                showConsentDialog = false
            },
            onDismiss = {
                showConsentDialog = false
            }
        )
    }
}

/**
 * Language Selection Page with search, native names, country flags, and RTL indicators
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionPage(
    languages: List<LanguageItem>,
    selectedCode: String,
    onSelectLanguage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredLanguages = remember(searchQuery, languages) {
        if (searchQuery.isBlank()) languages
        else languages.filter {
            it.languageName.contains(searchQuery, ignoreCase = true) ||
            it.nativeName.contains(searchQuery, ignoreCase = true) ||
            it.languageCode.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Language", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("language_selection_page")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search language or code...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("language_search_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLanguages, key = { it.languageCode }) { lang ->
                    val isSelected = lang.languageCode.equals(selectedCode, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLanguage(lang.languageCode) }
                            .testTag("language_item_${lang.languageCode}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) HealthogramColors.primary.copy(alpha = 0.12f) else HealthogramColors.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) HealthogramColors.primary else HealthogramColors.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = TranslationCustomFunctions.getLanguageFlag(lang.languageCode),
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = lang.languageName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) HealthogramColors.primary else HealthogramColors.onSurface
                                    )
                                    Text(
                                        text = "${lang.nativeName} (${lang.languageCode.uppercase()})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HealthogramColors.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (lang.isRtl) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = HealthogramColors.primary.copy(alpha = 0.15f),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = "RTL",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HealthogramColors.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = HealthogramColors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Educational Consent Dialog before enabling live call translation (Section 23)
 */
@Composable
fun TranslationConsentDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = "Consent Notice",
                tint = HealthogramColors.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Live Call Translation Consent",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Live translation processes spoken audio in real-time using secure AI translation and speech recognition services.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Audio is processed ephemerally and is NOT permanently recorded.\n• AI translation may contain inaccuracies.\n• For critical clinical consultations, consider a certified human medical interpreter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = HealthogramColors.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = HealthogramColors.primary),
                modifier = Modifier.testTag("consent_enable_button")
            ) {
                Text("Enable Translation")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Caption Accessibility Settings Page (Section 52)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptionSettingsPage(
    settings: CaptionSettings,
    onUpdateSettings: (CaptionSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caption Accessibility", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("caption_settings_page")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("CAPTION FONT SIZE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CaptionFontSize.entries.forEach { size ->
                    val isSelected = settings.fontSize == size
                    Button(
                        onClick = { onUpdateSettings(settings.copy(fontSize = size)) },
                        colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = HealthogramColors.primary)
                                 else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(size.name.replace("_", " ").lowercase().capitalize())
                    }
                }
            }

            HorizontalDivider(color = HealthogramColors.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("High Contrast Subtitles", fontWeight = FontWeight.SemiBold)
                    Text("Enhances subtitle readability against complex video backgrounds", style = MaterialTheme.typography.bodySmall, color = HealthogramColors.onSurfaceVariant)
                }
                Switch(
                    checked = settings.highContrast,
                    onCheckedChange = { onUpdateSettings(settings.copy(highContrast = it)) }
                )
            }
        }
    }
}

/**
 * Translation History Page (Section 33)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationHistoryPage(
    historyItems: List<TranslationHistoryItem>,
    onDeleteItem: (String) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Translation History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (historyItems.isNotEmpty()) {
                        TextButton(onClick = onClearAll) {
                            Text("Clear All", color = HealthogramColors.error)
                        }
                    }
                }
            )
        },
        modifier = modifier.testTag("translation_history_page")
    ) { innerPadding ->
        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No translation history found.", color = HealthogramColors.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyItems, key = { it.translationId }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthogramColors.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${TranslationCustomFunctions.getLanguageFlag(item.sourceLanguage)} → ${TranslationCustomFunctions.getLanguageFlag(item.targetLanguage)}",
                                    fontSize = 13.sp
                                )
                                IconButton(onClick = { onDeleteItem(item.translationId) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = HealthogramColors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = item.sourceReference, style = MaterialTheme.typography.bodySmall, color = HealthogramColors.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = item.translationReference, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Translation Usage & Cost Control Dashboard (Section 34)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationUsagePage(
    summary: TranslationUsageSummary,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (summary.limitUnits - summary.remainingUnits).toFloat() / summary.limitUnits.coerceAtLeast(1L)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usage & Cost Controls", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("translation_usage_page")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HealthogramColors.primary.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramColors.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Monthly Quota Remaining", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = HealthogramColors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${summary.remainingUnits} units left", fontWeight = FontWeight.SemiBold)
                        Text("Limit: ${summary.limitUnits}", color = HealthogramColors.onSurfaceVariant)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HealthogramColors.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("MONTHLY CONSUMPTION METRICS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Text Characters Translated")
                        Text("${summary.monthlyTextUnits}", fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Voice Message Seconds")
                        Text("${summary.monthlyAudioSeconds} s", fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Call Translation Minutes")
                        Text("${summary.monthlyCallMinutes} min", fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estimated Provider Cost")
                        Text("$${String.format("%.4f", summary.monthlyEstimatedCost)}", fontWeight = FontWeight.Bold, color = HealthogramColors.primary)
                    }
                }
            }
        }
    }
}
