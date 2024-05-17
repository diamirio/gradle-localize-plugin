package com.tailoredapps.gradle.localize.drive

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.CellData
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class DriveManager {
    /**
     * Represents a spreadsheet document
     * @param id The id of the sheet
     * @param worksheets The worksheets which are in this sheet
     */
    data class Sheet(
        val id: String,
        val worksheets: List<WorkSheet>
    ) {
        /**
         * Represents a Worksheet (which is a "sub-sheet" within a sheet)
         *
         * @param title The title of the worksheet
         * @param cells The cells and their string value (outer list are the lines, inner list are the columns within a line)
         */
        data class WorkSheet(
            val title: String,
            val cells: List<List<String?>>
        )
    }

    private val transport = GoogleNetHttpTransport.newTrustedTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()

    /**
     * Returns a [Credential] object from the given [serviceAccountCredentialsFile] which can be used by [Sheets] API
     * for authentication.
     *
     * @param serviceAccountCredentialsFile The [File] in which the service account credentials are stored, which
     * should be used as authorization.
     * @return a [Credential] object to be used at the [Sheets.Builder] class.
     */
    private fun getCredentials(serviceAccountCredentialsFile: File): GoogleCredentials {
        return ServiceAccountCredentials
            .fromStream(FileInputStream(serviceAccountCredentialsFile) as InputStream)
            .createScoped(listOf(SheetsScopes.SPREADSHEETS_READONLY))
    }

    /**
     * @param sheetId The id of the sheet to fetch.
     * @return The [Spreadsheet] for the given [sheetId]
     * @throws SpreadsheetNotFoundException if the sheet with the given [sheetId] could not be found
     * @throws AccessForbiddenException if access to the sheet with the given [sheetId] has been forbidden
     * @throws RuntimeException On any other error
     */
    private suspend fun Sheets.getSpreadsheetResponse(sheetId: String): Spreadsheet {
        return suspendCancellableCoroutine { continuation ->
            try {
                val response =
                    this.spreadsheets()
                        .get(sheetId)
                        .setIncludeGridData(true)
                        .execute()
                continuation.resume(response)
            } catch (throwable: Throwable) {
                val exception =
                    if (throwable is GoogleJsonResponseException) {
                        when (throwable.details.code) {
                            404 -> SpreadsheetNotFoundException(sheetId, throwable)
                            403 -> AccessForbiddenException(sheetId, throwable)
                            else ->
                                RuntimeException(
                                    "Error loading spreadsheet with id $sheetId",
                                    throwable
                                )
                        }
                    } else {
                        RuntimeException("Error loading spreadsheet with id $sheetId", throwable)
                    }
                continuation.resumeWithException(exception)
            }
        }
    }

    /**
     * Fetches a sheet from the Google Sheets API with the given [sheetId] using the authorization of the given
     * [serviceAccountCredentialsFile].
     *
     * @param serviceAccountCredentialsFile The [File] in which the service account credentials are stored, which
     * should be used as authorization.
     * @param sheetId The id of the spreadsheet to fetch
     * @return The fetched [Sheet]
     * @throws SpreadsheetNotFoundException if the sheet with the given [sheetId] could not be found
     * @throws AccessForbiddenException if access to the sheet with the given [sheetId] has been forbidden
     * @throws RuntimeException On any other error
     */
    suspend fun getSheet(serviceAccountCredentialsFile: File, sheetId: String): Sheet {
        val credentialsAdapter = HttpCredentialsAdapter(getCredentials(serviceAccountCredentialsFile))

        val sheetsApi =
            Sheets.Builder(transport, jsonFactory, credentialsAdapter)
                .setApplicationName("gradle localize")
                .build()

        val response = sheetsApi.getSpreadsheetResponse(sheetId)

        val values = response.values

        if (values.isEmpty()) {
            throw IllegalStateException("No data found")
        } else {
            val sheets = response.sheets
            val spreadsheetId = response.spreadsheetId

            val worksheets =
                sheets.map { sheet ->
                    val sheetTitle = sheet.properties.title

                    val cells =
                        sheet.data.first().rowData.mapNotNull { row ->
                            row.values.firstOrNull().let { cells ->
                                if (cells != null) {
                                    val cellsAsListOfCellData = cells as? List<CellData>
                                    cellsAsListOfCellData?.map { it.effectiveValue?.stringValue }
                                        ?: emptyList()
                                } else {
                                    emptyList()
                                }
                            }
                        }

                    Sheet.WorkSheet(
                        title = sheetTitle,
                        cells = cells
                    )
                }

            return Sheet(
                id = spreadsheetId,
                worksheets = worksheets
            )
        }
    }

    /**
     * Indicates that the spreadsheet for the given sheetId has not been found.
     */
    class SpreadsheetNotFoundException(sheetId: String, cause: Throwable? = null) :
        RuntimeException(
            "Spreadsheet '$sheetId' not found. Please make sure that the sheetId is correct. " +
                "It may also be that the sheet needs to be shared with a public link once for the plugin to be able to access the sheet.",
            cause
        )

    /**
     * Indicates that the access to the spreadsheet for the given sheetId has been denied (Error: Forbidden).
     */
    class AccessForbiddenException(sheetId: String, cause: Throwable? = null) : RuntimeException(
        "Access to Spreadsheet '$sheetId' is forbidden. Please make sure that the sheetId is correct, and the given service account (the given credentials file) has access to it. " +
            "It may also be that the sheet needs to be shared with a public link once for the plugin to be able to access the sheet.",
        cause
    )
}
