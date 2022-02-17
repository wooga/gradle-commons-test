package com.wooga.gradle.test

import com.wooga.gradle.test.writers.BasePropertyQueryTaskWriter
import groovy.transform.InheritConstructors
import nebula.test.functional.ExecutionResult

/**
 * Writes a task for querying the value of a {@code Map} type property
 * from a given path with the specified invocation.
 * It contains functions for testing whether the map contains certaion values.
 */
@InheritConstructors
class MapPropertyQueryTaskWriter extends BasePropertyQueryTaskWriter {

    final String keyword = "contains"

    void write(File file) {
        file << """
            task(${taskName}) {
                doLast {
                    def value = ${path}${invocation}
                    for(v in value) {
                        println("${path} ${keyword} " + v)
                    }
                }
            }
        """.stripIndent()
    }

    /**
     * @return True if the map contains the given key-value pairs
     */
    Boolean contains(ExecutionResult result, Map.Entry<String, ?>... entries) {
        for (e in entries) {
            if (!result.standardOutput.contains("${path} ${keyword} ${e}"))
                return false
        }
        return true
    }

    /**
     * @return True if the map contains the values of the given maps
     */
    Boolean contains(ExecutionResult result, Map<String, ?>... maps) {
        for (m in maps) {
            for (e in m) {
                if (!contains(result, e))
                    return false
            }
        }
        return true
    }


}
