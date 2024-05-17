package com.tailoredapps.gradle.localize

import com.tailoredapps.gradle.localize.extension.BaseLocalizeExtension
import com.tailoredapps.gradle.localize.extension.ConfigProvider
import com.tailoredapps.gradle.localize.extension.ConfigVerifier
import com.tailoredapps.gradle.localize.extension.ExtensionMerger
import com.tailoredapps.gradle.localize.extension.ProductLocalizeExtension
import com.tailoredapps.gradle.localize.util.PathToFileManager
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project

internal const val PLUGIN_CONFIGURATION_EXTENSION_NAME = "localizeConfig"
internal const val DEFAULT_LOCALIZATION_PATH = "./src/main/res"
internal const val DEFAULT_BASE_LANGUAGE = "en"

internal const val PLUGIN_TASK_GROUP_NAME = "localization"

//created using this tutorial: https://dzone.com/articles/the-complete-custom-gradle-plugin-building-tutoria
@Suppress("unused")
class GradleLocalizePlugin : Plugin<Project> {

    private val localize: Localize by lazy { Localize() }
    private lateinit var extensionMerger: ExtensionMerger
    private lateinit var pathToFileManager: PathToFileManager
    private lateinit var configProvider: ConfigProvider
    private lateinit var configVerifier: ConfigVerifier

    override fun apply(project: Project) {
        pathToFileManager = PathToFileManager(project)
        extensionMerger = ExtensionMerger(pathToFileManager = pathToFileManager)
        configProvider = ConfigProvider(extensionMerger = extensionMerger)
        configVerifier = ConfigVerifier()

        val extension = project.extensions.create(
            PLUGIN_CONFIGURATION_EXTENSION_NAME,
            BaseLocalizeExtension::class.java
        )
        extension.productConfigContainer = project.container(ProductLocalizeExtension::class.java)

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

        project.tasks.create("checkLocalization") { task ->
            task.doLast {
                runTask(project, extension) { config ->
                    localize.check(config)
                }
            }
        }.apply {
            description = "Checks whether the local localizations are up-to-date."
            group = PLUGIN_TASK_GROUP_NAME
        }

    }

    private fun runTask(
        project: Project,
        baseConfig: BaseLocalizeExtension,
        taskToRun: suspend (config: LocalizationConfig) -> Unit
    ) {
        val configs = configProvider.getProductAwareConfigs(baseConfig)

        runBlocking {
            configs
                .map { config ->
                    configVerifier.checkConfiguration(project.projectDir, config)
                    async {
                        taskToRun.invoke(config)
                    }
                }
                .forEach { it.await() }
        }
    }

}
