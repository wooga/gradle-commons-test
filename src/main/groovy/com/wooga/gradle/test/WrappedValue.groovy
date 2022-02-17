package com.wooga.gradle.test

class WrappedValue {

    final Object value
    final String typeName

    Object getValue() {
        value
    }

    String getTypeName() {
        typeName
    }

    WrappedValue(Object value, String typeName) {
        this.value = value
        this.typeName = typeName
    }

    static WrappedValue Boolean(Boolean value) {
        new WrappedValue(value, "Boolean")
    }

    static WrappedValue String(String value) {
        new WrappedValue(value, "String")
    }

    static WrappedValue Integer(Integer value) {
        new WrappedValue(value, "Integer")
    }
}
