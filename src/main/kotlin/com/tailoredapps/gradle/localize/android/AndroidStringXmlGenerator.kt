package com.tailoredapps.gradle.localize.android

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidStringXmlGenerator {

    private companion object {
        private const val INDENT = "    "
    }

    /**
     * Generates the contents for a `strings.xml` file with all given [values].
     *
     * @param values The [AndroidValues][ParsedSheetToAndroidTransformer.AndroidValue] to put into the `strings.xml`
     * file content.
     * @param addComments Whether the comments of the [AndroidValue][ParsedSheetToAndroidTransformer.AndroidValue] (if
     * present) should be added as XML comments to the file as well.
     * @param escapeApostrophes Whether apostrophes in the [values] should be escaped. The default
     * option should be true here, but for legacy reasons when migrating from the fastlane plugin,
     * the apostrophes may already have been escaped in the spreadsheet.
     * @param generateEmptyValues Whether empty [values] should be put into the `strings.xml`. Defaults
     * to true. Can be useful to allow a fallback to the default language if there is no value in a column.
     * @return The content for a `strings.xml` file for the given [values].
     */
    suspend fun androidValuesToStringsXml(
        values: List<ParsedSheetToAndroidTransformer.AndroidValue>,
        addComments: Boolean,
        escapeApostrophes: Boolean = true,
        generateEmptyValues: Boolean = true,
    ): String {
        return suspendCancellableCoroutine { continuation ->
            val string = StringBuilder().apply {
                append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                append("<resources>\n")
                values.forEach { androidValue ->
                    if (continuation.isCancelled) {
                        return@forEach
                    }
                    when (androidValue) {
                        is ParsedSheetToAndroidTransformer.AndroidValue.Blank -> {
                            if(generateEmptyValues) {
                                appendCommentIfPresent(
                                    androidValue.comment,
                                    addComments = addComments
                                )
                                append("$INDENT<string name=\"${androidValue.identifier}\"></string>\n")
                            }
                        }
                        is ParsedSheetToAndroidTransformer.AndroidValue.Plain -> {
                            appendCommentIfPresent(androidValue.comment, addComments = addComments)
                            append("$INDENT<string name=\"${androidValue.identifier}\">")
                            append(
                                androidValue.value
                                    .escapeApostrophes(escapeApostrophes)
                                    .escapeLineBreaks()
                                    .wrapInCData()
                            )
                            append("</string>\n")
                        }
                        is ParsedSheetToAndroidTransformer.AndroidValue.Array -> {
                            appendCommentIfPresent(androidValue.comment, addComments = addComments)
                            append("$INDENT<string-array name=\"${androidValue.identifier}\">\n")
                            androidValue.values.forEach { value ->
                                append("$INDENT$INDENT<item>")
                                append(
                                    value
                                        .escapeApostrophes(escapeApostrophes)
                                        .escapeLineBreaks()
                                        .wrapInCData()
                                )
                                append("</item>\n")
                            }
                            append("$INDENT</string-array>\n")
                        }
                        is ParsedSheetToAndroidTransformer.AndroidValue.Plural -> {
                            appendCommentIfPresent(androidValue.comment, addComments = addComments)
                            append("$INDENT<plurals name=\"${androidValue.identifier}\">\n")
                            androidValue.entries.forEach { (quantity, value) ->
                                append("$INDENT$INDENT<item quantity=\"$quantity\">")
                                append(
                                    value
                                        .escapeApostrophes(escapeApostrophes)
                                        .escapeLineBreaks()
                                        .wrapInCData()
                                )
                                append("</item>\n")
                            }
                            append("$INDENT</plurals>\n")
                        }
                    }
                }
                append("</resources>\n")
            }.toString()
            continuation.resume(string)
        }
    }

    private fun StringBuilder.appendCommentIfPresent(comment: String?, addComments: Boolean) {
        if (addComments && comment != null) {
            append("$INDENT<!-- $comment -->\n")
        }
    }

    private fun String.escapeApostrophes(escapeApostrophes: Boolean): String {
        return if (escapeApostrophes) {
            this.replace("'", "\\'")
        } else {
            this
        }
    }

    private fun String.escapeLineBreaks(): String = this.replace("\n", "\\n")

    private fun String.wrapInCData(): String = "<![CDATA[$this]]>"
}