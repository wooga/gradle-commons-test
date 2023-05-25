package com.wooga.gradle.test

import com.wooga.gradle.test.queries.TestValue
import spock.lang.Specification
import spock.lang.Unroll

import static com.wooga.gradle.test.IntegrationSpec.osPath
import static com.wooga.gradle.test.PropertyUtils.*

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

    @Unroll
    def "values #values for a list result in raw #raw and expected #expected"() {
        expect:
        def testValue = TestValue.list(values)
        raw == testValue.raw
        expected == testValue.expected

        where:
        values    | raw       | expected
        [1, 2, 3] | "[1,2,3]" | [1, 2, 3]
        7         | "[7]"     | [7]
    }

    @Unroll
    def "input #input results in raw value #raw and expected value #expected"() {
        expect:
        raw == testValue.raw.toString()
        expected == testValue.expected

        where:
        input          | testValue                                             | raw                                                     | expected
        ["/foo/bar"]   | TestValue.filePaths(input)                            | "[${wrapValueBasedOnType(osPath("/foo/bar"), String)}]" | [osPath("/foo/bar")]
        "/foo/bar"     | TestValue.filePaths(input)                            | "[${wrapValueBasedOnType(osPath("/foo/bar"), String)}]" | [osPath("/foo/bar")]
        "/foo/bar"     | TestValue.filePath(input, true)                       | "${wrapValueBasedOnType(osPath("/foo/bar"), String)}"   | osPath("/foo/bar")
        "/foo/bar"     | TestValue.filePath(input)                             | "${osPath("/foo/bar")}"                                 | osPath("/foo/bar")
        "pancakes"     | TestValue.list(input).expectPrepend("waffles")        | "[pancakes]"                                            | ["waffles", "pancakes"]
        _              | TestValue.none()                                      | "null"                                                  | null
        ["foo", "bar"] | TestValue.join(input)                                 | "foo,bar"                                               | input
        7              | TestValue.set(input).expect({ Integer i -> i * 2 })   | "7"                                                     | 14
        7              | TestValue.set(input).write({ "<${it}>" })             | "<7>"                                                   | 7
    }

}
