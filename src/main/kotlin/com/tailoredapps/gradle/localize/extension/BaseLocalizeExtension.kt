package com.tailoredapps.gradle.localize.extension

import com.tailoredapps.gradle.localize.DEFAULT_BASE_LANGUAGE
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer

/**
 * @param serviceAccountCredentialsFile The local path to the credentials file for the service-account.
 * @param baseLanguage The language (one of the values from `languageTitles`) which should be the default language, which is placed
 * in the `values` folder (so if this is set to `en`, there will be no `values-en` folder created, but the english
 * localizations will be placed in the `values` folder).
 * @param addToCheckTask Whether this plugin should add the `checkLocalization` task to the default `check` task.
 * @param addComments Whether the comments from the spreadsheet should be added to the strings.xml files (as comments)
 * as well.
 * @param escapeApostrophes Whether apostrophes should be escaped.
 * @param generateEmptyValues Whether empty [values] should be put into the `strings.xml`. Defaults
 * to true. Can be useful to allow a fallback to the default language if there is no value in a column.
 */
open class BaseLocalizeExtension(
    var serviceAccountCredentialsFile: String = "",
    var baseLanguage: String = DEFAULT_BASE_LANGUAGE,
    var addToCheckTask: Boolean = true,
    var addComments: Boolean = true,
    var escapeApostrophes: Boolean = true,
    var generateEmptyValues: Boolean = true
) {

    /**
     * Adds a flavor specific config which may overwrite any config of the base config (except [addToCheckTask]).
     *
     * @see ProductLocalizeExtension
     */
    fun configuration(closure: Closure<ProductLocalizeExtension>) {
        productConfigContainer.configure(closure)
    }

    /**
     * Adds a flavor specific config which may overwrite any config of the base config (except [addToCheckTask]).
     *
     * @see ProductLocalizeExtension
     */
    fun configuration(action: NamedDomainObjectContainer<ProductLocalizeExtension>.() -> Unit) {
        action.invoke(productConfigContainer)
    }

    internal lateinit var productConfigContainer: NamedDomainObjectContainer<ProductLocalizeExtension>
}
