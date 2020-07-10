package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.LocalizationConfig

class ConfigProvider(private val extensionMerger: ExtensionMerger) {

    fun getFlavorAwareConfigs(flavorNames: List<String>, baseConfig: BaseLocalizeExtension): List<LocalizationConfig> {
        return if (flavorNames.isEmpty()) {
            //no flavors configured in android extension -> use base extension
            val config = extensionMerger.merge(
                baseConfig = baseConfig,
                flavor = null
            )
            listOf(config)
        } else {
            flavorNames
                .map { flavorName ->
                    extensionMerger.merge(
                        baseConfig = baseConfig,
                        flavor = flavorName
                    )
                }
                .distinct()
        }
    }

}
