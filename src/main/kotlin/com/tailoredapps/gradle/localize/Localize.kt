package com.tailoredapps.gradle.localize

import com.tailoredapps.gradle.localize.android.AndroidStringXmlGenerator
import com.tailoredapps.gradle.localize.android.ParsedSheetToAndroidTransformer
import com.tailoredapps.gradle.localize.drive.DriveManager
import com.tailoredapps.gradle.localize.localization.LocalizationSheetParser
import com.tailoredapps.gradle.localize.util.forEachParallel
import java.io.File
import java.io.IOException

class Localize {

    private val driveManager: DriveManager = DriveManager()
    private val localizationSheetParser: LocalizationSheetParser = LocalizationSheetParser()
    private val androidSheetTransformer: ParsedSheetToAndroidTransformer = ParsedSheetToAndroidTransformer()
    private val stringXmlGenerator: AndroidStringXmlGenerator = AndroidStringXmlGenerator()

    suspend fun localize(
        sheetId: String,
        serviceAccountCredentialsFile: File,
        languageTitles: List<String>,
        baseLanguage: String,
        localizationPath: File,
        addComments: Boolean
    ) {
        val sheet = driveManager.getSheet(
            serviceAccountCredentialsFile = serviceAccountCredentialsFile,
            sheetId = sheetId
        )
        val parsedSheet = localizationSheetParser.parseSheet(
            sheet = sheet,
            languageColumnTitles = languageTitles
        )

        languageTitles.forEachParallel { language ->
            val transformedSheet = androidSheetTransformer.transformForLanguage(
                language = language,
                parsedSheet = parsedSheet
            )
            val stringXmlContent = stringXmlGenerator.androidValuesToStringsXml(
                values = transformedSheet,
                addComments = addComments
            )

            val file = getStringsXmlFileOrThrow(
                localizationPath = localizationPath,
                localizationIdentifier = if (language == baseLanguage) null else language
            )
            file.writeText(stringXmlContent)
        }
    }

    suspend fun check(
        sheetId: String,
        serviceAccountCredentialsFile: File,
        languageTitles: List<String>,
        baseLanguage: String,
        localizationPath: File,
        addComments: Boolean
    ) {
        val sheet = driveManager.getSheet(
            serviceAccountCredentialsFile = serviceAccountCredentialsFile,
            sheetId = sheetId
        )
        val parsedSheet = localizationSheetParser.parseSheet(
            sheet = sheet,
            languageColumnTitles = languageTitles
        )

        val localizationsWithDifferences = mutableListOf<String>()

        languageTitles.forEachParallel { language ->
            val transformedSheet = androidSheetTransformer.transformForLanguage(
                language = language,
                parsedSheet = parsedSheet
            )
            val stringXmlContent = stringXmlGenerator.androidValuesToStringsXml(
                values = transformedSheet,
                addComments = addComments
            )

            val file = getStringsXmlFileOrThrow(
                localizationPath = localizationPath,
                localizationIdentifier = if (language == baseLanguage) null else language
            )

            if (stringXmlContent != file.readText()) {
                localizationsWithDifferences.add(language)
            }
        }

        if (localizationsWithDifferences.isNotEmpty()) {
            throw RuntimeException("Localizations are not up-to-date. Found differences in languages: ${localizationsWithDifferences.joinToString()}")
        }
    }

    private fun getStringsXmlFileOrThrow(localizationPath: File, localizationIdentifier: String?): File {
        val valuesDirectory = File(
            localizationPath,
            if (localizationIdentifier == null) "values" else "values-$localizationIdentifier"
        ).also { directory ->
            if (directory.exists().not()) {
                if (directory.mkdirs().not()) {
                    throw IOException("Could not create directory (or parent directories for) ${directory.absolutePath}. This directory was defined as 'localizationPath' to save the strings.xml files in.")
                }
            }
        }
        return File(valuesDirectory, "strings.xml").also { file ->
            if (file.exists().not()) {
                if (file.createNewFile().not()) {
                    throw IOException("Could not create file ${file.absolutePath}. The location of this file was defined as 'localizationPath' to save the strings.xml files in.")
                }
            }
        }
    }


}