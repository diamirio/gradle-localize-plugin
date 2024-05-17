package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.LocalizationConfig

class ConfigProvider(private val extensionMerger: ExtensionMerger) {
    fun getProductAwareConfigs(baseConfig: BaseLocalizeExtension): List<LocalizationConfig> {
        return baseConfig.productConfigContainer.asMap.map { (productConfigName, productConfig) ->
            extensionMerger.merge(
                baseConfig = baseConfig,
                productConfigName = productConfigName,
                productConfig = productConfig
            )
        }
    }
}
