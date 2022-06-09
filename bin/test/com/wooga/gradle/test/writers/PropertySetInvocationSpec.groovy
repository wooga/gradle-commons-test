package com.wooga.gradle.test.writers

import spock.lang.Specification
import spock.lang.Unroll

class PropertySetInvocationSpec extends Specification {

    @Unroll
    def "composes property #path with setter #method -> #expected"() {
        expect:
        def actual = method.compose(path, value)
        expected == actual

        where:
        path          | value | method                                         | expected
        "foobar"      | "7"   | PropertySetInvocation.providerSet              | "foobar.set(7)"
        "foobar"      | "7"   | PropertySetInvocation.assignment               | "foobar = 7"
        "foo.bar"     | "7"   | PropertySetInvocation.assignment               | "foo.bar = 7"
        "foobar"      | "7"   | PropertySetInvocation.setter                   | "setFoobar(7)"
        "foo.bar"     | "7"   | PropertySetInvocation.setter                   | "foo.setBar(7)"
        "foobar"      | "7"   | PropertySetInvocation.method                   | "foobar(7)"
        "foo.bar"     | "7"   | PropertySetInvocation.method                   | "foo.bar(7)"
        "foobar"      | "7"   | PropertySetInvocation.customSetter("pancakes") | "pancakes(7)"
        "foo.bar"     | "7"   | PropertySetInvocation.customSetter("pancakes") | "foo.pancakes(7)"
        "foo.bar.baz" | "7"   | PropertySetInvocation.customSetter("pancakes") | "foo.bar.pancakes(7)"
    }

}
