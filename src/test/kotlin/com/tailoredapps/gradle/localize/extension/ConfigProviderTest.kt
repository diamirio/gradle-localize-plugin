package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.LocalizationConfig
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldNotBeEmpty
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Test

class ConfigProviderTest {
    private lateinit var configProvider: ConfigProvider

    @MockK
    private lateinit var extensionMerger: ExtensionMerger

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        configProvider = ConfigProvider(extensionMerger = extensionMerger)
    }

    @Test
    fun `getProductAwareConfigs for empty list of product configs`() {
        val baseConfig: BaseLocalizeExtension =
            mockk(relaxed = true) {
                productConfigContainer =
                    mockk {
                        every { asMap } returns sortedMapOf<String, ProductLocalizeExtension>()
                    }
            }

        val result = configProvider.getProductAwareConfigs(baseConfig = baseConfig)

        result.shouldNotBeNull()
        result.shouldBeEmpty()
        verify(exactly = 0) { extensionMerger.merge(any(), any(), any()) }
    }

    @Test
    fun `getProductAwareConfigs for list of one product config`() {
        val firstProductLocalizeExtension: ProductLocalizeExtension = mockk(relaxed = true)
        val baseConfig: BaseLocalizeExtension =
            mockk(relaxed = true) {
                productConfigContainer = mockk()
            }
        every { baseConfig.productConfigContainer.asMap } returns
            sortedMapOf<String, ProductLocalizeExtension>(
                "firstProduct" to firstProductLocalizeExtension
            )

        val firstMergedConfig: LocalizationConfig = mockk()
        every { extensionMerger.merge(any(), any(), any()) } returns firstMergedConfig

        val result = configProvider.getProductAwareConfigs(baseConfig = baseConfig)

        result.shouldNotBeNull()
        result.shouldNotBeEmpty()
        result.size `should be equal to` 1
        result.first() shouldBe firstMergedConfig
        verify(exactly = 1) {
            extensionMerger.merge(
                baseConfig,
                "firstProduct",
                firstProductLocalizeExtension
            )
        }
    }

    @Test
    fun `getProductAwareConfigs for list of two product config`() {
        val firstProductLocalizeExtension: ProductLocalizeExtension = mockk(relaxed = true)
        val secondProductLocalizeExtension: ProductLocalizeExtension = mockk(relaxed = true)
        val baseConfig: BaseLocalizeExtension =
            mockk(relaxed = true) {
                productConfigContainer = mockk()
            }
        every { baseConfig.productConfigContainer.asMap } returns
            sortedMapOf<String, ProductLocalizeExtension>(
                "firstProduct" to firstProductLocalizeExtension,
                "secondProduct" to secondProductLocalizeExtension
            )

        val firstMergedConfig: LocalizationConfig = mockk()
        val secondMergedConfig: LocalizationConfig = mockk()
        every { extensionMerger.merge(any(), "firstProduct", any()) } returns firstMergedConfig
        every { extensionMerger.merge(any(), "secondProduct", any()) } returns secondMergedConfig

        val result = configProvider.getProductAwareConfigs(baseConfig = baseConfig)

        result.shouldNotBeNull()
        result.shouldNotBeEmpty()
        result.size `should be equal to` 2
        result[0] shouldBe firstMergedConfig
        result[1] shouldBe secondMergedConfig
        verifyOrder {
            extensionMerger.merge(
                baseConfig,
                "firstProduct",
                firstProductLocalizeExtension
            )
            extensionMerger.merge(
                baseConfig,
                "secondProduct",
                secondProductLocalizeExtension
            )
        }
    }
}
