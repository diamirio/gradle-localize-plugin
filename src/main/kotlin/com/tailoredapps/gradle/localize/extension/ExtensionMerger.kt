package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.LocalizationConfig
import com.tailoredapps.gradle.localize.util.PathToFileManager

class ExtensionMerger(
    private val pathToFileManager: PathToFileManager
) {

    fun merge(
        baseConfig: BaseLocalizeExtension,
        productConfigName: String,
        productConfig: ProductLocalizeExtension
    ): LocalizationConfig {
        val serviceAccountCredentialsPath = productConfig.serviceAccountCredentialsFile
            ?: baseConfig.serviceAccountCredentialsFile

        val localizationPath = productConfig.localizationPath

        return LocalizationConfig(
            productName = productConfigName,
            serviceAccountCredentialsFile = pathToFileManager.pathToFile(
                serviceAccountCredentialsPath
            ),
            sheetId = productConfig.sheetId,
            worksheets = productConfig.worksheets?.toList(),
            languageTitles = productConfig.languageTitles,
            baseLanguage = productConfig.baseLanguage ?: baseConfig.baseLanguage,
            localizationPath = pathToFileManager.pathToFile(localizationPath),
            addComments = productConfig.addComments ?: baseConfig.addComments,
            escapeApostrophes = productConfig.escapeApostrophes ?: baseConfig.escapeApostrophes,
            generateEmptyValues = productConfig.generateEmptyValues ?: baseConfig.generateEmptyValues,
        )
    }


}