package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.DEFAULT_BASE_LANGUAGE
import com.tailoredapps.gradle.localize.DEFAULT_LOCALIZATION_PATH
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer

/**
 * @param serviceAccountCredentialsFile The local path to the credentials file for the service-account.
 * @param sheetId The id of the spreadsheet which contains the localization entries. You can get this id from the link
 * to your spreadsheet.
 * @param languageTitles The list of column titles of the languages in the localization sheet (which is simultaneously
 * also the list of local language folders which are created, so those should be e.g. `de` for german or `en` for
 * english, and the column titles in the sheet should be named the same.
 * @param baseLanguage The language (one of the values from [languageTitles]) which should be the default language, which is placed
 * in the `values` folder (so if this is set to `en`, there will be no `values-en` folder created, but the english
 * localizations will be placed in the `values` folder).
 * @param localizationPath The base directory path to put the localizations in. This defaults to the default path within
 * an app module to put the string resources to. Change this if you want to have your localizations put somewhere else.
 * @param addToCheckTask Whether this plugin should add the `checkLocalization` task to the default `check` task.
 * @param addComments Whether the comments from the spreadsheet should be added to the strings.xml files (as comments)
 * as well.
 */
open class BaseLocalizeExtension(
    var serviceAccountCredentialsFile: String = "",
    var sheetId: String = "",
    var languageTitles: MutableList<String> = mutableListOf(),
    var baseLanguage: String = DEFAULT_BASE_LANGUAGE,
    var localizationPath: String = DEFAULT_LOCALIZATION_PATH,
    var addToCheckTask: Boolean = true,
    var addComments: Boolean = true
) {

    /**
     * Adds a flavor specific config which may overwrite any config of the base config (except [addToCheckTask]).
     *
     * @see FlavorLocalizeExtension
     */
    fun productFlavors(closure: Closure<FlavorLocalizeExtension>) {
        flavorConfigContainer.configure(closure)
    }

    internal lateinit var flavorConfigContainer: NamedDomainObjectContainer<FlavorLocalizeExtension>
}
