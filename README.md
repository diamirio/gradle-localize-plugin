# Google Sheet Localize Gradle Plugin

Gradle Plugin to generate Android string resource files (`string.xml`) from a [Localization Spreadsheet](https://docs.google.com/spreadsheets/d/1fwRj1ZFPu2XlrDqkaqmIpJulqR5OVFEZnN35a9v37yc/edit?usp=sharing).

Once configured, it conveniently imports the string localizations of a given spreadsheet by calling the gradle task `./gradlew localize`.


This is a Gradle port of the fastlane plugin [google_sheet_localize](https://github.com/tailoredmedia/fastlane-plugin-localize).


## Setup

To use the plugin, you need to add the plugin as a classpath dependency to your buildscript (which is usually in your root-level `build.gradle` file).
Furthermore, you need to add the tailored-apps maven repository as a buildscript repository:

```groovy
buildscript {

    repositories {
        // other repositories here
        maven {
            url 'https://maven.tailored-apps.com/repository/maven-public/'

            // Optional, this ensures, that only dependencies of the 'com.tailoredapps.gradle' group are
            // requested from this repository. This ensures, that this repository does not gain any
            // information about which dependencies you use, as well as it stops this repository
            // from serving a tempered binary for any of the other dependencies you use.
            content { includeGroup 'com.tailoredapps.gradle' }
        }
    }

    dependencies {
        // other classpath dependencies here
        classpath 'com.tailoredapps.gradle:localize:0.4.0'
    }
}
```

Then, where you want to add the plugin, you need to apply it.
This is most likely in your app-level `build.gradle` file.
The location of this statement does not matter, however recommended is to put it on top with the other applied plugins:

```groovy
apply plugin: 'com.tailoredapps.gradle.localize'
```
Now you're (nearly) ready to use the plugin, you just need to configure it correctly.


## Configuration

To configure the plugin, it exposes a `localizeConfig` extension.
This needs to be placed in the same file you applied the plugin (most likely in your app-level `build.gradle` file).
At least the following configuration must be set, you can find all possible configuration fields further below.

Groovy:
```groovy
localizeConfig {
    serviceAccountCredentialsFile = "./google_drive_credentials.json"  // The location of your service-account credentials file (more about that below)
    configuration {
        main {  // you can define as much configuration sections as you like, within which you can define a source (sheetId) of the localizations as well as a destination. The name of a configuration section can be chosen arbitrarily by you.
            sheetId = "1fwRj1ZFPu2XlrDqkaqmIpJulqR5OVFEZnN35a9v37yc"   // The ID of the spreadsheet which contains the localizations
            languageTitles = ["de", "en"]                              // The column header of the languages you want to import
        }
    }
}
```

Kotlin Buildscript:
```kotlin
localizeConfig {
    serviceAccountCredentialsFile = "./google_drive_credentials.json"  // The location of your service-account credentials file (more about that below)
    configuration {
        create("main") {  // you can define as much configuration sections as you like, within which you can define a source (sheetId) of the localizations as well as a destination. The name of a configuration section can be chosen arbitrarily by you.
            sheetId = "1fwRj1ZFPu2XlrDqkaqmIpJulqR5OVFEZnN35a9v37yc"   // The ID of the spreadsheet which contains the localizations
            languageTitles = listOf("de", "en")                        // The column header of the languages you want to import
        }
    }
}
```

### Multiple configurations

This plugin allows you to configure multiple configurations (for e.g. multiple sources (sheets) for multiple build flavors).

To create an additional configuration, just create a lambda named with the name of your configuration
(which is not linked to any product flavors / build types, this can be any name you choose), add the
[product dependent fields](#product-dependent-configuration) you want to set in the lambda.

Groovy:
```groovy
localizeConfig {
    serviceAccountCredentialsFile = "./google_drive_credentials.json"  // The location of your service-account credentials file (more about that below)

    configuration {
        main {                                                         // 'main' is a name you can choose to name this configuration
            sheetId = "mainSheetIdHere"                                // for the product-configuration 'main', the given sheetId will be used
            localizationPath = "./src/main/res"                        // for the product-configuration 'main', the localizationPath "./src/main/res" will be used
            // the serviceAccountCredentialsFile will be taken from the base config above, as it is not defined here.
        }
        product1 {                                                     // 'product1' is a name you can choose to name this configuration
            sheetId = "product1SheetIdHere"                            // for the product-configuration 'product1', the given sheetId will be used
            localizationPath = "./src/flavor1/res"                     // for the product-configuration 'product1', the localizationPath "./src/flavor1/res" will be used
            serviceAccountCredentialsFile = "./product1/google_drive_credentials.json"  // the serviceAccountCredentialsFile is overwritten here
        }
    }
}
```

Kotlin Buildscript:
```kotlin
localizeConfig {
    serviceAccountCredentialsFile = "./google_drive_credentials.json"  // The location of your service-account credentials file (more about that below)

    configuration {
        create("main") {                                               // 'main' is a name you can choose to name this configuration
            sheetId = "mainSheetIdHere"                                // for the product-configuration 'main', the given sheetId will be used
            localizationPath = "./src/main/res"                        // for the product-configuration 'main', the localizationPath "./src/main/res" will be used
            // the serviceAccountCredentialsFile will be taken from the base config above, as it is not defined here.
        }
        create("product1") {                                           // 'product1' is a name you can choose to name this configuration
            sheetId = "product1SheetIdHere"                            // for the product-configuration 'product1', the given sheetId will be used
            localizationPath = "./src/flavor1/res"                     // for the product-configuration 'product1', the localizationPath "./src/flavor1/res" will be used
            serviceAccountCredentialsFile = "./product1/google_drive_credentials.json"  // the serviceAccountCredentialsFile is overwritten here
        }
    }
}
```


### Possible configuration fields

#### Base configuration
_([go to the definition in the code](./blob/master/src/main/kotlin/com/tailoredapps/gradle/localize/extension/BaseLocalizeExtension.kt))_

| Field                                   | Type      | Description                                                                                                                                                                                                                                                                        |
|:----------------------------------------|:----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `serviceAccountCredentialsFile`         | `String`  | The local path to the credentials file for the service-account. More about this in [Google Drive Service Account Credentials](#google-drive-service-account-credentials). Optional, if set in the product specific configuration                                                   |
| `baseLanguage` (default: `en`)          | `String`  | The language (one of the values from `languageTitles`) which should be the default language, which is placed in the `values` folder (so if this is set to `en`, there will be no `values-en` folder created, but the english localizations will be placed in the `values` folder). |
| `addComments` (default: `true`)         | `Boolean` | Whether the comments from the spreadsheet should be added to the strings.xml files (as comments) as well.                                                                                                                                                                          |
| `escapeApostrophes` (default: `true`)   | `Boolean` | Whether apostrophes in the spreadsheet should be escaped.                                                                                                                                                                                                                          |
| `generateEmptyValues` (default: `true`) | `Boolean` | Whether empty values should be put into the strings.xml files. Can be useful to allow a fallback to the default language if there is no value in a language column.                                                                                                                |


#### Product-dependent configuration
_([go to the definition in the code](./blob/master/src/main/kotlin/com/tailoredapps/gradle/localize/extensions/ProductLocalizeExtension.kt))_

| Field                                          | Type           | Description                                                                                                                                                                                                                                                                                                          |
|:-----------------------------------------------|:---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `serviceAccountCredentialsFile`                | `String`       | The local path to the credentials file for the service-account. More about this in [Google Drive Service Account Credentials](#google-drive-service-account-credentials). Optional, if set in the base configuration                                                                                                 |
| `sheetId`                                      | `String`       | The id of the spreadsheet which contains the localization entries. You can get this id from the link to your spreadsheet. e.g. For the spreadsheet-link `https://docs.google.com/spreadsheets/d/1fwRj1ZFPu2XlrDqkaqmIpJulqR5OVFEZnN35a9v37yc/edit`, the `sheetId` is `1fwRj1ZFPu2XlrDqkaqmIpJulqR5OVFEZnN35a9v37yc`. |
| `worksheets`                                   | `List<String>` | The titles of the worksheets ("tabs") of the spreadsheet which should be considered as source for localization entries. If not set, all worksheets will be parsed.                                                                                                                                                   |
| `languageTitles`                               | `List<String>` | The list of column titles of the languages in the localization sheet (which is simultaneously also the list of local language folders which are created, so those should be e.g. `de` for german or `en` for english, and the column titles in the sheet should be named the same.                                   |
| `baseLanguage` (default: `en`)                 | `String`       | The language (one of the values from `languageTitles`) which should be the default language, which is placed in the `values` folder (so if this is set to `en`, there will be no `values-en` folder created, but the english localizations will be placed in the `values` folder).                                   |
| `localizationPath` (default: `./src/main/res`) | `String`       | The base directory path to put the localizations in. This defaults to `./src/main/res`, which is the default path within an app module to put the string resources to. Change this if you want to have your localizations put somewhere else.                                                                        |
| `addComments`                                  | `Boolean`      | Whether the comments from the spreadsheet should be added to the strings.xml files (as comments) as well. Defaults to the value of the base configuration if not set                                                                                                                                                 |
| `escapeApostrophes` (default: `true`)          | `Boolean`      | Whether apostrophes in the spreadsheet should be escaped.                                                                                                                                                                                                                                                            |
| `generateEmptyValues` (default: `true`)        | `Boolean`      | Whether empty values should be put into the strings.xml files. Can be useful to allow a fallback to the default language if there is no value in a language column.                                                                                                                                                  |


## Sheet

The sheet supports string localizations as well as plurals and string-arrays. For examples / a quick overview, have a look at the example sheet [here](https://docs.google.com/spreadsheets/d/1fwRj1ZFPu2XlrDqkaqmIpJulqR5OVFEZnN35a9v37yc/edit?usp=sharing), especially `simulation.time.hour` and `strings.array.test`.

### String values

For a string value, just insert your value in the corresponding cell

### Plural values

For plural values, enter your values in one cell separated by lines, with the plural indicator starting the line, followed by a `|`, and then the value for that plural, e.g.:
```
one|%d hour
other|%d hours
```

### String arrays

For string arrays, place your values in a cell in a JSON String-Array format, e.g:
```
["first value", "second value", "third value"]
```

## Tasks

#### `localize`

Fetches the localizations from the spreadsheet and generates the `string.xml` and replaces 
the existing `string.xml` files with the new generated files.


#### `checkLocalization`

Fetches the localizations from the spreadsheet and checks whether the strings in the `strings.xml` files are still
up-to-date (and fails if not).


## Localization sheet

To manage different localizations to be entered for different platforms (currently iOS, Android, Web), duplicate [this google spreadsheet](https://docs.google.com/spreadsheets/d/1fwRj1ZFPu2XlrDqkaqmIpJulqR5OVFEZnN35a9v37yc/edit?usp=sharing) for your project.


## Google Drive Service Account Credentials

To be able to access a spreadsheet without OAuth authentication, you need to create service account credentials.

Best you follow [this guide](https://medium.com/@osanda.deshan/getting-google-oauth-access-token-using-google-apis-18b2ba11a11a) in how to do so.

The path to the locally stored `json` file you get out of that needs to be referenced as `serviceAccountCredentialsFile` in the configuration (you most likely do not want to check this file into your version control system, as it contains credentials which should be kept secret).


# License

```
Copyright 2020 Tailored Apps GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```