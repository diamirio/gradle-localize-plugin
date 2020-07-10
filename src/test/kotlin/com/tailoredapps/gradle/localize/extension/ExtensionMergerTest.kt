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
    fun `empty base config only without flavor`() {
        val config = extensionMerger.merge(
            baseConfig = prepareBaseLocalizeExtension(),
            flavor = null
        )

        config.shouldNotBeNull()
        config shouldBeEqualTo LocalizationConfig(
            serviceAccountCredentialsFile = File("/tmp"),
            sheetId = "",
            languageTitles = emptyList(),
            baseLanguage = "en",
            localizationPath = File("/tmp", "./src/main/res"),
            addToCheckTask = true,
            addComments = true
        )
    }

    @Test
    fun `base config only without flavor`() {
        val config = extensionMerger.merge(
            baseConfig = prepareBaseLocalizeExtension().apply {
                serviceAccountCredentialsFile = "./some-service-accounts-file.json"
                sheetId = "ASDF1234!"
                languageTitles = mutableListOf("de", "en", "ru")
                baseLanguage = "ru"
                localizationPath = "./src/main/some-custom-res"
                addToCheckTask = true
                addComments = true
            },
            flavor = null
        )

        config.shouldNotBeNull()
        config shouldBeEqualTo LocalizationConfig(
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
    fun `empty base config with one flavor config`() {
        val baseConfig = prepareBaseLocalizeExtension(
            listOf(
                FlavorLocalizeExtension("mock").apply {
                    serviceAccountCredentialsFile = "./some-service-accounts-file.json"
                    sheetId = "ASDF1234!"
                    languageTitles = mutableListOf("de", "en", "ru")
                    baseLanguage = "ru"
                    localizationPath = "./src/main/some-custom-res"
                    addComments = true
                }
            )
        )
        val configWithoutFlavor = extensionMerger.merge(
            baseConfig = baseConfig,
            flavor = null
        )

        val configForMock = extensionMerger.merge(
            baseConfig = baseConfig,
            flavor = "mock"
        )

        configWithoutFlavor.shouldNotBeNull()
        configWithoutFlavor shouldBeEqualTo LocalizationConfig(
            serviceAccountCredentialsFile = File("/tmp"),
            sheetId = "",
            languageTitles = emptyList(),
            baseLanguage = "en",
            localizationPath = File("/tmp/./src/main/res"),
            addToCheckTask = true,
            addComments = true
        )

        configForMock.shouldNotBeNull()
        configForMock shouldBeEqualTo LocalizationConfig(
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
    fun `base config only with flavor overwriting all fields`() {
        val baseConfig = prepareBaseLocalizeExtension(
            listOf(
                FlavorLocalizeExtension("mock").apply {
                    serviceAccountCredentialsFile = "./some-service-accounts-file-for-mock.json"
                    sheetId = "SHEET_FOR_MOCK"
                    languageTitles = mutableListOf("de", "en", "ru", "it")
                    baseLanguage = "it"
                    localizationPath = "./src/main/some-custom-res-for-mock"
                    addComments = false
                }
            )
        ).apply {
            serviceAccountCredentialsFile = "./some-service-accounts-file.json"
            sheetId = "ASDF1234!"
            languageTitles = mutableListOf("de", "en", "ru")
            baseLanguage = "ru"
            localizationPath = "./src/main/some-custom-res"
            addToCheckTask = true
            addComments = true
        }
        val configWithoutFlavor = extensionMerger.merge(
            baseConfig = baseConfig,
            flavor = null
        )
        val configForMock = extensionMerger.merge(
            baseConfig = baseConfig,
            flavor = "mock"
        )

        val configForNonExistingFlavor = extensionMerger.merge(
            baseConfig = baseConfig,
            flavor = "some-nonexistent-flavor"
        )

        configWithoutFlavor.shouldNotBeNull()
        configWithoutFlavor shouldBeEqualTo LocalizationConfig(
            serviceAccountCredentialsFile = File("/tmp/./some-service-accounts-file.json"),
            sheetId = "ASDF1234!",
            languageTitles = listOf("de", "en", "ru"),
            baseLanguage = "ru",
            localizationPath = File("/tmp", "./src/main/some-custom-res"),
            addToCheckTask = true,
            addComments = true
        )

        configForMock.shouldNotBeNull()
        configForMock shouldBeEqualTo LocalizationConfig(
            serviceAccountCredentialsFile = File("/tmp/./some-service-accounts-file-for-mock.json"),
            sheetId = "SHEET_FOR_MOCK",
            languageTitles = listOf("de", "en", "ru", "it"),
            baseLanguage = "it",
            localizationPath = File("/tmp", "./src/main/some-custom-res-for-mock"),
            addToCheckTask = true,
            addComments = false
        )

        configForNonExistingFlavor.shouldNotBeNull()
        configForNonExistingFlavor shouldBeEqualTo LocalizationConfig(
            serviceAccountCredentialsFile = File("/tmp/./some-service-accounts-file.json"),
            sheetId = "ASDF1234!",
            languageTitles = listOf("de", "en", "ru"),
            baseLanguage = "ru",
            localizationPath = File("/tmp", "./src/main/some-custom-res"),
            addToCheckTask = true,
            addComments = true
        )

    }


    private fun prepareBaseLocalizeExtension(flavorExtensions: List<FlavorLocalizeExtension> = emptyList()) =
        BaseLocalizeExtension().apply {
            flavorConfigContainer = mockk()
            every { flavorConfigContainer.isEmpty() } returns flavorExtensions.isEmpty()
            every { flavorConfigContainer.asMap } returns flavorExtensions.associateBy { it.name }.toSortedMap()
        }

}