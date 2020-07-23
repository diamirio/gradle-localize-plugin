package com.tailoredapps.gradle.localize.localization

import com.tailoredapps.gradle.localize.drive.DriveManager
import com.tailoredapps.gradle.localize.localization.LocalizationSheetParser.ParsedSheet

/**
 * A class which can parse worksheets loaded by the [DriveManager] into a [ParsedSheet] which contains the
 * localization values.
 */
class LocalizationSheetParser {

    /**
     * Represents a parsed sheet (similarly to a [DriveManager.Sheet], but parsed for localizations)
     *
     * @param worksheets The list of parsed worksheets (similarly to [DriveManager.Sheet.worksheets], but parsed for localizations)
     */
    data class ParsedSheet(
        val worksheets: List<WorkSheet>
    ) {

        /**
         * Represents a parsed worksheet (similarly to [DriveManager.Sheet.WorkSheet], but parsed for localizations)
         *
         * @param title The title of the worksheet
         * @param entries The localization entries / values within this worksheet.
         */
        data class WorkSheet(
            val title: String,
            val entries: List<LocalizationEntry>
        )

        /**
         * Represents a localization entry.
         *
         * @param identifier A [Map] for [Platform] -> IdentifierString, which contains for each platform the given localization entry has an identifier for, the identifier for the platform.
         * @param values A [Map] for LanguageIdentifier -> LocalizedString, which contains for each language the language the given entry has a translation, the translation.
         * @param comment The optional comment for this localization field, if set.
         */
        data class LocalizationEntry(
            val identifier: Map<Platform, String?>,
            val values: Map<String, String?>,
            val comment: String?
        )
    }

    private companion object {
        /**
         * The localization sheet column title for the `iOS` localization identifier
         */
        const val TITLE_IDENTIFIER_IOS = "Identifier iOS"

        /**
         * The localization sheet column title for the `Android` localization identifier
         */
        const val TITLE_IDENTIFIER_ANDROID = "Identifier Android"

        /**
         * The localization sheet column title for the `Web` localization identifier.
         *
         * Yes, the typo in "Identifier" is known, but also in the original fastlane plugin / sheet example, so never touch a running system :shrug:
         */
        const val TITLE_IDENTIFIER_WEB = "Identifer Web"

        /**
         * The localization sheet column title for the comments.
         */
        const val TITLE_IDENTIFIER_COMMENT = "Kommentar"
    }

    enum class Platform(val identifier: String) {
        iOS(TITLE_IDENTIFIER_IOS),
        Android(TITLE_IDENTIFIER_ANDROID),
        Web(TITLE_IDENTIFIER_WEB)
    }


    /**
     * Parses the given [sheet] for localizations.
     *
     * @param sheet The [DriveManager.Sheet] to parse
     * @param worksheets The list of tabs ("worksheets") to parse / return.
     * If `null`, all tabs will be taken,
     * Otherwise (non-null): all tabs named as one of the items in [tabs] will be parsed and returned
     * @param languageColumnTitles The sheet column titles for the localization to parse
     *
     * @return A [ParsedSheet] object which contains all localization entries found in the given [sheet] for the given
     * [languageColumnTitles].
     */
    fun parseSheet(
        sheet: DriveManager.Sheet,
        worksheets: List<String>?,
        languageColumnTitles: List<String>
    ): ParsedSheet {
        val parsedWorksheets = sheet.worksheets
            .let { allWorksheets ->
                if (worksheets != null) {
                    allWorksheets.filter { worksheets.contains(it.title) }
                } else {
                    allWorksheets
                }
            }
            .map { worksheet ->
                val firstLine = worksheet.cells.firstOrNull()
                    ?: throw IllegalStateException("Worksheet ${worksheet.title} does not contain a header line")

                val indexOfPlatforms = Platform.values()
                    .map { platform -> platform to firstLine.indexOfFirstOrNull(platform.identifier) }
                    .toMap()

                if (indexOfPlatforms.none { (_, index) -> index != null }) {
                    throw IllegalStateException(
                        "Worksheet '${worksheet.title}'s first line (a.k.a. the header line) does not contain a column with any of '${Platform.values()
                            .map { it.identifier }
                            .joinToString()}'. At least a header for one platform must be present."
                    )
                }

                val indexOfLanguages = languageColumnTitles
                    .map { languageIdentifier ->
                        languageIdentifier to firstLine.indexOfFirstOrThrow(
                            worksheet.title,
                            languageIdentifier
                        )
                    }
                    .toMap()

                val indexOfComment =
                    firstLine.indexOfFirst { it == TITLE_IDENTIFIER_COMMENT }
                        .let { if (it == -1) null else it }

                ParsedSheet.WorkSheet(
                    title = worksheet.title,
                    entries = worksheet.cells
                        .asSequence()
                        .drop(1)
                        .filter { it.size > 1 }
                        .map { row ->
                            ParsedSheet.LocalizationEntry(
                                identifier = Platform.values()
                                    .mapNotNull { platform ->
                                        indexOfPlatforms[platform]?.let { index -> platform to row[index] }
                                    }
                                    .toMap(),
                                values = indexOfLanguages
                                    .map { (languageIdentifier, columnIndex) -> languageIdentifier to row[columnIndex] }
                                    .toMap(),
                                comment = indexOfComment?.let { index -> row.getOrNull(index) }
                            )
                        }
                        .filter { it.identifier.any { (_, identifier) -> identifier != null } }
                        .toList()
                )
            }

        return ParsedSheet(
            worksheets = parsedWorksheets
        )
    }


    private fun List<String?>.indexOfFirstOrThrow(worksheetTitle: String, identifier: String): Int {
        val index = this.indexOfFirst { it == identifier }
        if (index == -1) {
            throw IllegalStateException("worksheet $worksheetTitle first line (a.k.a. the header line) does not contain a column with '$identifier'")
        }
        return index
    }

    private fun List<String?>.indexOfFirstOrNull(identifier: String): Int? {
        val index = this.indexOfFirst { it == identifier }
        return if (index == -1) {
            null
        } else {
            index
        }
    }

}