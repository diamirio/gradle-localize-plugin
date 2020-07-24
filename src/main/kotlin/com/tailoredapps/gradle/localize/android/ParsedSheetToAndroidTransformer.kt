package com.tailoredapps.gradle.localize.android

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tailoredapps.gradle.localize.localization.LocalizationSheetParser

class ParsedSheetToAndroidTransformer {

    sealed class AndroidValue {

        abstract val identifier: String
        abstract val comment: String?

        data class Plain(
            override val identifier: String,
            val value: String,
            override val comment: String?
        ) : AndroidValue()

        data class Array(
            override val identifier: String,
            val values: List<String>,
            override val comment: String?
        ) : AndroidValue()

        data class Plural(
            override val identifier: String,
            val entries: List<Pair<String, String>>,
            override val comment: String?
        ) : AndroidValue()

        data class Blank(
            override val identifier: String,
            override val comment: String?
        ) : AndroidValue()

    }

    private companion object {
        private val quantityKeywords = listOf("zero", "one", "two", "few", "many", "other")
        private val quantityPrefixes = quantityKeywords.map { "$it|" }
    }

    /**
     * Transforms the given [parsedSheet] for the given [language] into a list of [AndroidValue]s, which
     * then can be output to e.g. a `strings.xml` file.
     *
     * @param language The language to extract the [AndroidValue] for.
     * @param parsedSheet The parsed localization sheet to extract the [AndroidValue]s from.
     * @return a [List] of [AndroidValue]s which contain all translations for the given [language].
     */
    fun transformForLanguage(
        language: String,
        parsedSheet: LocalizationSheetParser.ParsedSheet
    ): List<AndroidValue> {
        return parsedSheet.worksheets.flatMap { worksheet ->
            worksheet.entries
                .asSequence()
                .filter { it.identifier[LocalizationSheetParser.Platform.Android] != null }
                .map { entry -> parseToAndroidValue(entry, language) }
                .toList()
        }
    }

    private fun String.isPluralDefinition(): Boolean {
        return this.split("\n").any { line -> quantityPrefixes.any { prefix -> line.startsWith(prefix) } }
    }


    private fun parseToAndroidValue(
        entry: LocalizationSheetParser.ParsedSheet.LocalizationEntry,
        language: String
    ): AndroidValue {
        val identifier = requireNotNull(entry.identifier[LocalizationSheetParser.Platform.Android]) {
            "entry.identifier[${LocalizationSheetParser.Platform.Android}]"
        }
        val value = entry.values[language]
        return if (value != null) {
            when {
                //check whether plural:
                value.isPluralDefinition() -> {
                    AndroidValue.Plural(
                        identifier = identifier,
                        entries = value.split("\n")
                            .mapNotNull { pluralLine ->
                                pluralLine.split("|", limit = 2)
                                    .takeIf { it.size == 2 }
                            }
                            .map { (quantity, value) ->
                                if (quantity !in quantityKeywords) {
                                    throw IllegalArgumentException("Invalid plural quantity keyword detected for $identifier: $quantity. Valid quantity keywords are: $quantityKeywords")
                                }
                                quantity to value
                            },
                        comment = entry.comment
                    )
                }

                //check whether array:
                value.startsWith("[\"") && value.endsWith("\"]") -> {
                    AndroidValue.Array(
                        identifier = identifier,
                        values = Gson().fromJson<List<String>>(
                            value,
                            object : TypeToken<List<String>>() {}.type
                        ),
                        comment = entry.comment
                    )
                }

                else -> {
                    AndroidValue.Plain(
                        identifier = identifier,
                        value = value,
                        comment = entry.comment
                    )
                }
            }
        } else {
            AndroidValue.Blank(
                identifier = identifier,
                comment = entry.comment
            )
        }
    }

}