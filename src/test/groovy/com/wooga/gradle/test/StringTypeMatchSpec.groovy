package com.wooga.gradle.test


import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class StringTypeMatchSpec extends Specification {

    @Unroll
    def "matches types correctly"() {
        when:
        def matcher = StringTypeMatch.match(type)

        then:
        matcher.mainType == mainType
        matcher.subType == subType

        where:
        type                     | mainType   | subType
        "String"                 | "String"   | null
        "String..."              | "..."      | "String"
        "String[]"               | "[]"       | "String"
        "List<String>"           | "List"     | "String"
        "List<Boolean>"          | "List"     | "Boolean"
        "Provider<Integer>"      | "Provider" | "Integer"
        "List<List<Integer>>"    | "List"     | "List<Integer>"
        "Provider<List<String>>" | "Provider" | "List<String>"
    }
}

