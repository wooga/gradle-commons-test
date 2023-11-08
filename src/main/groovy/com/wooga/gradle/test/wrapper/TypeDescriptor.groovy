package com.wooga.gradle.test.wrapper

import com.wooga.gradle.test.StringTypeMatch
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class TypeDescriptor {
    final String type
    final String subtype

    static TypeDescriptor fromTypeName(String typeName) {
        def match = StringTypeMatch.match(typeName)
        return new TypeDescriptor(match.mainType, match.subType)
    }

    TypeDescriptor(String type, String subtype = null) {
        this.type = type
        this.subtype = subtype
    }

    String[] allSubtypes(int limit) {
        return subtype.split(",", limit).collect { it.trim() }
    }

    @Override
    public String toString() {
        switch (type) {
            case "[]": return "${subtype ?: ""}[]"
            case "...": return "${subtype ?: ""}..."
        }
        if (subtype) {
            return "$type<$subtype>";
        }
        return type
    }
}