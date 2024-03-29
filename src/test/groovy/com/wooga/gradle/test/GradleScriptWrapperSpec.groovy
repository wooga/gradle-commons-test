package com.wooga.gradle.test


import spock.lang.Specification
import spock.lang.Unroll

class GradleScriptWrapperSpec extends Specification {

    @Unroll
    def "asGradleScript wraps given value #rawValue of type #rawType to requested type #type"() {
        expect:
        PropertyUtils.asGradleScript(rawValue, type) == expected

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
        "/some/path/to/a/file"                                     | "String"        | "Provider<RegularFile>"               | "project.provider{ project.layout.projectDirectory.file('${rawValue}') }"
        "/some/path/to/a/file"                                     | "String"        | "Provider<Directory>"                 | "project.provider{ project.layout.projectDirectory.dir('${rawValue}') }"
        ["some", "strings"]                                        | "String"        | "Provider<List<String>>"              | "project.provider({['some', 'strings']})"
        ["foo": "bar", "true": false, "one": 1]                    | "Map"           | "Provider<Map<String, String>>"       | "project.provider({['foo' : 'bar', 'true' : 'false', 'one' : '1']})"
        ["foo": ["bar"], "true": [false, false], "one": [1, 2, 3]] | "Map"           | "Provider<Map<String, List<String>>>" | "project.provider({['foo' : ['bar'], 'true' : ['false', 'false'], 'one' : ['1', '2', '3']]})"
    }

    @Unroll
    def "asGradleScript can wrap custom types with fallback closure"() {
        given: "a fallback closure"
        def fallback = { Object v, String t, Closure<String> fallback ->
            def value
            switch (t) {
                case "CustomType":
                    def m = v as Map
                    value = "'My custom type is a string with value ${m["value"]} and foo ${m["foo"]}'"
                    break
                default:
                    value = v.toString()
                    break
            }
            value
        }

        expect:
        PropertyUtils.asGradleScript(rawValue, type, fallback) == expected

        where:
        rawValue                                 | rawType | type                               | expected
        ["foo": "bar", "value": 22]              | "Map"   | "Provider<CustomType>"             | "project.provider({'My custom type is a string with value ${rawValue["value"]} and foo ${rawValue["foo"]}'})"
        ["content": ["foo": "bar", "value": 22]] | "Map"   | "Provider<Map<String,CustomType>>" | "project.provider({['content' : 'My custom type is a string with value ${rawValue["content"]["value"]} and foo ${rawValue["content"]["foo"]}']})"
    }
}
