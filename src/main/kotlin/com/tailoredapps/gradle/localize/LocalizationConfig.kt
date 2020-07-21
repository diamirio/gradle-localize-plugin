package com.tailoredapps.gradle.localize

import java.io.File

data class LocalizationConfig(
    val productName: String,
    val serviceAccountCredentialsFile: File,
    val sheetId: String,
    val languageTitles: List<String>,
    val baseLanguage: String,
    val localizationPath: File,
    val addToCheckTask: Boolean,
    val addComments: Boolean
)