package com.wooga.gradle.test.writers


import spock.lang.Specification
import spock.lang.Unroll

class MockValueWrapper implements ValueWrapper {
}

enum WeatherEnum {
    cool,
    hot
}

class ValueWrapperSpec extends Specification {

    @Unroll
    def "string #actual written as #expected"() {

        given: "a wrapper implementation"
        def wrapper = new MockValueWrapper()

        expect:
        wrapper.wrapString(actual) == expected

        where:
        actual   | expected
        "foobar" | "'foobar'"
    }

    @Unroll
    def "enum #actual of type #type is written as #expected"() {

        given: "a wrapper implementation"
        def wrapper = new MockValueWrapper()

        expect:
        wrapper.wrapEnum(actual, type) == expected

        where:
        actual           | type        | expected
        WeatherEnum.cool | WeatherEnum | "WeatherEnum.cool"
        WeatherEnum.hot  | WeatherEnum | "WeatherEnum.hot"
    }

    @Unroll
    def "file #actual is written as #expected"() {

        given: "a wrapper implementation"
        def wrapper = new MockValueWrapper()

        expect:
        wrapper.wrapFile(actual) == expected

        where:
        actual             | expected
        new File("foobar") | "new File('foobar')"
    }

}
