package com.wooga.gradle

import com.wooga.gradle.test.PropertyUtils
import spock.lang.Specification

class PropertyUtilsSpec extends Specification {

    def "environment name is converted from extension and property name"() {
        when:
        def environmentName = PropertyUtils.envNameFromProperty(extensionName, propertyName)

        then:
        environmentName == expectedEnvironmentName

        where:
        extensionName | propertyName | expectedEnvironmentName
        "foobar"      | "kat.zen"    | "FOOBAR_KAT_ZEN"
    }

    def "environment name is converted from property name"() {
        when:
        def environmentName = PropertyUtils.envNameFromProperty(propertyName)

        then:
        environmentName == expectedEnvironmentName

        where:
        propertyName | expectedEnvironmentName
        "kat.zen"    | "KAT_ZEN"
    }
}
