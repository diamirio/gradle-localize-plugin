package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.LocalizationConfig
import com.tailoredapps.gradle.localize.util.PathToFileManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Test
import java.io.File


class ExtensionMergerTest {

    private lateinit var extensionMerger: ExtensionMerger

    @MockK
    private lateinit var pathToFileManager: PathToFileManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { pathToFileManager.pathToFile(any()) } answers { File("/tmp", arg<String>(0)) }

        extensionMerger = ExtensionMerger(pathToFileManager)
    }


    @Test
    fun `empty base config with one flavor config`() {
        val productConfig = ProductLocalizeExtension("mock").apply {
            serviceAccountCredentialsFile = "./some-service-accounts-file.json"
            sheetId = "ASDF1234!"
            languageTitles = mutableListOf("de", "en", "ru")
            baseLanguage = "ru"
            localizationPath = "./src/main/some-custom-res"
            addComments = true
        }
        val baseConfig = prepareBaseLocalizeExtension(listOf(productConfig))

        val configForMock = extensionMerger.merge(
            baseConfig = baseConfig,
            productConfigName = "mock",
            productConfig = productConfig
        )

        configForMock.shouldNotBeNull()
        configForMock shouldBeEqualTo LocalizationConfig(
            productName = "mock",
            serviceAccountCredentialsFile = File("/tmp/./some-service-accounts-file.json"),
            sheetId = "ASDF1234!",
            languageTitles = listOf("de", "en", "ru"),
            baseLanguage = "ru",
            localizationPath = File("/tmp", "./src/main/some-custom-res"),
            addToCheckTask = true,
            addComments = true
        )
    }

    @Test
    fun `base config only with flavor overwriting all possible fields`() {
        val mockProductFlavor = ProductLocalizeExtension("someProductName").apply {
            serviceAccountCredentialsFile = "./some-service-accounts-file-for-mock.json"
            sheetId = "SHEET_FOR_MOCK"
            languageTitles = mutableListOf("de", "en", "ru", "it")
            baseLanguage = "it"
            localizationPath = "./src/main/some-custom-res-for-mock"
            addComments = false
        }
        val baseConfig = prepareBaseLocalizeExtension(
            listOf(mockProductFlavor)
        ).apply {
            serviceAccountCredentialsFile = "./some-service-accounts-file.json"
            baseLanguage = "ru"
            addToCheckTask = true
            addComments = true
        }
        val configForMock = extensionMerger.merge(
            baseConfig = baseConfig,
            productConfigName = "someProductName",
            productConfig = mockProductFlavor
        )

        configForMock.shouldNotBeNull()
        configForMock shouldBeEqualTo LocalizationConfig(
            productName = "someProductName",
            serviceAccountCredentialsFile = File("/tmp/./some-service-accounts-file-for-mock.json"),
            sheetId = "SHEET_FOR_MOCK",
            languageTitles = listOf("de", "en", "ru", "it"),
            baseLanguage = "it",
            localizationPath = File("/tmp", "./src/main/some-custom-res-for-mock"),
            addToCheckTask = true,
            addComments = false
        )
    }


    private fun prepareBaseLocalizeExtension(productExtensions: List<ProductLocalizeExtension> = emptyList()) =
        BaseLocalizeExtension().apply {
            productConfigContainer = mockk()
            every { productConfigContainer.isEmpty() } returns productExtensions.isEmpty()
            every { productConfigContainer.asMap } returns productExtensions.associateBy { it.name }
                .toSortedMap()
        }

}