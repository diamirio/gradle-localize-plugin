package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.DEFAULT_BASE_LANGUAGE
import com.tailoredapps.gradle.localize.DEFAULT_LOCALIZATION_PATH
import com.tailoredapps.gradle.localize.LocalizationConfig
import com.tailoredapps.gradle.localize.PLUGIN_CONFIGURATION_EXTENSION_NAME
import java.io.File

class ConfigVerifier {

    /**
     * Verifies the given [config] for any missing / obviously wrong values.
     *
     * This method will complete inf everything is ok, and will throw a [LocalizeConfigurationException] if a config error has been found.
     */
    fun checkConfiguration(projectPath: File, config: LocalizationConfig) {
        if (config.serviceAccountCredentialsFile == projectPath) {
            throw LocalizeConfigurationException(
                "'serviceAccountCredentialsFile' not set. This needs to be set to the path of the credentials file where the service account credentials are stored, either in the base configuration or in the product dependent configuration.\n" +
                        getServiceAccountCredentialsFileExample()
            )
        }

        if (config.serviceAccountCredentialsFile.exists().not()) {
            throw LocalizeConfigurationException(
                "${config.serviceAccountCredentialsFile.absolutePath} not found (defined as 'serviceAccountCredentialsFile': \"${config.serviceAccountCredentialsFile}\"). This needs to be set to the path of the credentials file where the service account credentials are stored.\n" +
                        getServiceAccountCredentialsFileExample()
            )
        }

        if (config.serviceAccountCredentialsFile.canRead().not()) {
            throw LocalizeConfigurationException(
                "${config.serviceAccountCredentialsFile.absolutePath} cannot be read (defined as 'serviceAccountCredentialsFile': \"${config.serviceAccountCredentialsFile}\"). This needs to be set to the path of the credentials file where the service account credentials are stored.\n" +
                        getServiceAccountCredentialsFileExample()
            )
        }

        if (config.sheetId.isBlank()) {
            throw LocalizeConfigurationException(
                "'sheetId' not set. This needs to be set to the id of the google spreadsheet in which the localization strings are entered.\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    configuration {\n" +
                        "        main { //the name of your product configuration\n" +
                        "            sheetId = \"1fwRj1ZFPu2XlrDqkaqmIpJulqR5OVFEZnN35a9v37yc\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
            )
        }

        if (config.languageTitles.isEmpty()) {
            throw LocalizeConfigurationException(
                "'languageTitles' not set (or set to an empty array). This needs to be set to the column header of the languages you want to import.\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    configuration {\n" +
                        "        main { //the name of your product configuration\n" +
                        "            languageTitles = [\"en\", \"de\", \"ru\"]\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
            )
        }

        if (config.baseLanguage.isBlank()) {
            throw LocalizeConfigurationException(
                "'baseLanguage' is set to an invalid value. Default is \"$DEFAULT_BASE_LANGUAGE\". Needs to be set to a value present in 'languageTitles'. This represents the language which is placed as a default language in the localizations (so as 'values/strings.xml' resource in Android, which is the fallback language if a language is not supported).\n" +
                        getBaseLanguageExample()
            )
        }

        if (config.languageTitles.none { it == config.baseLanguage }) {
            throw LocalizeConfigurationException(
                "'baseLanguage' is set to a value not present in 'languageTitles'. Needs to be set to a value present in 'languageTitles' (which is currently set to ${config.languageTitles.joinToString()}, so any of those values is valid). This represents the language which is placed as a default language in the localizations (so as 'values/strings.xml' resource in Android, which is the fallback language if a language is not supported).\n" +
                        getBaseLanguageExample()
            )
        }

        if (config.localizationPath == projectPath) {
            throw LocalizeConfigurationException(
                "'localizationPath' is set to an invalid value. Default is \"$DEFAULT_LOCALIZATION_PATH\". This is needed as a local path where to save the string resource xml files to. Normally this only needs to be changed if you need to place the string xml files in another module than the default app module.\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    configuration {\n" +
                        "        main { //the name of your product configuration\n" +
                        "            localizationPath = \"$DEFAULT_LOCALIZATION_PATH\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
            )
        }

    }


    private fun getServiceAccountCredentialsFileExample(): String {
        return "Example (when defined in the base configuration for all products):\n" +
                "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                "    serviceAccountCredentialsFile = \"./google_drive_credentials.json\"\n" +
                "}\n\n" +
                "Example (when defined for a specific product only):\n" +
                "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                "    configuration {\n" +
                "        main { //the name of your product configuration\n" +
                "            serviceAccountCredentialsFile = \"./google_drive_credentials.json\"\n" +
                "        }\n" +
                "    }\n" +
                "}"
    }

    private fun getBaseLanguageExample(): String {
        return "Example (when defined in the base configuration for all products):\n" +
                "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                "    baseLanguage = \"$DEFAULT_BASE_LANGUAGE\"\n" +
                "}\n\n" +
                "Example (when defined for a specific product only):\n" +
                "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                "    configuration {\n" +
                "        main { //the name of your product configuration\n" +
                "            baseLanguage = \"$DEFAULT_BASE_LANGUAGE\"\n" +
                "        }\n" +
                "    }\n" +
                "}"
    }

}

class LocalizeConfigurationException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(
        "$message\nCheck out the README on how to configure the localize plugin.",
        cause
    )