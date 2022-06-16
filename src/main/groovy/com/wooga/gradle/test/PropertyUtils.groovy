package com.wooga.gradle.test

import org.gradle.api.Incubating

import static com.wooga.gradle.PlatformUtils.escapedPath

//TODO: reroute to com.wooga.gradle.PropertyUtils
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
        property.replaceAll(/([A-Z.]|[0-9]+)/, '_$1').replaceAll(/[.]/, '').toUpperCase()
    }

    static String toCamelCase(String input) {
        input.replaceAll(/\(\)/,"").toLowerCase().replaceAll(/((\/|-|_|\.)+)([\w])/, { all, delimiterAll, delimiter, firstAfter -> "${firstAfter.toUpperCase()}" })
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
     * @return The components of a property path
     */
    static String[] getPathComponents(String path) {
        path.split(/\./)
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

    /**
     * Generates a build.gradle-aware code line where rawValue is represented as type.
     * @param rawValue value to be represented
     * @param type type to convert rawValue to
     * @param fallback fallback closure for specific types not covered by this method.
     * @return build.gradle aware code string representing rawValue as type.
     */
    static String wrapValueBasedOnType(Object rawValue, Class type, Closure<String> fallback = null) {
        return wrapValueBasedOnType(rawValue, type.simpleName, fallback)
    }

    /**
     * Generates a build.gradle-aware code line where rawValue is represented as type.
     * @param rawValue value to be represented
     * @param type type to convert rawValue to
     * @param fallback fallback closure for specific types not covered by this method.
     * @return build.gradle aware code string representing rawValue as type.
     */
    static String wrapValueBasedOnType(Object rawValue, String type, Closure<String> fallback = null) {
        String value

        def match = StringTypeMatch.match(type)
        type = match.mainType
        def subType = match.subType

        switch (type) {
            case "Closure":
                def returnType = subType ?: rawValue.class.typeName
                value = "{${wrapValueBasedOnType(rawValue, returnType, fallback)}}"
                break
                // TODO:
            case "Callable":
                def returnType = subType ?: rawValue.class.typeName
                value = "new java.util.concurrent.Callable<${returnType}>() {@Override ${returnType} call() throws Exception {${wrapValueBasedOnType(rawValue, returnType, fallback)}}"
                break
            case "Object":
                value = "new Object() {@Override String toString() { ${wrapValueBasedOnType(rawValue, "String", fallback)} }}"
                break
            case "Directory":
                value = "project.layout.projectDirectory.dir(${wrapValueBasedOnType(rawValue, "String", fallback)})"
                break
            case "RegularFile":
                value = "project.layout.projectDirectory.file(${wrapValueBasedOnType(rawValue, "String", fallback)})"
                break
            case "Provider":
                switch (subType) {
                    case "RegularFile":
                        value = "project.layout.buildDirectory.file(${wrapValueBasedOnType(rawValue, "Provider<String>", fallback)})"
                        break
                    case "Directory":
                        value = "project.layout.buildDirectory.dir(${wrapValueBasedOnType(rawValue, "Provider<String>", fallback)})"
                        break
                    default:
                        value = "project.provider(${wrapValueBasedOnType(rawValue, "Closure<${subType}>", fallback)})"
                        break
                }
                break
                // TODO: ...
            case "java.lang.String":
            case "String":
                value = "'${escapedPath(rawValue.toString())}'"
                break
                // TODO: Assumes that the raw value is a collection, no auto-conversion to collection
            case "[]":
                value = "${wrapValueBasedOnType(rawValue, "List<${subType}>", fallback)} as ${subType}[]"
                break
                // TODO: Assumes that the raw value is a collection, no auto-conversion to collection
            case "...":
                value = "${rawValue.collect { wrapValueBasedOnType(it, subType, fallback) }.join(", ")}"
                break
            case "File":
                value = "new File(${wrapValueBasedOnType(rawValue, String.class, fallback)})"
                break
                // TODO: Assumes that the raw value is a collection, no auto-conversion to collection
            case "java.util.ArrayList":
            case "List":
                def returnType = subType ?: String.class.typeName
                // If the value is not a list itself, add it to a list
                if (rawValue instanceof List) {
                    value = "[${rawValue.collect { wrapValueBasedOnType(it, returnType, fallback) }.join(", ")}]"
                } else {
                    value = "[${wrapValueBasedOnType(rawValue, returnType, fallback)}]"
                }

                break
                // TODO: Assumes that the raw value is a collection, no auto-conversion to collection
            case "Map":
                if (subType) {
                    def split = subType.split(",", 2).collect { it.trim() }
                    def keyType = split.first()
                    def valueType = split.last()
                    value = "[" + rawValue.collect { k, v -> "${wrapValueBasedOnType(k, keyType, fallback)} : ${wrapValueBasedOnType(v, valueType, fallback)}" }.join(", ") + "]"
                } else {
                    value = "[" + rawValue.collect { k, v -> "${wrapValueBasedOnType(k, k.getClass(), fallback)} : ${wrapValueBasedOnType(v, v.getClass(), fallback)}" }.join(", ") + "]"
                }
                value = value == "[]" ? "[:]" : value
                break
            default:
                if (fallback) {
                    fallback.setDelegate(this)
                    if (fallback.getMaximumNumberOfParameters() == 1) {
                        value = fallback(type)
                    } else if (fallback.getMaximumNumberOfParameters() == 3) {
                        value = fallback(rawValue, type, fallback)
                    }
                } else {
                    value = rawValue
                }
        }
        value
    }

    @Incubating
    static String serializeValueToEnvironment(Object rawValue, String type, Closure<String> fallback = null) {

        String value
        def match = StringTypeMatch.match(type)
        type = match.mainType
        def subType = match.subType

        switch (type) {
            case "[]":
                value = "${serializeValueToEnvironment(rawValue, "List<${subType}>", fallback)} as ${subType}[]"
                break
            case "...":
                value = "${rawValue.collect { serializeValueToEnvironment(it, subType, fallback) }.join(",")}"
                break
            case "java.util.ArrayList":
            case "List":
                def returnType = subType ?: String.class.typeName
                value = "${rawValue.collect { serializeValueToEnvironment(it, returnType, fallback) }.join(",")}"
                break
            default:
                if (fallback) {
                    fallback.setDelegate(this)
                    if (fallback.getMaximumNumberOfParameters() == 1) {
                        value = fallback(type)
                    } else if (fallback.getMaximumNumberOfParameters() == 3) {
                        value = fallback(rawValue, type, fallback)
                    }
                } else {
                    value = rawValue
                }
        }
        value
    }
}
