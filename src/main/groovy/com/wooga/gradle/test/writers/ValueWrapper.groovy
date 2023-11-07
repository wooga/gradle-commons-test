package com.wooga.gradle.test.writers

import com.wooga.gradle.test.IntegrationHandler
import com.wooga.gradle.test.PropertyUtils
import nebula.test.Integration
import org.gradle.internal.impldep.org.apache.http.annotation.Obsolete

/**
 * Provides functions for writing values onto a `build.gradle` script file, by wrapping them onto the expected DSL
 */
trait ValueWrapper {

    String wrapValue(Object rawValue, Class type, Closure<String> fallback = null) {
        wrapValue(rawValue, type.simpleName, fallback)
    }

    String wrapValue(Object rawValue, String type, Closure<String> fallback = null) {
        return PropertyUtils.wrapValue(rawValue, type, fallback)
    }

    // TODO: Remove for next major release
    /**
     * @deprecated Use the more succinct {@code wrapValue}
     */
    @Obsolete
    String wrapValueBasedOnType(Object rawValue, Class type, Closure<String> fallback = null) {
        wrapValue(rawValue, type.simpleName, fallback)
    }

    /**
     * @deprecated Use the more succinct {@code wrapValue}
     */
    @Obsolete
    String wrapValueBasedOnType(Object rawValue, String type, Closure<String> fallback = null) {
        wrapValue(rawValue, type, fallback)
    }


    String wrapString(Object rawValue) {
        return wrapValue(rawValue, String)
    }

    String wrapEnum(Enum rawValue, Class enumClass) {
        return "${enumClass.simpleName}.${rawValue.toString()}"
    }

    String wrapFile(File file) {
        return wrapValue(file, File)
    }
}

