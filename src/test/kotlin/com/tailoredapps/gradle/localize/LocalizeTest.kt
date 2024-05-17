package com.tailoredapps.gradle.localize

import com.tailoredapps.gradle.localize.localization.LocalizationSheetParser
import org.junit.Test

class LocalizeTest {

    private val localize = Localize()

    @Test(expected = IllegalStateException::class)
    fun `duplicate key check throws exception`() {
        with(localize) {
            getParsedSheet("key1", "key1", "key2")
                .abortLocalizationOnKeyDuplicates()
        }
    }

    @Test
    fun `duplicate key check is successful`() {
        with(localize) {
            getParsedSheet("key1", "key2", "key3")
                .abortLocalizationOnKeyDuplicates()
        }
    }

    private fun getParsedSheet(vararg keys: String) = LocalizationSheetParser.ParsedSheet(
        worksheets = listOf(
            LocalizationSheetParser.ParsedSheet.WorkSheet(
                title = "sheet 1",
                entries = keys.map { key ->
                    LocalizationSheetParser.ParsedSheet.LocalizationEntry(
                        identifier = mapOf(
                            LocalizationSheetParser.Platform.Android to key
                        ),
                        values = mapOf(
                            "de" to "translation",
                            "en" to "translation"
                        ),
                        comment = null
                    )
                }
            ),
            LocalizationSheetParser.ParsedSheet.WorkSheet(
                title = "sheet 2",
                entries = keys.map { key ->
                    LocalizationSheetParser.ParsedSheet.LocalizationEntry(
                        identifier = mapOf(
                            LocalizationSheetParser.Platform.Android to key
                        ),
                        values = mapOf(
                            "de" to "translation",
                            "en" to "translation"
                        ),
                        comment = null
                    )
                }
            )
        )
    )
}
