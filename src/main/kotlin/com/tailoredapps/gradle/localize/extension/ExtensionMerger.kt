package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.LocalizationConfig
import com.tailoredapps.gradle.localize.util.PathToFileManager

class ExtensionMerger(
    private val pathToFileManager: PathToFileManager
) {

    fun merge(
        baseConfig: BaseLocalizeExtension,
        flavor: String?
    ): LocalizationConfig {
        //check if we even have a flavor dependent config
        return if (baseConfig.flavorConfigContainer.isEmpty() || flavor == null) {
            LocalizationConfig(
                serviceAccountCredentialsFile = pathToFileManager.pathToFile(baseConfig.serviceAccountCredentialsFile),
                sheetId = baseConfig.sheetId,
                languageTitles = baseConfig.languageTitles,
                baseLanguage = baseConfig.baseLanguage,
                localizationPath = pathToFileManager.pathToFile(baseConfig.localizationPath),
                addToCheckTask = baseConfig.addToCheckTask,
                addComments = baseConfig.addComments
            )
        } else {
            val flavorConfig = baseConfig.flavorConfigContainer.asMap[flavor]

            val serviceAccountCredentialsPath = flavorConfig?.serviceAccountCredentialsFile
                ?: baseConfig.serviceAccountCredentialsFile

            val localizationPath = flavorConfig?.localizationPath ?: baseConfig.localizationPath

            LocalizationConfig(
                serviceAccountCredentialsFile = pathToFileManager.pathToFile(serviceAccountCredentialsPath),
                sheetId = flavorConfig?.sheetId ?: baseConfig.sheetId,
                languageTitles = flavorConfig?.languageTitles ?: baseConfig.languageTitles,
                baseLanguage = flavorConfig?.baseLanguage ?: baseConfig.baseLanguage,
                localizationPath = pathToFileManager.pathToFile(localizationPath),
                addToCheckTask = baseConfig.addToCheckTask,
                addComments = flavorConfig?.addComments ?: baseConfig.addComments
            )
        }
    }


}