package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.DEFAULT_LOCALIZATION_PATH

/**
 * @param name The name of the flavor this config is for. This will automatically be set by the gradle plugin by the
 * name of the closure that has been invoked.
 *
 * @see BaseLocalizeExtension
 */
open class ProductLocalizeExtension(
    var name: String
) {
    /**
     * The local path to the credentials file for the service-account.
     * @see [BaseLocalizeExtension.serviceAccountCredentialsFile]
     */
    var serviceAccountCredentialsFile: String? = null

    /**
     * The id of the spreadsheet which contains the localization entries.
     * You can get this id from the link to your spreadsheet.
     */
    var sheetId: String = ""

    /**
     * The titles worksheets (_tabs_) to parse / take into consideration as source for strings.
     * If not set, all tabs of the sheet will be considered.
     */
    var worksheets: MutableList<String>? = null

    /**
     * The list of column titles of the languages in the localization sheet (which is simultaneously
     * also the list of local language folders which are created, so those should be e.g. `de` for german or `en` for
     * english, and the column titles in the sheet should be named the same.
     */
    var languageTitles: MutableList<String> = mutableListOf()

    /**
     * The language (one of the values from [languageTitles]) which should be the default language, which is placed
     * in the `values` folder (so if this is set to `en`, there will be no `values-en` folder created, but the english
     * localizations will be placed in the `values` folder).
     * @see [BaseLocalizeExtension.baseLanguage]
     */
    var baseLanguage: String? = null

    /**
     * The base directory path to put the localizations in. This defaults to the default path within
     * an app module to put the string resources to. Change this if you want to have your localizations put somewhere else.
     *
     * As this here is the flavor dependent config, you might want to use this here to set the target of the given
     * localizations to e.g. `./src/<myFlavorName>/res`.
     */
    var localizationPath: String = DEFAULT_LOCALIZATION_PATH

    /**
     * Whether the comments from the spreadsheet should be added to the strings.xml files (as comments) as well.
     * @see [BaseLocalizeExtension.addComments]
     */
    var addComments: Boolean? = null
}