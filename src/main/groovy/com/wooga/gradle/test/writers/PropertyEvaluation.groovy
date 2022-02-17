package com.wooga.gradle.test.writers

import com.wooga.gradle.test.IntegrationSpec
import com.wooga.gradle.test.queries.TestValue

abstract class PropertyEvaluation {

    /**
     * Custom seriaolizers
     */
    protected Map<String, Closure<String>> serializers = new HashMap<String, Closure<String>>()

    /**
     * @return True if the given boolean property is true
     */
    abstract Boolean evaluateTrue(String pattern, String stdout)

    /**
     * @return True if the given boolean property is false
     */
    abstract Boolean evaluateFalse(String pattern, String stdout)

    /**
     * @return True if the given property is null
     */
    abstract Boolean evaluateNull(String pattern, String stdout)

    /**
     * @return True if the given property is not null
     */
    abstract Boolean evaluateNotNull(String pattern, String stdout)

    /**
     * @return The actual (what is present) and expected values of the given property
     */
    abstract PropertyComparison compare(String pattern, String stdout, TestValue testValue, String typeName)

    /**
     * @return True if the property's toString contains the given substring
     */
    Boolean contains(String pattern, String stdout, String substring, Boolean shouldAssert = false) {
        def value = getValue(pattern, stdout)
        if (shouldAssert) {
            assert value.contains(substring)
        }
        value.contains(substring)
    }

    /**
     * @return True if the property's path ends with the given relative path
     */
    Boolean endsWithPath(String pattern, String stdout, String path, Boolean shouldAssert = false) {
        def value = getValue(pattern, stdout)
        // TODO: Perhaps just use new File().path instead?
        def evaluation = value.endsWith(IntegrationSpec.osPath(path))
        if (shouldAssert) {
            assert evaluation: "'${nicifyPatterName(pattern)}' did not end with path '${path}' -> ${value}"
        }
        evaluation
    }

    /**
     * @return True if the file property's path matches that of the given path (relative to the project root)
     */
    Boolean matchesPath(String pattern, String stdout, File projectDir, String path, Boolean shouldAssert = false) {
        def value = getValue(pattern, stdout)
        def expected = new File(projectDir, path)
        if (shouldAssert) {
            assert value == expected.path
        }
        value == expected.path
    }

    Boolean matches(String pattern, String stdout, TestValue testValue, String typeName, Boolean shouldAssert) {
        def comparison = compare(pattern, stdout, testValue, typeName)
        matches(comparison.expected, comparison.actual, shouldAssert)
    }

    /**
     * @return True if the expected value matches the actual
     */
    static Boolean matches(String expected, String actual, Boolean shouldAssert) {
        if (shouldAssert) {
            assert expected == actual
        }
        expected == actual
    }

    /**
     * @return True if the boolean property returns true
     */
    Boolean evaluateTrue(String pattern, String stdout, Boolean shouldAssert) {
        def evaluation = evaluateTrue(pattern, stdout)
        if (shouldAssert) {
            assert evaluation: "${nicifyPatterName(pattern)} should be true"
        }
        evaluation
    }

    /**
     * @return True if the boolean property returns false
     */
    Boolean evaluateFalse(String pattern, String stdout, Boolean shouldAssert) {
        def evaluation = evaluateFalse(pattern, stdout)
        if (shouldAssert) {
            assert evaluation: "${nicifyPatterName(pattern)} should be false"
        }
        evaluation
    }

    /**
     * @return True if the property is null
     */
    Boolean evaluateNull(String pattern, String stdout, Boolean shouldAssert) {
        def evaluation = evaluateNull(pattern, stdout)
        if (shouldAssert) {
            assert evaluation: "${nicifyPatterName(pattern)} should be null"
        }
        evaluation
    }

    /**
     * @return True if the property is not null
     */
    Boolean evaluateNotNull(String pattern, String stdout, Boolean shouldAssert) {
        def evaluation = evaluateNotNull(pattern, stdout)
        if (shouldAssert) {
            assert evaluation: "${nicifyPatterName(pattern)} is null when it should not be"
        }
        evaluation
    }

    /**
     * @return The value of the property
     */
    static String getValue(String pattern, String stdout) {
        def line = getLine(pattern, stdout)
        line.replace(pattern, "").trim()
    }

    /**
     * @return The line that contains the results of the query
     */
    static String getLine(String pattern, String stdout) {
        // TODO: Provide functionality to only use the stdout subset of the task execution
        int startIndex = stdout.indexOf(pattern)
        int endIndex = stdout.indexOf(System.lineSeparator(), startIndex)
        stdout.substring(startIndex, endIndex).trim()
    }

    /**
     * @return a formatted version of the pattern
     */
    static String nicifyPatterName(String pattern) {
        def name = pattern.replaceAll(/[:=]/, "").trim()
        "The property '${name}'"
    }

    /**
     * If custom serializers are present, serializes the given value
     */
    protected Object processExpectedValue(Object expectedValue, String typeName) {
        if (typeName != null) {
            if (serializers.containsKey(typeName)) {
                def func = serializers[typeName]
                expectedValue = func(expectedValue)
            }
        }
        expectedValue
    }

    /**
     * If custom serializers are present and the test value was inherited from the raw value,
     * serializes the given value
     */
    protected Object processExpectedValue(TestValue testValue, String typeName) {
        testValue.inherited ? processExpectedValue(testValue.expected, typeName) : testValue.expected
    }

    /**
     * Adds a custom serializer that will be used when trying to match an expected value with a specific type
     */
    PropertyEvaluation withSerializer(Class type, Closure<String> serializer) {
        serializers.put(type.simpleName, serializer)
        this
    }

    /**
     * Adds a custom serializer that will be used when trying to match an expected value with a specific type
     */
    PropertyEvaluation withSerializer(String typeName, Closure<String> serializer) {
        serializers.put(typeName, serializer)
        this
    }

    /**
     * Adds a custom serializer that will be used when trying to match an expected value with a specific type
     */
    PropertyEvaluation withSerializers(Map<String, Closure<String>> other) {
        serializers.putAll(other)
        this
    }
}

class PropertyComparison {
    final String actual
    final String expected

    PropertyComparison(String expected, String actual) {
        this.actual = actual
        this.expected = expected
    }
}


