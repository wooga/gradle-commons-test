package com.wooga.gradle.test


import spock.lang.Specification
import spock.lang.Unroll


class PropertyLocationSpec extends Specification {

    @Unroll
    def "switch statement branches correctly"() {

        when:
        String actual = null
        switch (location) {
            case PropertyLocation.none:
                actual = "a"
                break
            case PropertyLocation.script:
                actual = "b"
                break
            case PropertyLocation.property:
                actual = "c"
                break
            case PropertyLocation.environment:
                actual = "d"
                break
        }

        then:
        actual == expected

        where:
        location                     | expected
        PropertyLocation.none        | "a"
        PropertyLocation.script      | "b"
        PropertyLocation.property    | "c"
        PropertyLocation.environment | "d"
    }

    def "can retrieve all values like an enum type"() {
        when:
        PropertyLocation[] locations = PropertyLocation.values()

        then:
        locations.length == 5
        locations.contains(PropertyLocation.none)
        locations.contains(PropertyLocation.script)
        locations.contains(PropertyLocation.property)
        locations.contains(PropertyLocation.environment)
        locations.contains(PropertyLocation.commandLine)
    }

    @Unroll
    def "can call valueOf for #name expecting #value like an enum type"() {
        when:
        def actual = PropertyLocation.valueOf(name)

        then:
        expected == actual

        where:
        name          | expected
        "none"        | PropertyLocation.none
        "script"      | PropertyLocation.script
        "property"    | PropertyLocation.property
        "environment" | PropertyLocation.environment
        "commandLine" | PropertyLocation.commandLine
    }

}

