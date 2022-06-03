package com.wooga.gradle.test.writers

import com.wooga.gradle.test.IntegrationHandler
import com.wooga.gradle.test.IntegrationSpec
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertySetArguments
import com.wooga.gradle.test.queries.TestValue
import com.wooga.gradle.test.serializers.IntegrationObjectProcessor
import com.wooga.gradle.test.serializers.PropertyTypeSerializer
import groovyjarjarcommonscli.MissingArgumentException
import nebula.test.Integration
import org.gradle.api.Action
import org.spockframework.lang.Wildcard

import java.util.function.Function

import static com.wooga.gradle.test.PropertyUtils.envNameFromProperty

class PropertySetterWriter extends BasePropertyWriter {

    /**
     * The object the property belongs to (could be an extension, task)
     */
    String objectName
    /**
     * The name of property that is being set
     */
    String propertyName

    // Set
    Object value
    String typeName

    // Overrides
    String environmentKey
    String propertyKey
    String commandLineOption

    // Locations
    PropertyLocation location = PropertyLocation.script
    PropertySetInvocation setInvocation = PropertySetInvocation.assignment

    // Options
    private Boolean _normalizeFilePaths

    // Evaluation
    IntegrationObjectProcessor preprocessors = new IntegrationObjectProcessor()
    Map<PropertyLocation, Map<String, PropertyTypeSerializer>> serializationFallbacks = new HashMap<PropertyLocation, Map<String, PropertyTypeSerializer>>()
    Closure<String> legacySerializationFallback

    PropertySetterWriter(String objectName, String propertyName) {
        super(composePath(objectName, propertyName))
        this.objectName = objectName
        this.propertyName = propertyName
    }

    /**
     * Assigns the value and type of value to be set by the writer by its invocation
     */
    PropertySetterWriter set(TestValue value, String typeName) {
        set(value.raw, typeName)
    }

    /**
     * Assigns the value and type of value to be set by the writer by its invocation
     */
    PropertySetterWriter set(Object value, String typeName) {

        // Record the original input
        // TODO: Is there a case where we don't want to do this?
        // TODO: Set a preprocessor?
        if (_normalizeFilePaths && (typeName == "File" || typeName == "Provider<RegularFile>")) {
            value = IntegrationSpec.osPath((String) value)
        }
        this.value = value
        this.typeName = typeName
        this
    }

    /**
     * Assigns no type
     */
    PropertySetterWriter set(Object value, Wildcard wildcard) {
        set(value, Wildcard.class)
    }

    /**
     * Assigns the value and type of value to be set by the writer by its invocation
     */
    PropertySetterWriter set(TestValue value, Class type) {
        set(value.raw, type)
    }

    /**
     * Assigns the value and type of value to be set by the writer by its invocation
     */
    PropertySetterWriter set(Object value, Class type) {
        set(value, type.simpleName)
    }

    /**
     * Assigns the value and type of value to be set by the writer by its invocation
     */
    PropertySetterWriter setCommandLineOption(String option) {
        this.commandLineOption = option
        this
    }

    /**
     * Assigns the location, which determines by what mechanism the value will be set (script, environment, etc)
     */
    PropertySetterWriter to(PropertyLocation location) {
        this.location = location
        this
    }

    /**
     * Assigns the location, which determines by what mechanism the value will be set (script, environment, etc)
     */
    PropertySetterWriter toScript(PropertySetInvocation method = PropertySetInvocation.providerSet) {
        this.location = PropertyLocation.script
        this.setInvocation = method
        this
    }

    /**
     * Assigns the location, which determines by what mechanism the value will be set (script, environment, etc)
     */
    PropertySetterWriter toCommandLine(String option) {
        to(PropertyLocation.commandLine)
        setCommandLineOption(option)
        this
    }

    /**
     * Assigns the invocation to use when setting the property by script
     */
    PropertySetterWriter use(PropertySetInvocation method) {
        this.setInvocation = method
        this
    }

    /**
     * Assigns the invocation to use when setting the property by script
     */
    PropertySetterWriter use(Wildcard) {
        this.setInvocation = PropertySetInvocation.none
        this
    }

    /**
     * Instructs the writer to use a specific environment key to be used when setting the property onto the environment
     */
    PropertySetterWriter withEnvironmentKey(String key) {
        this.environmentKey = key
        this
    }

    /**
     * Instructs the writer to use a specific key to be used when writing to the gradle.properties file
     */
    PropertySetterWriter withPropertyKey(String key) {
        this.propertyKey = key
        this
    }

    /**
     * Instructs the writer to write to properties file and to the environment using the extension name
     * instead of whatever was initially set
     */
    PropertySetterWriter forExtension(String extensionName) {
        withPropertyKey(composePath(extensionName.toLowerCase(), propertyName))
        withEnvironmentKey(envNameFromProperty(extensionName.toLowerCase(), propertyName))
    }

    /**
     * Instructs the writer to preprocess objects of the given type (before serialization)
     */
    PropertySetterWriter preprocess(Action<IntegrationObjectProcessor> action) {
        action.execute(preprocessors)
        this
    }

    PropertySetterWriter withFilesRelativeToProjectDirectory(Boolean createFile = false) {
        preprocessors.put(File, { Object value, Integration integration ->
            def path = (String) value
            def file = new File(integration.projectDir, path)
            if (createFile) {
                integration.createFile(path)
            }
            file.path
        })
        this
    }

    PropertySetterWriter normalizeFilePaths() {
        _normalizeFilePaths = true
        this
    }

    /**
     * Includes a serialization fallback that will be used when trying to serialize the value
     * into a specific location
     */
    PropertySetterWriter serialize(PropertyLocation location, PropertyTypeSerializer function) {
        if (!serializationFallbacks.containsKey(location)) {
            serializationFallbacks.put(location, new HashMap<String, PropertyTypeSerializer>())
        }

        serializationFallbacks[location].put(function.typeName, function)
        this
    }

    /**
     * Includes a serialization fallback that will be used when trying to serialize the value
     * into a specific location
     */
    PropertySetterWriter serialize(PropertyLocation location, String typeName, Function<Object, String> fallback) {
        this.legacySerializationFallback = null
        serialize(location, new PropertyTypeSerializer(typeName, fallback))
    }

    /**
     * Includes all the given serialization functions
     */
    PropertySetterWriter serialize(List<PropertyTypeSerializer> functions, PropertyLocation location = PropertyLocation.script) {
        for (f in functions) {
            serialize(location, f)
        }
        this
    }

    /**
     * Instructs the writer to use this given fallback. This will override any other serialization settings.
     */
    PropertySetterWriter serialize(Closure<String> fallback) {
        this.legacySerializationFallback = fallback
        this
    }

    /**
     * Includes a serialization fallback that will be used when trying to serialize the value
     * into a specific location
     */
    PropertySetterWriter serialize(PropertyLocation location, Class type, Function<Object, String> fallback) {
        serialize(location, type.simpleName, fallback)
        this
    }

    PropertyWrite write(IntegrationHandler integration) {

        if (value == null && location != PropertyLocation.none) {
            throw new MissingArgumentException("No value was set. Please make sure to call the set(value, type) method")
        }

        // Compose the arguments
        PropertySetArguments args = new PropertySetArguments()
        args.value = preprocessors.process(this.value, this.typeName, integration)
        args.typeName = typeName
        args.path = path
        args.setInvocation = setInvocation

        args.propertyKey = propertyKey
        args.environmentKey = environmentKey
        args.commandLineOption = commandLineOption
        args.wrapFallback = legacySerializationFallback ?: generateWrapFallback()

        args.integration = integration
        location.write(args)
    }

    Closure<String> generateWrapFallback() {

        if (!serializationFallbacks.containsKey(location)) {
            return null
        }

        def fallbacks = serializationFallbacks[location]
        return { Object value, String typeName, Closure<String> fallback ->
            String result = value
            if (fallbacks.containsKey(typeName)) {
                def func = fallbacks[typeName]
                if (func.serializer == null) {
                    throw new MissingArgumentException("No serializer on the function was set for type ${typeName}")
                }
                result = func.serializer.apply(value)
            }
            result
        }
    }

    static String composePath(String objectName, String propertyName) {
        "${objectName}.${propertyName}"
    }
}
