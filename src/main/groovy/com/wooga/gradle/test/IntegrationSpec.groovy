/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.wooga.gradle.test

import nebula.test.functional.ExecutionResult
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.ProvideSystemProperty

import static com.wooga.gradle.PlatformUtils.escapedPath
import static com.wooga.gradle.PlatformUtils.windows

class IntegrationSpec extends nebula.test.IntegrationSpec {

    @Rule
    ProvideSystemProperty properties = new ProvideSystemProperty("ignoreDeprecations", "true")

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()


    def setup() {
        environmentVariables.clear()
    }

    static String osPath(String path) {
        if (isWindows()) {
            path = path.startsWith('/') ? "c:" + path : path
        }
        new File(path).path
    }

    Boolean fileExists(String... path) {
        fileExists(path.join("/"))
    }

    Boolean outputContains(ExecutionResult result, String message) {
        result.standardOutput.contains(message) || result.standardError.contains(message)
    }

    String wrapValueBasedOnType(Object rawValue, Class type, Closure<String> fallback = null) {
        wrapValueBasedOnType(rawValue, type.simpleName, fallback)
    }

    // TODO: To be deprecated in the future by a better implementation
    String wrapValueBasedOnType(Object rawValue, String type, Closure<String> fallback = null) {
        def value
        def subType = null

        if (type.endsWith("...") || type.endsWith("[]")) {
            def parts = type.split(/(\.\.\.|\[\])/)
            subType = parts.first()
            type = type.endsWith("...") ? "..." : "[]"
        } else {
            def subtypeMatches = type =~ /(?<mainType>\w+)(<(?<subType>.*?)>)?/
            subType = (subtypeMatches.matches()) ? subtypeMatches.group("subType") : null
            type = (subtypeMatches.matches()) ? subtypeMatches.group("mainType") : type
        }

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
                value = "project.layout.projectDirectory.dir(${wrapValueBasedOnType(rawValue, String, fallback)})"
                break
            case "RegularFile":
                value = "project.layout.projectDirectory.file(${wrapValueBasedOnType(rawValue, String, fallback)})"
                break
            case "Provider":
                switch (subType) {
                    case "RegularFile":
                        value = "project.layout.buildDirectory.file(${wrapValueBasedOnType(rawValue, String, fallback)})"
                        break
                    case "Directory":
                        value = "project.layout.buildDirectory.dir(${wrapValueBasedOnType(rawValue, String, fallback)})"
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
                value = "[${rawValue.collect { wrapValueBasedOnType(it, returnType, fallback) }.join(", ")}]"
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
}





