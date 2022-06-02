package com.wooga.gradle.test.serializers

import com.wooga.gradle.test.IntegrationHandler
import com.wooga.gradle.test.StringTypeMatch

import java.util.function.BiFunction
import java.util.function.Function

/**
 * A function for processing an object of a given type during an integration test
 */
interface IntegrationObjectProcessorFunction extends BiFunction<Object, IntegrationHandler, String> {
}

/**
 * An object which holds processing functions for objects of given types, which are to be processed during
 * an integration test
 */
class IntegrationObjectProcessor extends HashMap<String, IntegrationObjectProcessorFunction> {
    Map<String, IntegrationObjectProcessorFunction> values = new HashMap<String, IntegrationObjectProcessorFunction>()

    void put(Class type, IntegrationObjectProcessorFunction function) {
        put(type.simpleName, function)
    }

    public <T> void transform(Class type, Function<T, T> transformation) {
        IntegrationObjectProcessorFunction function = { Object value, IntegrationHandler integration ->
            transformation.apply((T) value)
        }
        put(type.simpleName, function)
    }

    IntegrationObjectProcessorFunction get(Class type) {
        get(type.simpleName)
    }

    Object process(Object value,
                   String typeName,
                   IntegrationHandler integration) {

        StringTypeMatch typeMatch = StringTypeMatch.match(typeName)

        // If it's a list type
        if (typeMatch.mainType == "List") {
            // If the given value is a list
            if (value instanceof List) {
                value = value.collect({ process(it, typeMatch.subType, integration) })
            } else {
                value = [process(value, typeMatch.subType, integration)]
            }
        }
        // Otherwise if it's not a list, preprocess the value
        else {
            if (containsKey(typeName)) {
                def function = this[typeName]
                try {
                    value = function.apply(value, integration)
                }
                catch (Exception e) {
                    throw new Exception("Failed to invoke the processing function for type ${typeName} ->\n${e}")
                }

            }
        }
        value
    }

    Object process(Object value,
                   Class type,
                   IntegrationHandler integration) {
        process(value, type.simpleName, integration)
    }
}
