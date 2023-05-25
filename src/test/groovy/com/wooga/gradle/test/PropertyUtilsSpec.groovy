package com.wooga.gradle.test

import spock.lang.Specification
import spock.lang.Unroll

import com.wooga.gradle.test.serializers.IntegrationObjectProcessor
import com.wooga.gradle.test.serializers.IntegrationObjectProcessorFunction

import java.util.function.BiFunction

class PropertyUtilsSpec extends Specification {

    @Unroll
    def "environment name is converted from extension and property name"() {
        expect:
        PropertyUtils.envNameFromProperty(extensionName, propertyName) == expectedEnvironmentName

        where:
        extensionName | propertyName          || expectedEnvironmentName
        "foobar"      | "kat.zen"             || "FOOBAR_KAT_ZEN"
        "foobar"      | "fooBar"              || "FOOBAR_FOO_BAR"
        "foobar"      | "foo.bar"             || "FOOBAR_FOO_BAR"
        "foobar"      | "fooBar.barFoo"       || "FOOBAR_FOO_BAR_BAR_FOO"
        "foobar"      | "foo2Bar.bar2Foo"     || "FOOBAR_FOO_2_BAR_BAR_2_FOO"
        "foobar"      | "foo2bar.bar2foo"     || "FOOBAR_FOO_2BAR_BAR_2FOO"
        "foobar"      | "foo44Bar.bar5555Foo" || "FOOBAR_FOO_44_BAR_BAR_5555_FOO"
        "foobar"      | "foo22bar.bar2222foo" || "FOOBAR_FOO_22BAR_BAR_2222FOO"
    }

    @Unroll
    def "environment name is converted from property name"() {
        expect:
        PropertyUtils.envNameFromProperty(propertyName) == expectedEnvironmentName

        where:
        propertyName          || expectedEnvironmentName
        "fooBar"              || "FOO_BAR"
        "foo.bar"             || "FOO_BAR"
        "fooBar.barFoo"       || "FOO_BAR_BAR_FOO"
        "foo2Bar.bar2Foo"     || "FOO_2_BAR_BAR_2_FOO"
        "foo2bar.bar2foo"     || "FOO_2BAR_BAR_2FOO"
        "foo44Bar.bar5555Foo" || "FOO_44_BAR_BAR_5555_FOO"
        "foo22bar.bar2222foo" || "FOO_22BAR_BAR_2222FOO"
    }

    @Unroll
    def "toCamelCase converts '#input' to camel case '#expectedValue"() {
        expect:
        PropertyUtils.toCamelCase(input) == expectedValue

        where:
        input                     || expectedValue
        "FOO_BAR"                 || "fooBar"
        "FOO_BAR_BAR_FOO"         || "fooBarBarFoo"
        "FOO_2_BAR_BAR_2_FOO"     || "foo2BarBar2Foo"
        "FOO_2BAR_BAR_2FOO"       || "foo2barBar2foo"
        "FOO_44_BAR_BAR_5555_FOO" || "foo44BarBar5555Foo"
        "FOO_22BAR_BAR_2222FOO"   || "foo22barBar2222foo"
        "foo_bar"                 || "fooBar"
        "foo_bar_bar_foo"         || "fooBarBarFoo"
        "foo_2_bar_bar_2_foo"     || "foo2BarBar2Foo"
        "foo_2bar_bar_2foo"       || "foo2barBar2foo"
        "foo_44_bar_bar_5555_foo" || "foo44BarBar5555Foo"
        "foo_22bar_bar_2222foo"   || "foo22barBar2222foo"
    }

    @Unroll
    def "toProviderSet returns a provider set method invocation #expectedResult from property #property"() {
        expect:
        PropertyUtils.toProviderSet(property) == expectedResult

        where:
        property      | expectedResult
        "foo"         | "foo.set"
        "foo.bar"     | "foo.bar.set"
        "foo.bar.baz" | "foo.bar.baz.set"
    }

    @Unroll
    def "toSetter returns a setter method invocation #expectedResult from property #property"() {
        expect:
        PropertyUtils.toSetter(property) == expectedResult

        where:
        property      | expectedResult
        "foo"         | "setFoo"
        "foo.bar"     | "foo.setBar"
        "foo.bar.baz" | "foo.bar.setBaz"
    }

    @Unroll
    def "preprocesses value #value of type #type into #expected"() {

        given: "a preprocessor"
        def processor = new IntegrationObjectProcessor()

        and: "some transformation functions"
        // String
        BiFunction<Object, IntegrationHandler, String> stringProcessor = { Object v, integration ->
            return ((String) v).toUpperCase()
        }
        processor.put("String", stringProcessor)
        // Integer
        IntegrationObjectProcessorFunction intPreprocessor = { v, IntegrationHandler i ->
            return ((int) v) * 2
        }
        processor.put("Integer", intPreprocessor)
        // Boolean
        def booleanPreprocessor = { Boolean b -> false }
        processor.transform(Boolean, booleanPreprocessor)
        // Float
        processor.transform(Float, { Float f ->
            f + 1
        })

        and: "a mock integration"
        IntegrationHandler integration = new CustomIntegrationHandler(File.createTempDir())

        when:
        def actual = processor.process(value, type, integration)

        then:
        expected == actual

        where:
        value    | type           | expected
        "foobar" | String         | "FOOBAR"
        "foobar" | "List<String>" | ["FOOBAR"]
        7        | Integer        | 14
        3.5      | Float          | 4.5
        true     | Boolean        | false
    }

    @Unroll
    def "wrapValueBasedOnType wraps given value #rawValue of type #rawType to requested type #type"() {
        expect:
        PropertyUtils.wrapValueBasedOnType(rawValue, type) == expected

        where:
        rawValue                                                   | rawType         | type                                  | expected
        "some value"                                               | "String"        | "String"                              | "'${rawValue}'"
        22                                                         | "Integer"       | "String"                              | "'${rawValue}'"
        false                                                      | "Boolean"       | "String"                              | "'${rawValue}'"

        ["some", "strings"]                                        | "List<String>"  | "String[]"                            | "['some', 'strings'] as String[]"
        [22, 10, 23]                                               | "List<Integer>" | "String[]"                            | "['22', '10', '23'] as String[]"
        [false, true, false]                                       | "List<Boolean>" | "String[]"                            | "['false', 'true', 'false'] as String[]"

        [22, 10, 23]                                               | "List<Integer>" | "int[]"                               | "[22, 10, 23] as int[]"
        [false, true, false]                                       | "List<Boolean>" | "boolean[]"                           | "[false, true, false] as boolean[]"

        ["some", "strings"]                                        | "List<String>"  | "String..."                           | "'some', 'strings'"
        [22, 10, 23]                                               | "List<Integer>" | "String..."                           | "'22', '10', '23'"
        [false, true, false]                                       | "List<Boolean>" | "String..."                           | "'false', 'true', 'false'"

        [22, 10, 23]                                               | "List<Integer>" | "int..."                              | "22, 10, 23"
        [false, true, false]                                       | "List<Boolean>" | "boolean..."                          | "false, true, false"

        ["some", "strings"]                                        | "List<String>"  | "List"                                | "['some', 'strings']"
        [22, 10, 23]                                               | "List<Integer>" | "List"                                | "['22', '10', '23']"
        [false, true, false]                                       | "List<Boolean>" | "List"                                | "['false', 'true', 'false']"

        ["some", "strings"]                                        | "List<String>"  | "List<String>"                        | "['some', 'strings']"
        [22, 10, 23]                                               | "List<Integer>" | "List<Integer>"                       | "[22, 10, 23]"
        [false, true, false]                                       | "List<Boolean>" | "List<Boolean>"                       | "[false, true, false]"

        ["foo": "bar", "true": false, "one": 1]                    | "Map"           | "Map"                                 | "['foo' : 'bar', 'true' : false, 'one' : 1]"
        ["foo": "bar", "true": false, "one": 1]                    | "Map"           | "Map<String, String>"                 | "['foo' : 'bar', 'true' : 'false', 'one' : '1']"

        "some value"                                               | "String"        | "Closure"                             | "{'${rawValue}'}"
        22                                                         | "Integer"       | "Closure"                             | "{${rawValue}}"
        false                                                      | "Boolean"       | "Closure"                             | "{${rawValue}}"

        "some value"                                               | "String"        | "Closure<Object>"                     | "{new Object() {@Override String toString() { '${rawValue}' }}}"
        22                                                         | "Integer"       | "Closure<String>"                     | "{'${rawValue}'}"
        false                                                      | "Boolean"       | "Closure<String>"                     | "{'${rawValue}'}"

        ["some", "strings"]                                        | "String"        | "Closure<List<String>>"               | "{['some', 'strings']}"
        ["some", "strings"]                                        | "String"        | "Closure"                             | "{['some', 'strings']}"

        "some value"                                               | "String"        | "Callable"                            | "new java.util.concurrent.Callable<java.lang.String>() {@Override java.lang.String call() throws Exception {'${rawValue}'}}"
        "1.1.1"                                                    | "String"        | "Callable"                            | "new java.util.concurrent.Callable<java.lang.String>() {@Override java.lang.String call() throws Exception {'${rawValue}'}}"
        22                                                         | "Integer"       | "Callable"                            | "new java.util.concurrent.Callable<java.lang.Integer>() {@Override java.lang.Integer call() throws Exception {${rawValue}}}"
        false                                                      | "Boolean"       | "Callable"                            | "new java.util.concurrent.Callable<java.lang.Boolean>() {@Override java.lang.Boolean call() throws Exception {${rawValue}}}"

        "some value"                                               | "String"        | "Callable<Object>"                    | "new java.util.concurrent.Callable<Object>() {@Override Object call() throws Exception {new Object() {@Override String toString() { '${rawValue}' }}}}"
        22                                                         | "Integer"       | "Callable<String>"                    | "new java.util.concurrent.Callable<String>() {@Override String call() throws Exception {'${rawValue}'}}"
        false                                                      | "Boolean"       | "Callable<String>"                    | "new java.util.concurrent.Callable<String>() {@Override String call() throws Exception {'${rawValue}'}}"

        ["some", "strings"]                                        | "String"        | "Callable<List<String>>"              | "new java.util.concurrent.Callable<List<String>>() {@Override List<String> call() throws Exception {['some', 'strings']}}"
        ["some", "strings"]                                        | "String"        | "Callable"                            | "new java.util.concurrent.Callable<java.util.ArrayList>() {@Override java.util.ArrayList call() throws Exception {['some', 'strings']}}"

        "/some/path/to/a/file"                                     | "String"        | "File"                                | "new File('${rawValue}')"
        "/some/path/to/a/file"                                     | "String"        | "RegularFile"                         | "project.layout.projectDirectory.file('${rawValue}')"
        "/some/path/to/a/file"                                     | "String"        | "Directory"                           | "project.layout.projectDirectory.dir('${rawValue}')"
        "/some/path/to/a/file"                                     | "String"        | "Provider<File>"                      | "project.provider({new File('${rawValue}')})"
        "/some/path/to/a/file"                                     | "String"        | "Provider<RegularFile>"               | "project.layout.buildDirectory.file(project.provider({'${rawValue}'}))"
        "/some/path/to/a/file"                                     | "String"        | "Provider<Directory>"                 | "project.layout.buildDirectory.dir(project.provider({'${rawValue}'}))"
        ["some", "strings"]                                        | "String"        | "Provider<List<String>>"              | "project.provider({['some', 'strings']})"
        ["foo": "bar", "true": false, "one": 1]                    | "Map"           | "Provider<Map<String, String>>"       | "project.provider({['foo' : 'bar', 'true' : 'false', 'one' : '1']})"
        ["foo": ["bar"], "true": [false, false], "one": [1, 2, 3]] | "Map"           | "Provider<Map<String, List<String>>>" | "project.provider({['foo' : ['bar'], 'true' : ['false', 'false'], 'one' : ['1', '2', '3']]})"
    }

    def "wrapValueBasedOnType can wrap custom types with fallback closure"() {
        given: "a fallback closure"
        def fallback = { Object _rawValue, String _type, Closure<String> fallback ->
            def value
            switch (_type) {
                case "CustomType":
                    def m = _rawValue as Map
                    value = "'My custom type is a string with value ${m["value"]} and foo ${m["foo"]}'"
                    break
                default:
                    value = _rawValue.toString()
                    break
            }
            value
        }

        expect:
        PropertyUtils.wrapValueBasedOnType(rawValue, type, fallback) == expected

        where:
        rawValue                                 | rawType | type                               | expected
        ["foo": "bar", "value": 22]              | "Map"   | "Provider<CustomType>"             | "project.provider({'My custom type is a string with value ${rawValue["value"]} and foo ${rawValue["foo"]}'})"
        ["content": ["foo": "bar", "value": 22]] | "Map"   | "Provider<Map<String,CustomType>>" | "project.provider({['content' : 'My custom type is a string with value ${rawValue["content"]["value"]} and foo ${rawValue["content"]["foo"]}']})"
    }
}
