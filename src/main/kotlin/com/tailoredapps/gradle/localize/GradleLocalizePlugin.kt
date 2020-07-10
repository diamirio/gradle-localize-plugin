package com.tailoredapps.gradle.localize

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.dsl.ProductFlavor
import com.tailoredapps.gradle.localize.extension.BaseLocalizeExtension
import com.tailoredapps.gradle.localize.extension.ConfigProvider
import com.tailoredapps.gradle.localize.extension.ExtensionMerger
import com.tailoredapps.gradle.localize.extension.FlavorLocalizeExtension
import com.tailoredapps.gradle.localize.util.PathToFileManager
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

internal const val PLUGIN_CONFIGURATION_EXTENSION_NAME = "localizeConfig"
internal const val DEFAULT_LOCALIZATION_PATH = "./src/main/res"
internal const val DEFAULT_BASE_LANGUAGE = "en"

internal const val PLUGIN_TASK_GROUP_NAME = "localization"

//created using this tutorial: https://dzone.com/articles/the-complete-custom-gradle-plugin-building-tutoria
class GradleLocalizePlugin : Plugin<Project> {

    private val localize: Localize by lazy { Localize() }
    private lateinit var extensionMerger: ExtensionMerger
    private lateinit var pathToFileManager: PathToFileManager
    private lateinit var configProvider: ConfigProvider

    override fun apply(project: Project) {
        pathToFileManager = PathToFileManager(project)
        extensionMerger = ExtensionMerger(pathToFileManager = pathToFileManager)
        configProvider = ConfigProvider(extensionMerger = extensionMerger)


        val extension =
            project.extensions.create(PLUGIN_CONFIGURATION_EXTENSION_NAME, BaseLocalizeExtension::class.java)
        extension.flavorConfigContainer = project.container(FlavorLocalizeExtension::class.java)

        project.tasks.create("localize") { task ->
            task.doLast {
                runTask(project, extension) { config ->
                    localize.localize(config)
                }
            }
        }.apply {
            description = "Imports strings from a localization sheet"
            group = PLUGIN_TASK_GROUP_NAME
        }
        val checkLocalization = project.tasks.create("checkLocalization") { task ->
            task.doLast {
                runTask(project, extension) { config ->
                    localize.check(config)
                }
            }
        }.apply {
            description = "Checks whether the local localizations are up-to-date."
            group = PLUGIN_TASK_GROUP_NAME
        }

        if (extension.addToCheckTask) {
            project.tasks.getByName("check").dependsOn(checkLocalization)
        }

    }

    private fun checkConfiguration(projectPath: File, config: LocalizationConfig) {
        if (config.serviceAccountCredentialsFile == projectPath) {
            throw LocalizeConfigurationException(
                "'serviceAccountCredentialsFile' not set. This needs to be set to the path of the credentials file where the service account credentials are stored.\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    serviceAccountCredentialsFile = \"./google_drive_credentials.json\"\n" +
                        "}"
            )
        }

        if (config.serviceAccountCredentialsFile.exists().not()) {
            throw LocalizeConfigurationException(
                "${config.serviceAccountCredentialsFile.absolutePath} not found (defined as 'serviceAccountCredentialsFile': \"${config.serviceAccountCredentialsFile}\"). This needs to be set to the path of the credentials file where the service account credentials are stored.\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    serviceAccountCredentialsFile = \"./google_drive_credentials.json\"\n" +
                        "}"
            )
        }
        if (config.serviceAccountCredentialsFile.canRead().not()) {
            throw LocalizeConfigurationException(
                "${config.serviceAccountCredentialsFile.absolutePath} cannot be read (defined as 'serviceAccountCredentialsFile': \"${config.serviceAccountCredentialsFile}\"). This needs to be set to the path of the credentials file where the service account credentials are stored.\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    serviceAccountCredentialsFile = \"./google_drive_credentials.json\"\n" +
                        "}"
            )
        }

        if (config.sheetId.isBlank()) {
            throw LocalizeConfigurationException(
                "'sheetId' not set. This needs to be set to the id of the google spreadsheet in which the localization strings are entered.\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    sheetId = \"1fwRj1ZFPu2XlrDqkaqmIpJulqR5OVFEZnN35a9v37yc\"\n" +
                        "}"
            )
        }

        if (config.languageTitles.isEmpty()) {
            throw LocalizeConfigurationException(
                "'languageTitles' not set (or set to an empty array). This needs to be set to the column header of the languages you want to import.\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    languageTitles = [\"en\", \"de\", \"ru\"]\n" +
                        "}"
            )
        }

        if (config.baseLanguage.isBlank()) {
            throw LocalizeConfigurationException(
                "'baseLanguage' is set to an invalid value. Default is \"$DEFAULT_BASE_LANGUAGE\". Needs to be set to a value present in 'languageTitles'. This represents the language which is placed as a default language in the localizations (so as 'values/strings.xml' resource in Android, which is the fallback language if a language is not supported).\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    baseLanguage = \"$DEFAULT_BASE_LANGUAGE\"\n" +
                        "}"
            )
        }

        if (config.languageTitles.none { it == config.baseLanguage }) {
            throw LocalizeConfigurationException(
                "'baseLanguage' is set to a value not present in 'languageTitles'. Needs to be set to a value present in 'languageTitles' (which is currently set to ${config.languageTitles.joinToString()}, so any of those values is valid). This represents the language which is placed as a default language in the localizations (so as 'values/strings.xml' resource in Android, which is the fallback language if a language is not supported).\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    baseLanguage = \"${config.languageTitles.first()}\"\n" +
                        "}"
            )
        }

        if (config.localizationPath == projectPath) {
            throw LocalizeConfigurationException(
                "'localizationPath' is set to an invalid value. Default is \"$DEFAULT_LOCALIZATION_PATH\". This is needed as a local path where to save the string resource xml files to. Normally this only needs to be changed if you need to place the string xml files in another module than the default app module.\n" +
                        "Example:\n" +
                        "$PLUGIN_CONFIGURATION_EXTENSION_NAME {\n" +
                        "    localizationPath = \"$DEFAULT_LOCALIZATION_PATH\"\n" +
                        "}"
            )
        }

    }

    private fun Project.getListOfFlavors(): List<ProductFlavor> {
        val androidAppExtension = project.extensions.findByType(AppExtension::class.java)
            ?: throw IllegalStateException("Could not find the android App Extensions, which indicates that this module is not an android application. Does your module apply the 'com.android.application' plugin?'")

        return androidAppExtension.productFlavors.toList()
    }

    private fun runTask(
        project: Project,
        baseConfig: BaseLocalizeExtension,
        taskToRun: suspend (config: LocalizationConfig) -> Unit
    ) {

        val flavorNames = project.getListOfFlavors().map { it.name }

        baseConfig.flavorConfigContainer.asMap.keys.forEach { configFlavor ->
            if (configFlavor !in flavorNames) {
                throw IllegalArgumentException("localizeConfig.$configFlavor has been declared, but no flavor 'android.productFlavors.$configFlavor' found. Current build flavors are: $flavorNames)")
            }
        }

        val configs = configProvider.getFlavorAwareConfigs(
            flavorNames = flavorNames,
            baseConfig = baseConfig
        )


        runBlocking {
            configs
                .map { config ->
                    checkConfiguration(project.projectDir, config)
                    async {
                        taskToRun.invoke(config)
                    }
                }
                .forEach { it.await() }
        }
    }

}

class LocalizeConfigurationException(message: String, cause: Throwable? = null) :
    IllegalArgumentException("$message\nCheck out the README on how to configure the localize plugin.", cause)
