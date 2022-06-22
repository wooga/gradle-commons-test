package com.wooga.gradle.test.queries


import com.wooga.gradle.test.IntegrationHandler
import com.wooga.gradle.test.IntegrationSpec
import com.wooga.gradle.test.PropertyUtils

import java.util.function.Function

/**
 * Used during tests to specify a raw (input) value and the expected (output) value
 */
class TestValue {

    private Object _value
    private Object _expected
    private String _message

    private Function<Object, String> onWriteRawValue
    private Function<Object, Object> onGenerateExpected
    private Function<IntegrationHandler, Object> onProcessValue
    private Function<IntegrationHandler, Object> onProcessExpectedValue

    /**
     * The value that will be written/set.
     */
    Object getRaw() {
        if (onWriteRawValue != null) {
            return onWriteRawValue.apply(_value)
        }
        _value
    }

    /**
     * The expected value (usually the raw value)
     */
    Object getExpected() {

        // If an expected value was directly set, prioritize that
        if (_expected != null) {
            return _expected
        }

        // If a function to transform the raw value onto the expected was provided...
        if (onGenerateExpected != null) {
            return onGenerateExpected.apply(_value)
        }

        // Return the default value
        _value
    }

    /**
     * @return True if the expected value was inherited from the raw value
     */
    Boolean getInherited() {
        _expected == null
    }

    @Override
    String toString() {
        String res = "\"${raw}\""
        if (_expected != null) {
            res += " expecting \"${expected}\""
        }
        if (_message != null) {
            res += " ${_message}"
        }
        res
    }

    //------------------------------------------------------------------------/
    // Constructor
    //------------------------------------------------------------------------/
    TestValue(Object value) {
        this._value = value
    }

    //------------------------------------------------------------------------/
    // Static Constructors
    //------------------------------------------------------------------------/
    /**
     * @return A test value with a value directly set
     */
    static TestValue set(Object raw) {
        new TestValue(raw)
    }

    /**
     * @return A test value where raw = "null', expected = null
     */
    static TestValue none() {
        new TestValue(null)
    }

    static TestValue list(List<Object> values) {
        set(values)
            .write({ List<Object> it -> "[${it.join(",")}]" })
    }

    static TestValue list(Object... values) {
        set(values)
            .write({ List<Object> it -> "[${it.join(",")}]" })
    }

    static TestValue list(Object value) {
        list([value])
    }

    static TestValue list(Function<Object, Object> preprocess, Object... values) {
        list(values.collect({ it -> preprocess(it) }))
    }

    static TestValue filePath(String path, Boolean wrap = false) {
        set(IntegrationSpec.osPath(path))
            .write({ String s ->
                wrap ? PropertyUtils.wrapValueBasedOnType(s, String) : s
            })
    }

    static TestValue filePaths(String... paths) {
        def values = paths.collect({ IntegrationSpec.osPath(it) })
        set(values)
            .write({ List<String> p ->
                p.collect({ it -> PropertyUtils.wrapValueBasedOnType(it, String) })
            })
    }

    static TestValue filePaths(List<String> paths) {
        filePaths(*paths)
    }

    static TestValue joinFilePaths(List<String> values, String separator = ",") {
        join(values.collect({ new File(it).path }), separator)
    }

    static TestValue join(List<String> values, String separator = ",") {
        set(values)
            .write({ List<String> r -> r.join(separator) })
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
        onProcessValue = func
        this
    }

    /**
     * Sets a function that will specify how to write the raw value
     */
    TestValue write(Function<Object, String> func) {
        onWriteRawValue = func
        this
    }

    /**
     * If there's an evaluation function set, evaluates the current raw value
     */
    TestValue evaluate(IntegrationHandler integration) {
        if (onProcessValue != null) {
            _value = onProcessValue.apply(integration)
        }
        if (onProcessExpectedValue != null) {
            _expected = onProcessExpectedValue.apply(integration)
        }
        this
    }

    //------------------------------------------------------------------------/
    // Transformers
    //------------------------------------------------------------------------/
    /**
     * Appends additional information to be printed when toString() is invoked
     */
    TestValue describe(String message) {
        this._message = message
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
        this._expected = value
        this
    }

    /**
     * Sets the expected value according to a mapping procedure
     * @param closure A procedure that will map the value
     */
    TestValue expect(Function<Object, Object> closure) {
        onGenerateExpected = closure
        this
    }

    /**
     * Sets the expected value according to a mapping procedure
     * @param closure A procedure that will map the raw value
     */
    TestValue expect(Closure<Object> closure) {
        onGenerateExpected = closure
        this
    }

    /**
     * Sets the expected value according to one generated from the integration
     * @param function A function that uses the integration to generate a value
     */
    TestValue expectFromIntegration(Function<IntegrationHandler, Object> function) {
        onProcessExpectedValue = function
        this
    }

    /**
     * Sets the expected value according to one generated from the integration
     * @param function The path to the file, relative to the project directory
     */
    TestValue expectProjectFile(String path) {
        expectFromIntegration({ IntegrationHandler it -> new File(it.projectDir, path) })
    }

    /**
     * Sets the expected value according to one generated from the integration (from the currently set raw value)
     */
    TestValue expectAsProjectFile() {
        expectProjectFile((String) _value)
    }

    /**
     * When the expected value is to be a list
     * @param separator Between each value
     */
    TestValue expectList(String separator = ",") {
        // Add a whitespace after each separator
        expect("[${raw.toString().replaceAll(/${separator}/, "${separator} ")}]")
    }

    /**
     * When the expected value contains an additional element at the start
     */
    TestValue expectPrepend(Object other) {
        def newValue = [other]

        def current = _value

        if (current instanceof Object[]) {
            def list = current as Object[]
            newValue.addAll(list)
        } else if (current instanceof ArrayList) {
            def arrayList = current as ArrayList
            newValue.addAll(arrayList)
        } else {
            newValue << expected
        }

        expect(newValue)
    }
}
