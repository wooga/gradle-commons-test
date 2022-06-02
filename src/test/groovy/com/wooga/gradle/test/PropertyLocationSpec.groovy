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
}

