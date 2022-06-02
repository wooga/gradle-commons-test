package com.wooga.gradle.test

import com.wooga.gradle.test.queries.TestValue
import spock.lang.Specification
import spock.lang.Unroll

class TestValueSpec extends Specification {

    @Unroll
    def "sets the raw value #raw, expecting #expected"() {

        expect:
        def testValue = TestValue.set(raw)
        expected == testValue.expected

        where:
        raw | expected
        7   | 7
    }

    @Unroll
    def "maps the raw value #raw, expecting #expected"() {
        expect:
        def testValue = TestValue.set(raw)
        testValue.expect(function)
        expected == testValue.expected

        where:
        raw | function                 | expected
        7   | { it -> it * 2 }         | 14
        2   | { Integer it -> it * 2 } | 4
    }

}
