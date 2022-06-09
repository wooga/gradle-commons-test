package com.wooga.gradle.test

import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertyWrite
import groovyjarjarcommonscli.MissingArgumentException
import org.spockframework.lang.Wildcard

import static com.wooga.gradle.test.PropertyUtils.envNameFromProperty

/**
 * The location used to set/get a gradle project's particular property
 */
abstract class PropertyLocation {

    abstract String reason()

    abstract PropertyWrite write(PropertySetArguments args)

    @Override
    String toString() {
        return reason()
    }

    static PropertyLocation none = new NonePropertyLocation()
    static PropertyLocation script = new BuildScriptPropertyLocation()
    static PropertyLocation property = new PropertiesFilePropertyLocation()
    static PropertyLocation environment = new EnvironmentPropertyLocation()
    static PropertyLocation commandLine = new CommandLineOptionPropertyLocation()

    static PropertyLocation valueOf(String name) {
        switch (name) {
            case "script":
                return script
            case "property":
                return property
            case "environment":
                return environment
            case "commandLine":
                return commandLine
        }
        none
    }

    static PropertyLocation[] values() {
        [none, script, property, environment, commandLine]
    }
}

/**
 * Fallback which does nothing. NOTHING!
 */
class NonePropertyLocation extends PropertyLocation {

    @Override
    String reason() {
        "no value was configured"
    }

    @Override
    PropertyWrite write(PropertySetArguments args) {
        null
    }
}

/**
 * Sets the property by writing the invocation in the gradle build script (in DSL)
 */
class BuildScriptPropertyLocation extends PropertyLocation {

    @Override
    String reason() {
        "value is provided in script"
    }

    @Override
    PropertyWrite write(PropertySetArguments args) {
        def value = args.value
        def wrappedValue = PropertyUtils.wrapValueBasedOnType(value, args.typeName, args.wrapFallback)
        def scriptInvocation = "${args.setInvocation.compose(args.path, wrappedValue)}"
        args.integration.buildFile << scriptInvocation
        null
    }
}

/**
 * Sets the property into the project's gradle.properties file
 */
class PropertiesFilePropertyLocation extends PropertyLocation {

    @Override
    String reason() {
        "value is provided in properties"
    }

    @Override
    PropertyWrite write(PropertySetArguments args) {
        def propertiesFile = args.integration.createFile("gradle.properties")
        def propValue = PropertyUtils.serializeValueToEnvironment(args.value, args.typeName, args.wrapFallback)
        propertiesFile << "${args.propertyKey ?: args.path} = ${propValue}"
        null
    }
}

/**
 * Sets the property onto the system environment
 */
class EnvironmentPropertyLocation extends PropertyLocation {

    @Override
    String reason() {
        "value is provided in environment"
    }

    @Override
    PropertyWrite write(PropertySetArguments args) {
        def envPropertyKey = args.environmentKey ?: envNameFromProperty(args.path)
        def envValue = PropertyUtils.serializeValueToEnvironment(args.value, args.typeName, args.wrapFallback)
        args.integration.environmentVariables.set(envPropertyKey, envValue)
        null
    }
}

/**
 * Sets the property via the command line arguments (which are invoked alongside the task name)
 */
class CommandLineOptionPropertyLocation extends PropertyLocation {

    String assignmentOperator

    CommandLineOptionPropertyLocation(String assigmentOperator = "=") {
        this.assignmentOperator = assigmentOperator
    }

    @Override
    String reason() {
        "value is provided as a command line argument"
    }

    @Override
    PropertyWrite write(PropertySetArguments args) {
        if (!args.commandLineOption) {
            throw new MissingArgumentException("No command line option was provided for '${args.path}'")
        }

        String option = args.commandLineOption
        if (args.typeName != "Boolean" && args.value != Wildcard.INSTANCE) {
            def cliValue = PropertyUtils.wrapValueBasedOnType(args.value, args.typeName, args.wrapFallback)
            option += "${assignmentOperator}${cliValue}"
        }

        new PropertyWrite([option])
    }
}

/**
 * Arguments used for setting a property
 */
class PropertySetArguments {

    // Required
    Object value
    String typeName
    String path
    PropertySetInvocation setInvocation
    // Overrides
    String environmentKey
    String propertyKey
    String commandLineOption
    // Serialization
    Closure<String> wrapFallback
    // This is set at the end
    IntegrationHandler integration
}
