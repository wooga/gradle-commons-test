package com.wooga.gradle.test

import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import groovy.transform.InheritConstructors
import nebula.test.functional.ExecutionResult

/**
 * Legacy class used in the older API
 */
@InheritConstructors
class PropertyQueryTaskWriter extends PropertyGetterTaskWriter {

    //------------------------------------------------------------------------/
    // Evaluation functions (Legacy API)
    //------------------------------------------------------------------------/
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
        evaluation.evaluateTrue(pattern, result.standardOutput, false)
    }

    /**
     * @return True if the boolean property returns false
     */
    Boolean isFalse(ExecutionResult result) {
        evaluation.evaluateFalse(pattern, result.standardOutput, false)
    }

    /**
     * @return True if the property is null
     */
    Boolean isNull(ExecutionResult result) {
        evaluation.evaluateNull(pattern, result.standardOutput, false)
    }

    /**
     * @return True if the property is not null
     */
    Boolean isNotNull(ExecutionResult result) {
        evaluation.evaluateNotNull(pattern, result.standardOutput, false)
    }

    /**
     * @return True if the property's toString contains the given substring
     */
    Boolean contains(ExecutionResult result, String substring) {
        evaluation.contains(pattern, result.standardOutput, substring, false)
    }

    /**
     * @return The line that contains the results of the query
     */
    String getLine(ExecutionResult result) {
        evaluation.getLine(pattern, result.standardOutput)
    }

    /**
     * @return The value of the property
     */
    String getValue(ExecutionResult result) {
        evaluation.getValue(pattern, result.standardOutput)
    }
}
