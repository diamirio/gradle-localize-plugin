package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.LocalizationConfig
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEmpty
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
    fun `getFlavorAwareConfigs for empty flavor list`() {
        val config: LocalizationConfig = mockk()
        every { extensionMerger.merge(any(), any()) } returns config

        val baseConfig: BaseLocalizeExtension = mockk()

        val result = configProvider.getFlavorAwareConfigs(
            flavorNames = emptyList(),
            baseConfig = baseConfig
        )

        verify { extensionMerger.merge(baseConfig, null) }

        result.shouldNotBeEmpty()
        result.size shouldBeEqualTo 1
        result.first() shouldBe config
    }

    @Test
    fun `getFlavorAwareConfigs for one flavor`() {
        val config: LocalizationConfig = mockk()
        every { extensionMerger.merge(any(), any()) } returns config

        val baseConfig: BaseLocalizeExtension = mockk()

        val result = configProvider.getFlavorAwareConfigs(
            flavorNames = listOf("mock"),
            baseConfig = baseConfig
        )

        verify { extensionMerger.merge(baseConfig, "mock") }

        result.shouldNotBeEmpty()
        result.size shouldBeEqualTo 1
        result.first() shouldBe config
    }

    @Test
    fun `getFlavorAwareConfigs for two flavors`() {
        val config1: LocalizationConfig = mockk()
        every { extensionMerger.merge(any(), eq("flavor1")) } returns config1
        val config2: LocalizationConfig = mockk()
        every { extensionMerger.merge(any(), eq("flavor2")) } returns config2

        val baseConfig: BaseLocalizeExtension = mockk()

        val result = configProvider.getFlavorAwareConfigs(
            flavorNames = listOf("flavor1", "flavor2"),
            baseConfig = baseConfig
        )

        verify { extensionMerger.merge(baseConfig, "flavor1") }
        verify { extensionMerger.merge(baseConfig, "flavor2") }

        result.shouldNotBeEmpty()
        result.size shouldBeEqualTo 2
        result[0] shouldBe config1
        result[1] shouldBe config2
    }

    @Test
    fun `getFlavorAwareConfigs for two flavors with same configs`() {
        val config: LocalizationConfig = mockk()
        every { extensionMerger.merge(any(), any()) } returns config

        val baseConfig: BaseLocalizeExtension = mockk()

        val result = configProvider.getFlavorAwareConfigs(
            flavorNames = listOf("flavor1", "flavor2"),
            baseConfig = baseConfig
        )

        verify { extensionMerger.merge(baseConfig, "flavor1") }
        verify { extensionMerger.merge(baseConfig, "flavor2") }

        result.shouldNotBeEmpty()
        result.size shouldBeEqualTo 1
        result[0] shouldBe config
    }

}