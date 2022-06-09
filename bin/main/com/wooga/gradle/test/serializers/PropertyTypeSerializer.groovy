package com.wooga.gradle.test.serializers

import com.wooga.gradle.test.PropertyUtils

import java.util.function.Function

class PropertyTypeSerializer {
    String typeName
    Function<Object, String> serializer

    PropertyTypeSerializer(String typeName, Function<Object, String> serializer) {
        this.typeName = typeName
        this.serializer = serializer
    }

    static PropertyTypeSerializer constructor(Class type, Class paramType) {
        def serializer = {
            v -> "new ${type.name}(${PropertyUtils.wrapValueBasedOnType(v, paramType.simpleName)})"
        }
        new PropertyTypeSerializer(type.simpleName, serializer)
    }

    static PropertyTypeSerializer enumeration(Class type) {
        def serializer = { v ->
            "${type.canonicalName}.${v.toString()}".toString()
        }
        new PropertyTypeSerializer(type.simpleName, serializer)
    }

}
