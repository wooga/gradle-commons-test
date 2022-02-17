package com.wooga.gradle.test.writers

import com.wooga.gradle.test.queries.TestValue

class CommandLinePropertyEvaluation extends PropertyEvaluation {

    String assignmentOperator = "="

    @Override
    Boolean evaluateTrue(String pattern, String stdout) {
        stdout.contains("${pattern}")
    }

    @Override
    Boolean evaluateFalse(String pattern, String stdout) {
        !stdout.contains("${pattern}")
    }

    @Override
    Boolean evaluateNull(String pattern, String stdout) {
        evaluateTrue(pattern, stdout)
    }

    @Override
    Boolean evaluateNotNull(String pattern, String stdout) {
        evaluateFalse(pattern, stdout)
    }

    @Override
    PropertyComparison compare(String pattern, String stdout, TestValue testValue, String typeName) {

        String actual = null

        // Special case for booleans since the presence a command line flag means the property is set to true, false otherwise
        if (typeName == "Boolean") {
            actual = ((Boolean) testValue.expected) == evaluateTrue(pattern, stdout)
        }
        // Other types
        else {
            actual = getValue("${pattern}${assignmentOperator}", stdout)
        }

        def expectedValue = processExpectedValue(testValue, typeName)
        new PropertyComparison(expectedValue.toString(), actual)
    }
}
