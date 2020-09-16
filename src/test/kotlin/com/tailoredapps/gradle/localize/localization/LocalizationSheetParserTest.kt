package com.tailoredapps.gradle.localize.localization

import com.tailoredapps.gradle.localize.drive.DriveManager
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Test


class LocalizationSheetParserTest {

    private lateinit var localizationSheetParser: LocalizationSheetParser

    @Before
    fun setUp() {
        localizationSheetParser = LocalizationSheetParser()
    }

    @Test
    fun lastLanguageDoesNotContainAValue() {
        val sheet = DriveManager.Sheet(
            id = "some-id",
            worksheets = listOf(
                DriveManager.Sheet.WorkSheet(
                    title = "First Worksheet",
                    cells = listOf(
                        listOf(
                            "Identifier iOS",
                            "Identifier Android",
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "German Translation",
                            "English Translation",
                            "French Translation",
                            "And also a comment"
                        ),
                        listOf(
                            null,
                            "key2",
                            "German Translation",
                            "English Translation",
                            "French Translation"
                        ),
                        listOf(
                            null,
                            "key3",
                            "German Translation",
                            "English Translation",
                            "French Translation",
                            null
                        ),
                        listOf(
                            null,
                            "key4",
                            "German Translation",
                            "English Translation",
                            null,
                            "And also a comment"
                        ),
                        listOf(
                            null,
                            "key5",
                            "German Translation",
                            null,
                            "French Translation",
                            "And also a comment"
                        ),
                        listOf(
                            null,
                            "key6",
                            "German Translation",
                            "English Translation",
                            null
                        ),
                        listOf(null, "key7", "German Translation", "English Translation")
                    )
                )
            )
        )
        val parsedSheet = localizationSheetParser.parseSheet(
            sheet = sheet,
            worksheets = null,
            languageColumnTitles = listOf("de", "en", "fr")
        )

        parsedSheet.worksheets.size shouldBeEqualTo 1
        val parsedWorksheet = parsedSheet.worksheets.first()
        parsedWorksheet.title shouldBeEqualTo "First Worksheet"
        parsedWorksheet.entries.size shouldBeEqualTo 7

        parsedWorksheet.entries[0].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key1"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation",
                "en" to "English Translation",
                "fr" to "French Translation"
            )
            entry.comment.shouldNotBeNull()
            entry.comment shouldBeEqualTo "And also a comment"
        }

        parsedWorksheet.entries[1].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key2"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation",
                "en" to "English Translation",
                "fr" to "French Translation"
            )
            entry.comment.shouldBeNull()
        }

        parsedWorksheet.entries[2].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key3"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation",
                "en" to "English Translation",
                "fr" to "French Translation"
            )
            entry.comment.shouldBeNull()
        }

        parsedWorksheet.entries[3].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key4"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation",
                "en" to "English Translation",
                "fr" to null
            )
            entry.comment.shouldNotBeNull()
            entry.comment shouldBeEqualTo "And also a comment"
        }

        parsedWorksheet.entries[4].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key5"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation",
                "en" to null,
                "fr" to  "French Translation"
            )
            entry.comment.shouldNotBeNull()
            entry.comment shouldBeEqualTo "And also a comment"
        }

        parsedWorksheet.entries[5].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key6"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation",
                "en" to "English Translation",
                "fr" to  null
            )
            entry.comment.shouldBeNull()
        }

        parsedWorksheet.entries[6].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key7"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation",
                "en" to "English Translation",
                "fr" to  null
            )
            entry.comment.shouldBeNull()
        }
    }

}