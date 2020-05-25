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
     * @param values The [ParsedSheetToAndroidTransformer.AndroidValue]s to put into the `strings.xml` file content.
     * @return The content for a `strings.xml` file for the given [values].
     */
    suspend fun androidValuesToStringsXml(values: List<ParsedSheetToAndroidTransformer.AndroidValue>): String {
        return suspendCancellableCoroutine<String> { continuation ->
            val string = StringBuilder().apply {
                append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                append("<resources>\n")
                values.forEach { androidValue ->
                    if (continuation.isCancelled) {
                        return@forEach
                    }
                    when (androidValue) {
                        is ParsedSheetToAndroidTransformer.AndroidValue.Blank -> {
                            appendCommentIfPresent(androidValue.comment)
                            append("$INDENT<string name=\"${androidValue.identifier}\"></string>\n")
                        }
                        is ParsedSheetToAndroidTransformer.AndroidValue.Plain -> {
                            appendCommentIfPresent(androidValue.comment)
                            append("$INDENT<string name=\"${androidValue.identifier}\">")
                            append(androidValue.value.escapeSingleApostrophes().wrapInCData())
                            append("</string>\n")
                        }
                        is ParsedSheetToAndroidTransformer.AndroidValue.Array -> {
                            appendCommentIfPresent(androidValue.comment)
                            append("$INDENT<string-array name=\"${androidValue.identifier}\">\n")
                            androidValue.values.forEach { value ->
                                append("$INDENT$INDENT<item>")
                                append(value.escapeSingleApostrophes().wrapInCData())
                                append("</item>\n")
                            }
                            append("$INDENT</string-array>\n")
                        }
                        is ParsedSheetToAndroidTransformer.AndroidValue.Plural -> {
                            appendCommentIfPresent(androidValue.comment)
                            append("$INDENT<plurals name=\"${androidValue.identifier}\">\n")
                            androidValue.entries.forEach { (quantity, value) ->
                                append("$INDENT$INDENT<item quantity=\"$quantity\">")
                                append(value.escapeSingleApostrophes().wrapInCData())
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

    private fun StringBuilder.appendCommentIfPresent(comment: String?) {
        if (comment != null) {
            append("$INDENT<!-- $comment -->\n")
        }
    }

    private fun String.escapeSingleApostrophes(): String = this.replace("'", "\\'")
    private fun String.wrapInCData(): String = "<![CDATA[$this]]>"
}