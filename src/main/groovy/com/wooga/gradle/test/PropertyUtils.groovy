package com.wooga.gradle.test

trait PropertyUtilsImpl {

    // TODO: Why do the following 2 methods have different replacements?

    /**
     * @param extensionName The name of the extension the property belongs to
     * @param property The name of the property
     * @return
     */
    static String envNameFromProperty(String extensionName, String property) {
        "${extensionName.toUpperCase()}_${property.replaceAll(/([A-Z])/, "_\$1").toUpperCase()}"
    }

    static String envNameFromProperty(String property) {
        property.replaceAll(/([A-Z.])/, '_$1').replaceAll(/[.]/, '').toUpperCase()
    }
}

class PropertyUtils implements PropertyUtilsImpl {
}

