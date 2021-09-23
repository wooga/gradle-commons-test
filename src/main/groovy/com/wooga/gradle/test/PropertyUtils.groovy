package com.wooga.gradle.test

class PropertyUtils {

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

    static String toCamelCase(String input) {
        input.replaceAll(/\(\)/,"").replaceAll(/((\/|-|_|\.)+)([\w])/, { all, delimiterAll, delimiter, firstAfter -> "${firstAfter.toUpperCase()}" })
    }

    /**
     * Returns the {@code Provider} set method string for the given property name.
     * This method simply appends {@code .set} to the provided property string.
     *
     * @param propertyName The property name to convert to a {@code Provider} set method
     * @return The {@code Provider} set method string for the given property name.
     */
    static String toProviderSet(String propertyName) {
        "${propertyName}.set"
    }

    /**
     * Returns the setter method string for the given property name.
     * <p>
     * If the property name is fully qualified it will split at `.`
     * and append the setter prefix to the last item.
     * <p>
     * {@code foo.bar.baz -> foo.bar.setBaz}
     * @param propertyName The property name to convert to a setter
     * @return The setter method string for the given property name.
     */
    static String toSetter(String propertyName) {
        def propertyChain = propertyName.split(/\./).reverse().toList()
        if (propertyChain.size() > 1) {
            return (propertyChain.tail().reverse() << toSetter(propertyChain.head())).join(".")
        }
        "set${propertyChain.head().capitalize()}"
    }
}

