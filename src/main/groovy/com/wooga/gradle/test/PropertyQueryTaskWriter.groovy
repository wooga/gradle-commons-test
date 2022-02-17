package com.wooga.gradle.test

import groovy.transform.InheritConstructors
import nebula.test.functional.ExecutionResult
import org.omg.CORBA.Environment

/**
 * Writes a task for querying the value of a property
 * from a given path with the specified invocation.
 */
abstract class BasePropertyQueryTaskWriter {

    String path
    String invocation
    String taskName

    String getTaskName() {
        taskName
    }

    /**
     * Writes the task that will print the given query
     */
    abstract void write(File file);

    BasePropertyQueryTaskWriter(String path, String invocation = ".getOrNull()", String taskName = null) {
        this.path = path
        this.invocation = invocation
        this.taskName = taskName ?: PropertyUtils.toCamelCase("query_${path}")
    }
}

/**
 * Writes a task for querying the value of a property
 * from a given path with the specified invocation.
 */
@InheritConstructors
class PropertyQueryTaskWriter extends BasePropertyQueryTaskWriter {

    final String separator = " : "
    final String pattern = "${path}${separator}"

    void write(File file) {
        file << """
            task(${taskName}) {
                doLast {
                    def value = ${path}${invocation}
                    println("${path}${separator}" + value)
                }
            }
        """.stripIndent()
    }

    /**
     * @return True if the property's toString() equals the given value
     */
    Boolean matches(ExecutionResult result, Object value) {
        result.standardOutput.contains("${path}${separator}${value}")
    }

    /**
     * @return True if the boolean property returns true
     */
    Boolean isTrue(ExecutionResult result) {
        result.standardOutput.contains("${path}${separator}true")
    }

    /**
     * @return True if the boolean property returns false
     */
    Boolean isFalse(ExecutionResult result) {
        result.standardOutput.contains("${path}${separator}false")
    }

    /**
     * @return True if the property is null
     */
    Boolean isNull(ExecutionResult result) {
        result.standardOutput.contains("${path}${separator}null")
    }

    /**
     * @return True if the property is not null
     */
    Boolean isNotNull(ExecutionResult result) {
        !isNull(result)
    }

    /**
     * @return True if the property's toString contains the given substring
     */
    Boolean contains(ExecutionResult result, String substring) {
        def value = getValue(result)
        value.contains(substring)
    }

    /**
     * @return The line that contains the results of the query
     */
    String getLine(ExecutionResult result){
        int startIndex = result.standardOutput.indexOf(pattern)
        int endIndex = result.standardOutput.indexOf(System.lineSeparator(), startIndex)
        result.standardOutput.substring(startIndex, endIndex).trim()
    }

    /**
     * @return The value of the property
     */
    String getValue(ExecutionResult result){
        def line = getLine(result)
        line.replace(pattern, "").trim()
    }
}

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
