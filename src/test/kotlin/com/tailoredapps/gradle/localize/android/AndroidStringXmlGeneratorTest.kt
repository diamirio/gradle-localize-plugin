package com.tailoredapps.gradle.localize.android

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
internal class AndroidStringXmlGeneratorTest {
    private lateinit var generator: AndroidStringXmlGenerator

    @Before
    fun setUp() {
        generator = AndroidStringXmlGenerator()
    }

    @Test
    fun `real example sheet values generates string xml for german values including comments`(): Unit = runBlocking {
        val values =
            listOf(
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "example.example",
                    value = "Mario hat %1s eine %2s gegessen",
                    comment = "beispiel beispiel"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "TBD",
                    value = "Mario hat %1d eine %2d gegessen",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test01",
                    value = "Test01",
                    comment = "Test01 is used because de is the default language"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test02",
                    value = "Bitte drücken Sie \"Fortsetzen\"",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test03",
                    value = "Hallo",
                    comment = "Hallo is used because de is the default language"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "continue",
                    value = "Continue test",
                    comment = "Continue is supported as a Variable name "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "switch",
                    value = "Switch test",
                    comment = "Switch is supported as a Variable name "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test04",
                    value = "Los geht's",
                    comment = "\"'\"test for Android"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "viewController.purchaseButton.title",
                    value = "upcase, downcase test ",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plural(
                    identifier = "simulation.time.hour",
                    entries = listOf("one" to "%d Stunde", "other" to "%d Stunden"),
                    comment = "Plurals example "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test05",
                    value = "Es ist ein %s",
                    comment = "Always use %s for strings, on iOS its converted to %@"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "TBD",
                    value = "hallo",
                    comment = "Test TBD"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "NR",
                    value = "hallo",
                    comment = "Test NR"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "strings.array.test",
                    values = listOf("test0", "test1", "test2"),
                    comment = "String array example"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test06",
                    value = "Links oder Rechts doppelklicken, um %d Sekunden zu überspringen",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "strings.array.test1",
                    values = listOf("Message vocal", "Message d'image", "Message d'emplacement"),
                    comment = "Test array with '"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "template2.example.example",
                    value = "Mario hat %1s eine %2s gegessen",
                    comment = "beispiel beispiel"
                )
            )

        val fileContent = generator.androidValuesToStringsXml(values, true)

        fileContent shouldBeEqualTo """<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <!-- beispiel beispiel -->
    <string name="example.example"><![CDATA[Mario hat %1s eine %2s gegessen]]></string>
    <string name="TBD"><![CDATA[Mario hat %1d eine %2d gegessen]]></string>
    <!-- Test01 is used because de is the default language -->
    <string name="android.test01"><![CDATA[Test01]]></string>
    <string name="android.test02"><![CDATA[Bitte drücken Sie "Fortsetzen"]]></string>
    <!-- Hallo is used because de is the default language -->
    <string name="android.test03"><![CDATA[Hallo]]></string>
    <!-- Continue is supported as a Variable name  -->
    <string name="continue"><![CDATA[Continue test]]></string>
    <!-- Switch is supported as a Variable name  -->
    <string name="switch"><![CDATA[Switch test]]></string>
    <!-- "'"test for Android -->
    <string name="android.test04"><![CDATA[Los geht\'s]]></string>
    <string name="viewController.purchaseButton.title"><![CDATA[upcase, downcase test ]]></string>
    <!-- Plurals example  -->
    <plurals name="simulation.time.hour">
        <item quantity="one"><![CDATA[%d Stunde]]></item>
        <item quantity="other"><![CDATA[%d Stunden]]></item>
    </plurals>
    <!-- Always use %s for strings, on iOS its converted to %@ -->
    <string name="android.test05"><![CDATA[Es ist ein %s]]></string>
    <!-- Test TBD -->
    <string name="TBD"><![CDATA[hallo]]></string>
    <!-- Test NR -->
    <string name="NR"><![CDATA[hallo]]></string>
    <!-- String array example -->
    <string-array name="strings.array.test">
        <item><![CDATA[test0]]></item>
        <item><![CDATA[test1]]></item>
        <item><![CDATA[test2]]></item>
    </string-array>
    <string name="android.test06"><![CDATA[Links oder Rechts doppelklicken, um %d Sekunden zu überspringen]]></string>
    <!-- Test array with ' -->
    <string-array name="strings.array.test1">
        <item><![CDATA[Message vocal]]></item>
        <item><![CDATA[Message d\'image]]></item>
        <item><![CDATA[Message d\'emplacement]]></item>
    </string-array>
    <!-- beispiel beispiel -->
    <string name="template2.example.example"><![CDATA[Mario hat %1s eine %2s gegessen]]></string>
</resources>
"""
    }

    @Test
    fun `real example sheet values generates string xml for english values including comments`(): Unit = runBlocking {
        val values =
            listOf(
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "example.example",
                    value = "Mario ate a %2s %1s",
                    comment = "beispiel beispiel"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "TBD",
                    value = "Mario ate a %2d %1d",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test01",
                    value = "TBD",
                    comment = "Test01 is used because de is the default language"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test02",
                    value = "Please press \"Continue\"",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Blank(
                    identifier = "android.test03",
                    comment = "Hallo is used because de is the default language"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "continue",
                    value = "Continue test",
                    comment = "Continue is supported as a Variable name "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "switch",
                    value = "Switch test",
                    comment = "Switch is supported as a Variable name "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test04",
                    value = "Los geht's",
                    comment = "\"'\"test for Android"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "viewController.purchaseButton.title",
                    value = "upcase, downcase test ",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plural(
                    identifier = "simulation.time.hour",
                    entries = listOf("one" to "%d Hour", "other" to "%d Hours"),
                    comment = "Plurals example "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test05",
                    value = "It is a %s",
                    comment = "Always use %s for strings, on iOS its converted to %@"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "TBD",
                    value = "hi",
                    comment = "Test TBD"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "NR",
                    value = "hi",
                    comment = "Test NR"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "strings.array.test",
                    values = listOf("test0", "test1", "test2"),
                    comment = "String array example"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test06",
                    value = "Double-click left or right to skip %d seconds.",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "strings.array.test1",
                    values = listOf("Message vocal", "Message d'image", "Message d'emplacement"),
                    comment = "Test array with '"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "template2.example.example",
                    value = "Mario ate a %2s %1s",
                    comment = "beispiel beispiel"
                )
            )

        val fileContent = generator.androidValuesToStringsXml(values, true)

        fileContent shouldBeEqualTo """<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <!-- beispiel beispiel -->
    <string name="example.example"><![CDATA[Mario ate a %2s %1s]]></string>
    <string name="TBD"><![CDATA[Mario ate a %2d %1d]]></string>
    <!-- Test01 is used because de is the default language -->
    <string name="android.test01"><![CDATA[TBD]]></string>
    <string name="android.test02"><![CDATA[Please press "Continue"]]></string>
    <!-- Hallo is used because de is the default language -->
    <string name="android.test03"></string>
    <!-- Continue is supported as a Variable name  -->
    <string name="continue"><![CDATA[Continue test]]></string>
    <!-- Switch is supported as a Variable name  -->
    <string name="switch"><![CDATA[Switch test]]></string>
    <!-- "'"test for Android -->
    <string name="android.test04"><![CDATA[Los geht\'s]]></string>
    <string name="viewController.purchaseButton.title"><![CDATA[upcase, downcase test ]]></string>
    <!-- Plurals example  -->
    <plurals name="simulation.time.hour">
        <item quantity="one"><![CDATA[%d Hour]]></item>
        <item quantity="other"><![CDATA[%d Hours]]></item>
    </plurals>
    <!-- Always use %s for strings, on iOS its converted to %@ -->
    <string name="android.test05"><![CDATA[It is a %s]]></string>
    <!-- Test TBD -->
    <string name="TBD"><![CDATA[hi]]></string>
    <!-- Test NR -->
    <string name="NR"><![CDATA[hi]]></string>
    <!-- String array example -->
    <string-array name="strings.array.test">
        <item><![CDATA[test0]]></item>
        <item><![CDATA[test1]]></item>
        <item><![CDATA[test2]]></item>
    </string-array>
    <string name="android.test06"><![CDATA[Double-click left or right to skip %d seconds.]]></string>
    <!-- Test array with ' -->
    <string-array name="strings.array.test1">
        <item><![CDATA[Message vocal]]></item>
        <item><![CDATA[Message d\'image]]></item>
        <item><![CDATA[Message d\'emplacement]]></item>
    </string-array>
    <!-- beispiel beispiel -->
    <string name="template2.example.example"><![CDATA[Mario ate a %2s %1s]]></string>
</resources>
"""
    }

    @Test
    fun `real example sheet values generates string xml for german values without comments`(): Unit = runBlocking {
        val values =
            listOf(
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "example.example",
                    value = "Mario hat %1s eine %2s gegessen",
                    comment = "beispiel beispiel"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "TBD",
                    value = "Mario hat %1d eine %2d gegessen",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test01",
                    value = "Test01",
                    comment = "Test01 is used because de is the default language"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test02",
                    value = "Bitte drücken Sie \"Fortsetzen\"",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test03",
                    value = "Hallo",
                    comment = "Hallo is used because de is the default language"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "continue",
                    value = "Continue test",
                    comment = "Continue is supported as a Variable name "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "switch",
                    value = "Switch test",
                    comment = "Switch is supported as a Variable name "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test04",
                    value = "Los geht's",
                    comment = "\"'\"test for Android"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "viewController.purchaseButton.title",
                    value = "upcase, downcase test ",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plural(
                    identifier = "simulation.time.hour",
                    entries = listOf("one" to "%d Stunde", "other" to "%d Stunden"),
                    comment = "Plurals example "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test05",
                    value = "Es ist ein %s",
                    comment = "Always use %s for strings, on iOS its converted to %@"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "TBD",
                    value = "hallo",
                    comment = "Test TBD"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "NR",
                    value = "hallo",
                    comment = "Test NR"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "strings.array.test",
                    values = listOf("test0", "test1", "test2"),
                    comment = "String array example"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test06",
                    value = "Links oder Rechts doppelklicken, um %d Sekunden zu überspringen",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "strings.array.test1",
                    values = listOf("Message vocal", "Message d'image", "Message d'emplacement"),
                    comment = "Test array with '"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "template2.example.example",
                    value = "Mario hat %1s eine %2s gegessen",
                    comment = "beispiel beispiel"
                )
            )

        val fileContent = generator.androidValuesToStringsXml(values, false)

        fileContent shouldBeEqualTo """<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <string name="example.example"><![CDATA[Mario hat %1s eine %2s gegessen]]></string>
    <string name="TBD"><![CDATA[Mario hat %1d eine %2d gegessen]]></string>
    <string name="android.test01"><![CDATA[Test01]]></string>
    <string name="android.test02"><![CDATA[Bitte drücken Sie "Fortsetzen"]]></string>
    <string name="android.test03"><![CDATA[Hallo]]></string>
    <string name="continue"><![CDATA[Continue test]]></string>
    <string name="switch"><![CDATA[Switch test]]></string>
    <string name="android.test04"><![CDATA[Los geht\'s]]></string>
    <string name="viewController.purchaseButton.title"><![CDATA[upcase, downcase test ]]></string>
    <plurals name="simulation.time.hour">
        <item quantity="one"><![CDATA[%d Stunde]]></item>
        <item quantity="other"><![CDATA[%d Stunden]]></item>
    </plurals>
    <string name="android.test05"><![CDATA[Es ist ein %s]]></string>
    <string name="TBD"><![CDATA[hallo]]></string>
    <string name="NR"><![CDATA[hallo]]></string>
    <string-array name="strings.array.test">
        <item><![CDATA[test0]]></item>
        <item><![CDATA[test1]]></item>
        <item><![CDATA[test2]]></item>
    </string-array>
    <string name="android.test06"><![CDATA[Links oder Rechts doppelklicken, um %d Sekunden zu überspringen]]></string>
    <string-array name="strings.array.test1">
        <item><![CDATA[Message vocal]]></item>
        <item><![CDATA[Message d\'image]]></item>
        <item><![CDATA[Message d\'emplacement]]></item>
    </string-array>
    <string name="template2.example.example"><![CDATA[Mario hat %1s eine %2s gegessen]]></string>
</resources>
"""
    }

    @Test
    fun `real example sheet values generates string xml for english values without comments`(): Unit = runBlocking {
        val values =
            listOf(
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "example.example",
                    value = "Mario ate a %2s %1s",
                    comment = "beispiel beispiel"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "TBD",
                    value = "Mario ate a %2d %1d",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test01",
                    value = "TBD",
                    comment = "Test01 is used because de is the default language"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test02",
                    value = "Please press \"Continue\"",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Blank(
                    identifier = "android.test03",
                    comment = "Hallo is used because de is the default language"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "continue",
                    value = "Continue test",
                    comment = "Continue is supported as a Variable name "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "switch",
                    value = "Switch test",
                    comment = "Switch is supported as a Variable name "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test04",
                    value = "Los geht's",
                    comment = "\"'\"test for Android"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "viewController.purchaseButton.title",
                    value = "upcase, downcase test ",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plural(
                    identifier = "simulation.time.hour",
                    entries = listOf("one" to "%d Hour", "other" to "%d Hours"),
                    comment = "Plurals example "
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test05",
                    value = "It is a %s",
                    comment = "Always use %s for strings, on iOS its converted to %@"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "TBD",
                    value = "hi",
                    comment = "Test TBD"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "NR",
                    value = "hi",
                    comment = "Test NR"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "strings.array.test",
                    values = listOf("test0", "test1", "test2"),
                    comment = "String array example"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "android.test06",
                    value = "Double-click left or right to skip %d seconds.",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "strings.array.test1",
                    values = listOf("Message vocal", "Message d'image", "Message d'emplacement"),
                    comment = "Test array with '"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "template2.example.example",
                    value = "Mario ate a %2s %1s",
                    comment = "beispiel beispiel"
                )
            )

        val fileContent = generator.androidValuesToStringsXml(values, false)

        fileContent shouldBeEqualTo """<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <string name="example.example"><![CDATA[Mario ate a %2s %1s]]></string>
    <string name="TBD"><![CDATA[Mario ate a %2d %1d]]></string>
    <string name="android.test01"><![CDATA[TBD]]></string>
    <string name="android.test02"><![CDATA[Please press "Continue"]]></string>
    <string name="android.test03"></string>
    <string name="continue"><![CDATA[Continue test]]></string>
    <string name="switch"><![CDATA[Switch test]]></string>
    <string name="android.test04"><![CDATA[Los geht\'s]]></string>
    <string name="viewController.purchaseButton.title"><![CDATA[upcase, downcase test ]]></string>
    <plurals name="simulation.time.hour">
        <item quantity="one"><![CDATA[%d Hour]]></item>
        <item quantity="other"><![CDATA[%d Hours]]></item>
    </plurals>
    <string name="android.test05"><![CDATA[It is a %s]]></string>
    <string name="TBD"><![CDATA[hi]]></string>
    <string name="NR"><![CDATA[hi]]></string>
    <string-array name="strings.array.test">
        <item><![CDATA[test0]]></item>
        <item><![CDATA[test1]]></item>
        <item><![CDATA[test2]]></item>
    </string-array>
    <string name="android.test06"><![CDATA[Double-click left or right to skip %d seconds.]]></string>
    <string-array name="strings.array.test1">
        <item><![CDATA[Message vocal]]></item>
        <item><![CDATA[Message d\'image]]></item>
        <item><![CDATA[Message d\'emplacement]]></item>
    </string-array>
    <string name="template2.example.example"><![CDATA[Mario ate a %2s %1s]]></string>
</resources>
"""
    }

    @Test
    fun `sheet values with apostrophe and escapeApostrophes enabled`(): Unit = runBlocking {
        val values =
            listOf(
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "example.1",
                    value = "This is a sample's text",
                    comment = "sample comment"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "example.2",
                    value = "This is a sample\\'s text",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "strings.array.test",
                    values = listOf("test0", "test1", "test2"),
                    comment = "String array example"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plural(
                    identifier = "strings.array.test",
                    entries = listOf("one" to "example's", "other" to "example\\'s"),
                    comment = "String array example"
                )
            )

        val fileContent =
            generator.androidValuesToStringsXml(
                values = values,
                addComments = false,
                escapeApostrophes = true
            )

        fileContent shouldBeEqualTo """<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <string name="example.1"><![CDATA[This is a sample\'s text]]></string>
    <string name="example.2"><![CDATA[This is a sample\\'s text]]></string>
    <string-array name="strings.array.test">
        <item><![CDATA[test0]]></item>
        <item><![CDATA[test1]]></item>
        <item><![CDATA[test2]]></item>
    </string-array>
    <plurals name="strings.array.test">
        <item quantity="one"><![CDATA[example\'s]]></item>
        <item quantity="other"><![CDATA[example\\'s]]></item>
    </plurals>
</resources>
"""
    }

    @Test
    fun `sheet values with apostrophe and escapeApostrophes disabled`(): Unit = runBlocking {
        val values =
            listOf(
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "example.1",
                    value = "This is a sample's text",
                    comment = "sample comment"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plain(
                    identifier = "example.2",
                    value = "This is a sample\\'s text",
                    comment = null
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "strings.array.test",
                    values = listOf("test0", "test1", "test2"),
                    comment = "String array example"
                ),
                ParsedSheetToAndroidTransformer.AndroidValue.Plural(
                    identifier = "strings.array.test",
                    entries = listOf("one" to "example's", "other" to "example\\'s"),
                    comment = "String array example"
                )
            )

        val fileContent =
            generator.androidValuesToStringsXml(
                values = values,
                addComments = false,
                escapeApostrophes = false
            )

        fileContent shouldBeEqualTo """<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <string name="example.1"><![CDATA[This is a sample's text]]></string>
    <string name="example.2"><![CDATA[This is a sample\'s text]]></string>
    <string-array name="strings.array.test">
        <item><![CDATA[test0]]></item>
        <item><![CDATA[test1]]></item>
        <item><![CDATA[test2]]></item>
    </string-array>
    <plurals name="strings.array.test">
        <item quantity="one"><![CDATA[example's]]></item>
        <item quantity="other"><![CDATA[example\'s]]></item>
    </plurals>
</resources>
"""
    }

    @Test
    fun `parsed real example for previously failing array definitions`(): Unit = runBlocking {
        val values =
            listOf(
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "some_identifier",
                    values =
                    listOf(
                        "A first item\n",
                        "A second item with two line breaks\n\n",
                        "A third item with no line breaks"
                    ),
                    comment = null
                )
            )

        val fileContent =
            generator.androidValuesToStringsXml(
                values = values,
                addComments = false,
                escapeApostrophes = false
            )

        fileContent shouldBeEqualTo """<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <string-array name="some_identifier">
        <item><![CDATA[A first item\n]]></item>
        <item><![CDATA[A second item with two line breaks\n\n]]></item>
        <item><![CDATA[A third item with no line breaks]]></item>
    </string-array>
</resources>
"""
    }

    @Test
    fun `parsed real example for previously failing array definitions with different line breaks`(): Unit = runBlocking {
        val values =
            listOf(
                ParsedSheetToAndroidTransformer.AndroidValue.Array(
                    identifier = "some_identifier",
                    values =
                    listOf(
                        "A first item\n\n\n",
                        "A second item",
                        "A third item\n\n"
                    ),
                    comment = null
                )
            )

        val fileContent =
            generator.androidValuesToStringsXml(
                values = values,
                addComments = false,
                escapeApostrophes = false
            )

        fileContent shouldBeEqualTo """<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <string-array name="some_identifier">
        <item><![CDATA[A first item\n\n\n]]></item>
        <item><![CDATA[A second item]]></item>
        <item><![CDATA[A third item\n\n]]></item>
    </string-array>
</resources>
"""
    }
}
