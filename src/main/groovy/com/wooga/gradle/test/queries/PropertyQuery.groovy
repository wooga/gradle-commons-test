package com.wooga.gradle.test.queries

import com.wooga.gradle.test.IntegrationHandler
import com.wooga.gradle.test.writers.PropertyEvaluation
import nebula.test.functional.ExecutionResult
import org.apache.commons.io.FileUtils

/**
 * Used for evaluating the value of a property that was fetched by a script invocation
 */
class PropertyQuery {

    //--------------------------------------------------------------------------------------------/
    // Fields
    //--------------------------------------------------------------------------------------------/
    final ExecutionResult result
    final Boolean success
    final String pattern
    final IntegrationHandler integration
    final PropertyEvaluation evaluation
    private final String standardOutput

    String typeName
    Boolean enableAssertions = true

    //--------------------------------------------------------------------------------------------/
    // Constructor
    //--------------------------------------------------------------------------------------------/
    PropertyQuery(String pattern,
                  IntegrationHandler integration,
                  String standardOutput,
                  PropertyEvaluation evaluation,
                  Boolean success = true) {
        this.result = result
        this.standardOutput = standardOutput
        this.pattern = pattern
        this.integration = integration
        this.evaluation = evaluation
        this.success = success
        this.typeName = ""
    }

    //--------------------------------------------------------------------------------------------/
    // Evaluation
    //--------------------------------------------------------------------------------------------/
    /**
     * Asserts that the property has the expected value
     * @param expected The value to be expected
     */
    Boolean matches(Object expected) {
        matches(expected, typeName)
    }

    /**
     * @return True if the boolean property returns true
     */
    Boolean isTrue() {
        evaluation.evaluateTrue(pattern, standardOutput, enableAssertions)
    }

    /**
     * @return True if the boolean property returns false
     */
    Boolean isFalse() {
        evaluation.evaluateFalse(pattern, standardOutput, enableAssertions)
    }

    /**
     * @return True if the property is null
     */
    Boolean isNull() {
        evaluation.evaluateNull(pattern, standardOutput, enableAssertions)
    }

    /**
     * @return True if the property is not null
     */
    Boolean isNotNull() {
        evaluation.evaluateNotNull(pattern, standardOutput, enableAssertions)
    }

    /**
     * @return True if the property's toString contains the given substring
     */
    Boolean contains(String substring) {
        evaluation.contains(pattern, standardOutput, substring, enableAssertions)
    }

    /**
     * @return True if the property's toString ends with the given relative path
     */
    Boolean endsWithPath(String path) {
        evaluation.endsWithPath(pattern, standardOutput, path, enableAssertions)
    }

    /**
     * @return True if the file property's path matches that of the given path (relative to the project root)
     */
    Boolean matchesPath(String path) {
        evaluation.matchesPath(pattern, standardOutput, integration.projectDir, path, enableAssertions)
    }

    /**
     * @return The line that contains the results of the query
     */
    String getLine() {
        evaluation.getLine(pattern, standardOutput)
    }

    /**
     * @return The value of the property, as retrieved from the invoked property getter
     */
    String getValue() {
        evaluation.getValue(pattern, standardOutput)
    }

    /**
     * @return True if the property's toString() equals the given value, with special handling for custom types
     */
    Boolean matches(TestValue value, String typeName) {
        // Evaluate the value before checking for a match
        value.evaluate(integration)
        evaluation.matches(pattern, standardOutput, value, typeName, enableAssertions)
    }

    /**
     * @return True if the property's toString() equals the given value, with special handling for custom types
     */
    Boolean matches(Object expectedValue, String typeName) {
        matches(TestValue.set(expectedValue), typeName)
    }

    /**
     * @return True if the property's toString() equals the given value, with special handling for custom types
     */
    Boolean matches(Object expected, Class type) {
        matches(expected, type.simpleName)
    }

    /**
     * @return True if the property's toString() equals the given value, with special handling for custom types
     */
    Boolean matches(TestValue value, Class type) {
        matches(value, type.simpleName)
    }

    //--------------------------------------------------------------------------------------------/
    // Configuration
    //--------------------------------------------------------------------------------------------/
    /**
     * Disables assertions during the evaluation methods
     */
    PropertyQuery withoutAssertions() {
        this.enableAssertions = false
        this
    }

    /**
     * Directly associates this query's value with a given type
     */
    PropertyQuery associate(String typeName) {
        this.typeName = typeName
        this
    }

    /**
     * Adds a custom serializer that will be used when trying to match an expected value with a specific type
     */
    PropertyQuery withSerializer(Class type, Closure<String> serializer) {
        evaluation.withSerializer(type.simpleName, serializer)
        this
    }

    /**
     * Adds a custom serializer that will be used when trying to match an expected value with a specific type
     */
    PropertyQuery withSerializer(String typeName, Closure<String> serializer) {
        evaluation.withSerializer(typeName, serializer)
        this
    }

    /**
     * Adds a matcher that will treat all Files as starting from the project root
     */
    PropertyQuery withFilePathsRelativeToProject() {
        withSerializer(File, {
            def relativePath = (String) it
            new File(integration.projectDir, relativePath).path
        })
    }

    /**
     * Adds a matcher that will treat all paths from regular file providers as starting from the project root + dir
     */
    PropertyQuery withFileProvidersRelativeTo(String dir) {
        withSerializer("Provider<RegularFile>", {
            def relativePath = (String) it
            FileUtils.getFile(integration.projectDir, dir, relativePath).path
        })
    }
}
