package com.wooga.gradle.test.wrapper


class GradleScriptWrapper {
    interface Serializer { //functional interface
        String call(GradleTypeWrapper, TypeDescriptor, Object)
    }

    static GradleScriptWrapper withBuiltInSerializers() {
        return TypeSerializers.populateWithSerializers(new GradleScriptWrapper())
    }

    private Map<TypeDescriptor, Serializer> valueSerializers = new HashMap<>()
    Closure<String> fallback = null

    GradleScriptWrapper(Map<String, Serializer>... valueSerializers) {
        this.valueSerializers = valueSerializers.collectEntries { it }
    }

    GradleScriptWrapper addSerializer(Class type, Closure<String> serializer) {
        return addSerializer(type, serializer as Serializer)
    }

    GradleScriptWrapper addSerializer(String type, Closure<String> serializer) {
        return addSerializer(type, serializer as Serializer)
    }

    GradleScriptWrapper addSerializer(Class type, Serializer serializer) {
        return addSerializer(type.simpleName, serializer)
    }

    GradleScriptWrapper addSerializer(String type, Serializer serializer) {
        return addSerializer(TypeDescriptor.fromTypeName(type), serializer)
    }

    GradleScriptWrapper addSerializer(TypeDescriptor type, Serializer serializer) {
        this.valueSerializers[type] = serializer
        return this
    }

    GradleScriptWrapper withFallback(Closure<String> fallback) {
        this.fallback = fallback
        return this
    }

    String wrap(Object rawValue, Class type) {
        return wrap(rawValue, type.simpleName)
    }

    String wrap(Object rawValue, String typeName) {
        def type = TypeDescriptor.fromTypeName(typeName)
        def maybeSerializer = matchType(type)
        def serialized = maybeSerializer
                .map {serializer -> serializer(this, type, rawValue) }
                .orElseGet { fallback ? triggerFallback(fallback, rawValue, type) : rawValue.toString() }
        return serialized
    }

    //for now this fallback must follow the signature from the old "wrapValueBasedOnType"
    String triggerFallback(Closure<String> fallback, Object rawValue, TypeDescriptor type) {
        def cloneFallback = fallback.clone()
        cloneFallback.setDelegate(this)
        def typeString = type.toString()
        switch (fallback.maximumNumberOfParameters) {
            case 1:
                return cloneFallback.call(typeString)
            case 2:
                return cloneFallback.call(rawValue, typeString)
            case 3:
                return cloneFallback.call(rawValue, typeString, cloneFallback)
        }
        throw new IllegalStateException("No valid signature for fallback closure")
    }

    private Optional<Serializer> matchType(TypeDescriptor type) {
        def serializer = valueSerializers[type]
        if (!serializer) {
            serializer = valueSerializers.find { it.key.type == type.type }?.value
        }
        return Optional.ofNullable(serializer)
    }
}

