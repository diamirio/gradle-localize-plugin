package com.tailoredapps.gradle.localize.localization

import com.tailoredapps.gradle.localize.drive.DriveManager
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.shouldThrow
import org.junit.Before
import org.junit.Test


class LocalizationSheetParserTest {

    private lateinit var localizationSheetParser: LocalizationSheetParser

    @Before
    fun setUp() {
        localizationSheetParser = LocalizationSheetParser()
    }

    @Test
    fun `last language does not contain a value`() {
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
                "fr" to "French Translation"
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
                "fr" to null
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
                "fr" to null
            )
            entry.comment.shouldBeNull()
        }
    }

    @Test
    fun `parsing web identifier with legacy Identifer Web`() {
        val sheet = DriveManager.Sheet(
            id = "some-id",
            worksheets = listOf(
                DriveManager.Sheet.WorkSheet(
                    title = "First Worksheet",
                    cells = listOf(
                        listOf(
                            "Identifier iOS",
                            "Identifier Android",
                            "Identifer Web",
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation",
                            "English Translation",
                            "French Translation",
                            "And also a comment"
                        ),
                        listOf(
                            null,
                            "key2",
                            "key2_web",
                            "German Translation",
                            "English Translation",
                            "French Translation"
                        )
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
        parsedWorksheet.entries.size shouldBeEqualTo 2

        parsedWorksheet.entries[0].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key1",
                LocalizationSheetParser.Platform.Web to "key1_web"
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
                LocalizationSheetParser.Platform.Android to "key2",
                LocalizationSheetParser.Platform.Web to "key2_web"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation",
                "en" to "English Translation",
                "fr" to "French Translation"
            )
            entry.comment.shouldBeNull()
        }
    }

    @Test
    fun `parsing web identifier with fixed Identifier Web`() {
        val sheet = DriveManager.Sheet(
            id = "some-id",
            worksheets = listOf(
                DriveManager.Sheet.WorkSheet(
                    title = "First Worksheet",
                    cells = listOf(
                        listOf(
                            "Identifier iOS",
                            "Identifier Android",
                            "Identifier Web",
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation",
                            "English Translation",
                            "French Translation",
                            "And also a comment"
                        ),
                        listOf(
                            null,
                            "key2",
                            "key2_web",
                            "German Translation",
                            "English Translation",
                            "French Translation"
                        )
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
        parsedWorksheet.entries.size shouldBeEqualTo 2

        parsedWorksheet.entries[0].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key1",
                LocalizationSheetParser.Platform.Web to "key1_web"
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
                LocalizationSheetParser.Platform.Android to "key2",
                LocalizationSheetParser.Platform.Web to "key2_web"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation",
                "en" to "English Translation",
                "fr" to "French Translation"
            )
            entry.comment.shouldBeNull()
        }
    }

    @Test
    fun `parsing fails if no platform identifier titles found case 1`() {
        val sheet = DriveManager.Sheet(
            id = "some-id",
            worksheets = listOf(
                DriveManager.Sheet.WorkSheet(
                    title = "First Worksheet",
                    cells = listOf(
                        listOf(
                            null,
                            null,
                            null,
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation",
                            "English Translation",
                            "French Translation",
                            "And also a comment"
                        )
                    )
                )
            )
        );
        val throwResult = {
            localizationSheetParser.parseSheet(
                sheet = sheet,
                worksheets = null,
                languageColumnTitles = listOf("de", "en", "fr")
            )
        }.shouldThrow(IllegalStateException::class)

        throwResult.exceptionMessage shouldBeEqualTo "Worksheet 'First Worksheet's first line (a.k.a. the header line) does not contain a column with any of 'Identifier Android','Identifier iOS','Identifier Web' (or for legacy reasons also 'Identifer Web'). At least a header for one platform must be present."
    }

    @Test
    fun `parsing fails if no platform identifier titles found case 2`() {
        val sheet = DriveManager.Sheet(
            id = "some-id",
            worksheets = listOf(
                DriveManager.Sheet.WorkSheet(
                    title = "First Worksheet",
                    cells = listOf(
                        listOf(
                            "ios",
                            "android",
                            "web",
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation",
                            "English Translation",
                            "French Translation",
                            "And also a comment"
                        )
                    )
                )
            )
        );
        val throwResult = {
            localizationSheetParser.parseSheet(
                sheet = sheet,
                worksheets = null,
                languageColumnTitles = listOf("de", "en", "fr")
            )
        }.shouldThrow(IllegalStateException::class)

        throwResult.exceptionMessage shouldBeEqualTo "Worksheet 'First Worksheet's first line (a.k.a. the header line) does not contain a column with any of 'Identifier Android','Identifier iOS','Identifier Web' (or for legacy reasons also 'Identifer Web'). At least a header for one platform must be present."
    }

    @Test
    fun `parsing fails if no line could be found`() {
        val sheet = DriveManager.Sheet(
            id = "some-id",
            worksheets = listOf(
                DriveManager.Sheet.WorkSheet(
                    title = "First Worksheet",
                    cells = emptyList()
                )
            )
        );
        val throwResult = {
            localizationSheetParser.parseSheet(
                sheet = sheet,
                worksheets = null,
                languageColumnTitles = listOf("de", "en", "fr")
            )
        }.shouldThrow(IllegalStateException::class)

        throwResult.exceptionMessage shouldBeEqualTo "Worksheet 'First Worksheet' does not contain a header line"
    }

    @Test
    fun `parsing multiple worksheets will only parse the given worksheet titles case 1`() {
        val sheet = DriveManager.Sheet(
            id = "some-id",
            worksheets = listOf(
                DriveManager.Sheet.WorkSheet(
                    title = "First Worksheet",
                    cells = listOf(
                        listOf(
                            "Identifier iOS",
                            "Identifier Android",
                            "Identifier Web",
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation First Sheet",
                            "English Translation First Sheet",
                            "French Translation First Sheet",
                            "And also a comment First Sheet"
                        )
                    )
                ),
                DriveManager.Sheet.WorkSheet(
                    title = "Second Worksheet",
                    cells = listOf(
                        listOf(
                            "Identifier iOS",
                            "Identifier Android",
                            "Identifier Web",
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation Second Sheet",
                            "English Translation Second Sheet",
                            "French Translation Second Sheet",
                            "And also a comment Second Sheet"
                        )
                    )
                ),
                DriveManager.Sheet.WorkSheet(
                    title = "Third Worksheet",
                    cells = listOf(
                        listOf(
                            "Identifier iOS",
                            "Identifier Android",
                            "Identifier Web",
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation Third Sheet",
                            "English Translation Third Sheet",
                            "French Translation Third Sheet",
                            "And also a comment Third Sheet"
                        )
                    )
                )
            )
        )

        val parsedSheet = localizationSheetParser.parseSheet(
            sheet = sheet,
            worksheets = listOf("Second Worksheet"),
            languageColumnTitles = listOf("de", "en", "fr")
        )

        parsedSheet.worksheets.size shouldBeEqualTo 1
        val parsedWorksheet = parsedSheet.worksheets.first()
        parsedWorksheet.title shouldBeEqualTo "Second Worksheet"
        parsedWorksheet.entries.size shouldBeEqualTo 1

        parsedWorksheet.entries[0].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key1",
                LocalizationSheetParser.Platform.Web to "key1_web"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation Second Sheet",
                "en" to "English Translation Second Sheet",
                "fr" to "French Translation Second Sheet"
            )
            entry.comment.shouldNotBeNull()
            entry.comment shouldBeEqualTo "And also a comment Second Sheet"
        }
    }

    @Test
    fun `parsing multiple worksheets will only parse the given worksheet titles case 2`() {
        val sheet = DriveManager.Sheet(
            id = "some-id",
            worksheets = listOf(
                DriveManager.Sheet.WorkSheet(
                    title = "First Worksheet",
                    cells = listOf(
                        listOf(
                            "Identifier iOS",
                            "Identifier Android",
                            "Identifier Web",
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation First Sheet",
                            "English Translation First Sheet",
                            "French Translation First Sheet",
                            "And also a comment First Sheet"
                        )
                    )
                ),
                DriveManager.Sheet.WorkSheet(
                    title = "Second Worksheet",
                    cells = listOf(
                        listOf(
                            "Identifier iOS",
                            "Identifier Android",
                            "Identifier Web",
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation Second Sheet",
                            "English Translation Second Sheet",
                            "French Translation Second Sheet",
                            "And also a comment Second Sheet"
                        )
                    )
                ),
                DriveManager.Sheet.WorkSheet(
                    title = "Third Worksheet",
                    cells = listOf(
                        listOf(
                            "Identifier iOS",
                            "Identifier Android",
                            "Identifier Web",
                            "de",
                            "en",
                            "fr",
                            "Kommentar"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation Third Sheet",
                            "English Translation Third Sheet",
                            "French Translation Third Sheet",
                            "And also a comment Third Sheet"
                        )
                    )
                )
            )
        )

        val parsedSheet = localizationSheetParser.parseSheet(
            sheet = sheet,
            worksheets = listOf("First Worksheet", "Third Worksheet"),
            languageColumnTitles = listOf("de", "en", "fr")
        )

        parsedSheet.worksheets.size shouldBeEqualTo 2

        val parsedWorksheet1 = parsedSheet.worksheets.first()
        parsedWorksheet1.title shouldBeEqualTo "First Worksheet"
        parsedWorksheet1.entries.size shouldBeEqualTo 1

        parsedWorksheet1.entries[0].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key1",
                LocalizationSheetParser.Platform.Web to "key1_web"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation First Sheet",
                "en" to "English Translation First Sheet",
                "fr" to "French Translation First Sheet"
            )
            entry.comment.shouldNotBeNull()
            entry.comment shouldBeEqualTo "And also a comment First Sheet"
        }

        val parsedWorksheet2 = parsedSheet.worksheets[1]
        parsedWorksheet2.title shouldBeEqualTo "Third Worksheet"
        parsedWorksheet2.entries.size shouldBeEqualTo 1

        parsedWorksheet2.entries[0].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key1",
                LocalizationSheetParser.Platform.Web to "key1_web"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation Third Sheet",
                "en" to "English Translation Third Sheet",
                "fr" to "French Translation Third Sheet"
            )
            entry.comment.shouldNotBeNull()
            entry.comment shouldBeEqualTo "And also a comment Third Sheet"
        }
    }

    @Test
    fun `parsing web identifier with en comment identifier`() {
        val sheet = DriveManager.Sheet(
            id = "some-id",
            worksheets = listOf(
                DriveManager.Sheet.WorkSheet(
                    title = "First Worksheet",
                    cells = listOf(
                        listOf(
                            "Identifier iOS",
                            "Identifier Android",
                            "Identifier Web",
                            "de",
                            "en",
                            "fr",
                            "Comment"
                        ),
                        listOf("//This is a comment in the worksheet"),
                        listOf(
                            null,
                            "key1",
                            "key1_web",
                            "German Translation",
                            "English Translation",
                            "French Translation",
                            "And also a comment"
                        ),
                        listOf(
                            null,
                            "key2",
                            "key2_web",
                            "German Translation",
                            "English Translation",
                            "French Translation"
                        )
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
        parsedWorksheet.entries.size shouldBeEqualTo 2

        parsedWorksheet.entries[0].also { entry ->
            entry.identifier shouldBeEqualTo mapOf(
                LocalizationSheetParser.Platform.iOS to null,
                LocalizationSheetParser.Platform.Android to "key1",
                LocalizationSheetParser.Platform.Web to "key1_web"
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
                LocalizationSheetParser.Platform.Android to "key2",
                LocalizationSheetParser.Platform.Web to "key2_web"
            )
            entry.values shouldBeEqualTo mapOf(
                "de" to "German Translation",
                "en" to "English Translation",
                "fr" to "French Translation"
            )
            entry.comment.shouldBeNull()
        }
    }

}