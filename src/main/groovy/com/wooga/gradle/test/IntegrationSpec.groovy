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

import com.wooga.gradle.PlatformUtils
import nebula.test.functional.ExecutionResult
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.ProvideSystemProperty

import java.nio.file.Files

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
        return PropertyUtils.wrapValueBasedOnType(rawValue, type, fallback)
    }

    static File generateBatchWrapper(String fileName, Boolean printEnvironment = false) {
        BatchmodeWrapper wrapper = new BatchmodeWrapper(fileName)
        wrapper.printEnvironment = printEnvironment
        wrapper.toTempFile()
    }
}





