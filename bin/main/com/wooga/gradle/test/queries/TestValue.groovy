package com.wooga.gradle.test.queries


import com.wooga.gradle.test.IntegrationHandler

import java.util.function.Function

/**
 * Used during tests to specify a raw (input) value and the expected (output) value
 */
class TestValue {

    private Function<IntegrationHandler, Object> onProcessRawValue
    private Function<IntegrationHandler, Object> onProcessExpectedValue
    private Object expected
    private Object raw
    private String message

    /**
     * The value that was used to set
     */
    Object getRaw() {
        raw
    }

    /**
     * The expected value (usually the raw value)
     */
    Object getExpected() {
        if (expected != null) {
            return expected
        }
        raw
    }

    /**
     * @return True if the expected value was inherited from the raw value
     */
    Boolean getInherited() {
        expected == null
    }


    //------------------------------------------------------------------------/
    // Constructor
    //------------------------------------------------------------------------/
    TestValue(Object raw) {
        this.raw = raw
    }

    @Override
    String toString() {
        String res = "\"${raw}\""
        if (expected != null) {
            res += " expecting \"${expected}\""
        }
        if (message != null) {
            res += " ${message}"
        }
        res
    }

    //------------------------------------------------------------------------/
    // Static Constructors
    //------------------------------------------------------------------------/
    static TestValue set(Object raw) {
        new TestValue(raw)
    }

    static TestValue none() {
        new TestValue(null)
    }

    static TestValue list(Object... values) {
        String raw = "[${values.join(",")}]"
        set(raw)
    }

    static TestValue list(Function<Object, Object> preprocess, Object... values) {
        list(values.collect({ it -> preprocess(it) }))
    }

    static TestValue filePath(String path) {
        set(new File(path).path)
    }

    static TestValue join(List<String> values, String separator = ",") {
        String raw = values.join("${separator}")
        set(raw)
    }

    static TestValue joinFilePaths(List<String> values, String separator = ",") {
        join(values.collect({ new File(it).path }), separator)
    }

    static TestValue projectFile(String path) {
        set(path).process({ it -> new File(it.projectDir, path) })
    }

    static TestValue projectBuildFile(String path) {
        projectFile("build/" + path)
    }

    //------------------------------------------------------------------------/
    // Evaluation
    //------------------------------------------------------------------------/
    /**
     * Sets an evaluation function that will modify the raw value
     */
    TestValue process(Function<IntegrationHandler, Object> func) {
        onProcessRawValue = func
        this
    }

    /**
     * If there's an evaluation function set, evaluates the current raw value
     */
    TestValue evaluate(IntegrationHandler integration) {
        if (onProcessRawValue != null) {
            raw = onProcessRawValue.apply(integration)
        }
        if (onProcessExpectedValue != null) {
            expected = onProcessExpectedValue.apply(integration)
        }
        this
    }

    //------------------------------------------------------------------------/
    // Transformers
    //------------------------------------------------------------------------/
    /**
     * Sets this value as if it were a list
     */
    TestValue asList() {
        set("[${raw}]")
        this
    }

    /**
     * Appends additional information to be printed when toString() is invoked
     */
    TestValue describe(String message) {
        this.message = message
        this
    }

    //------------------------------------------------------------------------/
    // Expected
    //------------------------------------------------------------------------/
    /**
     * Sets the expected value
     * @param value The value that will be expected, differing from the input
     */
    TestValue expect(Object value) {
        this.expected = value
        this
    }

    /**
     * Sets the expected value according to a mapping procedure
     * @param closure A procedure that will map the value
     */
    TestValue expect(Function<Object, Object> closure) {
        expect(closure.apply(raw))
        this
    }

    /**
     * Sets the expected value according to one generated from the integration
     * @param function A function that uses the integration to generate a value
     */
    TestValue expectIntegration(Function<IntegrationHandler, Object> function) {
        onProcessExpectedValue = function
        this
    }

    /**
     * Sets the expected value according to one generated from the integration
     * @param function The path to the file, relative to the project directory
     */
    TestValue expectProjectFile(String path) {
        expectIntegration({ IntegrationHandler it -> new File(it.projectDir, path) })
    }

    /**
     * Sets the expected value according to one generated from the integration (from the currently set raw value)
     */
    TestValue expectAsProjectFile() {
        expectProjectFile((String) raw)
    }

    /**
     * Sets the expected value according to a mapping procedure
     * @param closure A procedure that will map the value
     */
    TestValue expect(Closure<Object> closure) {
        expect(closure(raw))
        this
    }

    /**
     * When the expected value is to be a list
     * @param separator Between each value
     */
    TestValue expectList(String separator = ",") {
        // Add a whitespace after each separator
        expect("[${raw.toString().replaceAll(/${separator}/, "${separator} ")}]")
    }
}
