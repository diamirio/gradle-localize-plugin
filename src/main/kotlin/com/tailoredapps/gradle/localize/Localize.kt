package com.tailoredapps.gradle.localize

import com.tailoredapps.gradle.localize.android.AndroidStringXmlGenerator
import com.tailoredapps.gradle.localize.android.ParsedSheetToAndroidTransformer
import com.tailoredapps.gradle.localize.drive.DriveManager
import com.tailoredapps.gradle.localize.localization.LocalizationSheetParser
import com.tailoredapps.gradle.localize.util.forEachParallel
import difflib.DiffUtils
import java.io.File
import java.io.IOException

class Localize {

    private val driveManager: DriveManager = DriveManager()
    private val localizationSheetParser: LocalizationSheetParser = LocalizationSheetParser()
    private val androidSheetTransformer: ParsedSheetToAndroidTransformer =
        ParsedSheetToAndroidTransformer()
    private val stringXmlGenerator: AndroidStringXmlGenerator = AndroidStringXmlGenerator()

    suspend fun localize(config: LocalizationConfig) {
        val sheet = driveManager.getSheet(
            serviceAccountCredentialsFile = config.serviceAccountCredentialsFile,
            sheetId = config.sheetId
        )
        val parsedSheet = localizationSheetParser.parseSheet(
            sheet = sheet,
            worksheets = config.worksheets,
            languageColumnTitles = config.languageTitles
        )

        config.languageTitles.forEachParallel { language ->
            val transformedSheet = androidSheetTransformer.transformForLanguage(
                language = language,
                parsedSheet = parsedSheet
            )
            val stringXmlContent = stringXmlGenerator.androidValuesToStringsXml(
                values = transformedSheet,
                addComments = config.addComments
            )

            val file = getStringsXmlFileOrThrow(
                localizationPath = config.localizationPath,
                localizationIdentifier = if (language == config.baseLanguage) null else language
            )
            file.writeText(stringXmlContent)
        }
    }

    suspend fun check(config: LocalizationConfig) {
        val sheet = driveManager.getSheet(
            serviceAccountCredentialsFile = config.serviceAccountCredentialsFile,
            sheetId = config.sheetId
        )
        val parsedSheet = localizationSheetParser.parseSheet(
            sheet = sheet,
            worksheets = config.worksheets,
            languageColumnTitles = config.languageTitles
        )

        val diffs = mutableListOf<String>()

        config.languageTitles
            .map { language ->
                val transformedSheet = androidSheetTransformer.transformForLanguage(
                    language = language,
                    parsedSheet = parsedSheet
                )
                val stringXmlContent = stringXmlGenerator.androidValuesToStringsXml(
                    values = transformedSheet,
                    addComments = config.addComments
                )

                val file = getStringsXmlFileOrThrow(
                    localizationPath = config.localizationPath,
                    localizationIdentifier = if (language == config.baseLanguage) null else language
                )


                val diffResult = compareFiles(
                    expectedFileName = config.localizationPath.canonicalPath + " (remote)",
                    expectedFileContent = stringXmlContent,
                    actualFileName = config.localizationPath.canonicalPath + " (local)",
                    actualFileContent = file.readText()
                )


                if (diffResult != null) {
                    diffs.add(diffResult)
                }
            }

        if (diffs.isNotEmpty()) {
            throw RuntimeException(
                "Localizations are not up-to-date.\n\n${diffs.joinToString(
                    separator = "\n"
                )}"
            )
        }
    }


    private fun compareFiles(
        expectedFileName: String,
        expectedFileContent: String,
        actualFileName: String,
        actualFileContent: String
    ): String? {
        // We don't compare full text because newlines on Windows & Linux/macOS are different
        val checkLines = expectedFileContent.lines()
        val actualLines = actualFileContent.lines()
        if (checkLines == actualLines)
            return null

        val patch = DiffUtils.diff(checkLines, actualLines)
        val diff =
            DiffUtils.generateUnifiedDiff(expectedFileName, actualFileName, checkLines, patch, 3)
        return diff.joinToString("\n")
    }

    private fun getStringsXmlFileOrThrow(
        localizationPath: File,
        localizationIdentifier: String?
    ): File {
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