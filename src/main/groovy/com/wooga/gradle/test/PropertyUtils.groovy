package com.wooga.gradle.test

trait PropertyUtilsImpl {

    /**
     * @param extensionName The name of the extension the property belongs to
     * @param property The name of the property
     * @return
     */
    static String envNameFromProperty(String extensionName, String property) {
        envNameFromProperty(extensionName + "." + property)
    }

    static String envNameFromProperty(String property) {
        property.replaceAll(/([A-Z.])/, '_$1').replaceAll(/[.]/, '').toUpperCase()
    }
}

class PropertyUtils implements PropertyUtilsImpl {
}

