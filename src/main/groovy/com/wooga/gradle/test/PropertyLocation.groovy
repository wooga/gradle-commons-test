package com.wooga.gradle.test

import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertyWrite
import org.spockframework.lang.Wildcard

import static com.wooga.gradle.test.PropertyUtils.envNameFromProperty

/**
 * The location used to set/get a gradle project's particular property
 */
abstract class PropertyLocation {

    /**
     * @return A description of the location
     */
    abstract String reason()
    /**
     * @param args The set of arguments needed by the location to perform the write operation
     * @return Optionally, a sequence of command line arguments to execute as part of the write operation (by the caller)
     */
    abstract PropertyWrite write(PropertySetArguments args)

    @Override
    String toString() {
        return reason()
    }

    /**
     * No location (skipped)
     */
    static PropertyLocation none = new NonePropertyLocation()
    /**
     * To the project's build.gradle script
     */
    static PropertyLocation script = new BuildScriptPropertyLocation()
    /**
     * To the project's gradle.properties file
     */
    static PropertyLocation property = new PropertiesFilePropertyLocation()
    /**
     * To the environment, before invoking the task
     */
    static PropertyLocation environment = new EnvironmentPropertyLocation()
    /**
     * To the command line, as an argument in the task invocation
     */
    static PropertyLocation commandLine = new CommandLineOptionPropertyLocation()

    /**
     * To the command line, as an argument in the task invocation using the -P flag.
     * This location is not considered by default since it has the same function as {@code property}
     */
    static PropertyLocation propertyCommandLine = new PropertiesCommandLinePropertyLocation()

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
 * Sets the property into by through the command line with the -P flag
 */
class PropertiesCommandLinePropertyLocation extends PropertyLocation {

    @Override
    String reason() {
        "value is provided in properties by -P command line flag"
    }

    @Override
    PropertyWrite write(PropertySetArguments args) {
        def propValue = PropertyUtils.serializeValueToEnvironment(args.value, args.typeName, args.wrapFallback)
        def commandLineArg = "-P${args.propertyKey ?: args.path}=${propValue}"
        new PropertyWrite([commandLineArg])
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

    String prefix
    String assignmentOperator

    CommandLineOptionPropertyLocation(String assigmentOperator = "=") {
        this(null, assigmentOperator)
    }

    CommandLineOptionPropertyLocation(String prefix, String assigmentOperator) {
        this.prefix = prefix
        this.assignmentOperator = assigmentOperator
    }

    @Override
    String reason() {
        "value is provided as a command line argument"
    }

    @Override
    PropertyWrite write(PropertySetArguments args) {
        if (!args.commandLineOption) {
            throw new IllegalArgumentException("No command line option was provided for '${args.path}'")
        }

        String option = ""
        if (prefix != null) {
            option += prefix
        }
        option += args.commandLineOption

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
