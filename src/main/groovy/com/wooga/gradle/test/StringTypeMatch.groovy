package com.wooga.gradle.test

class StringTypeMatch {
    String mainType
    String subType

    static StringTypeMatch match(String type) {
        StringTypeMatch result = new StringTypeMatch()
        if (type.endsWith("...") || type.endsWith("[]")) {
            def parts = type.split(/(\.\.\.|\[\])/)
            result.subType = parts.first()
            result.mainType = type.endsWith("...") ? "..." : "[]"
        } else {
            def subtypeMatches = type =~ /(?<mainType>\w+)(<(?<subType>.*?)>)?/
            result.subType = (subtypeMatches.matches()) ? subtypeMatches.group("subType") : null
            result.mainType = (subtypeMatches.matches()) ? subtypeMatches.group("mainType") : type
        }
        result
    }
}
