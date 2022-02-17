package com.wooga.gradle.test.writers

import com.wooga.gradle.test.queries.TestValue

class ScriptPropertyEvaluation extends PropertyEvaluation {

    @Override
    Boolean evaluateTrue(String pattern, String stdout) {
        stdout.contains("${pattern}true")
    }

    @Override
    Boolean evaluateFalse(String pattern, String stdout) {
        stdout.contains("${pattern}false")
    }

    @Override
    Boolean evaluateNull(String pattern, String stdout) {
        stdout.contains("${pattern}null")
    }

    @Override
    Boolean evaluateNotNull(String pattern, String stdout) {
        !evaluateNull(pattern, stdout)
    }

    @Override
    PropertyComparison compare(String pattern, String stdout, TestValue testValue, String typeName) {
        def actual = getValue(pattern, stdout)
        def expectedValue = processExpectedValue(testValue, typeName)
        def expected = expectedValue.toString()
        new PropertyComparison(expected, actual)
    }
}
