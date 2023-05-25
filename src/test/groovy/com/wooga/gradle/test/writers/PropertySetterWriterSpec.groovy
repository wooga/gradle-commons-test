package com.wooga.gradle.test.writers


import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.serializers.PropertyTypeSerializer
import com.wooga.gradle.test.mock.MockTaskIntegrationSpec
import com.wooga.gradle.test.queries.TestValue
import spock.lang.Unroll

class PropertySetterWriterSpec extends MockTaskIntegrationSpec<PropertyTask> {

    def "throws if no value is set"() {
        when:
        runPropertyQuery(getter, setter)

        then:
        thrown(IllegalArgumentException)

        where:
        property = "pancakes"
        setter = new PropertySetterWriter(subjectUnderTestName, property)
        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll
    def "property setter writer can write type #type, method #method and #location"() {

        expect:
        runPropertyQuery(getter, setter).matches(value)

        where:
        property        | value                                                                              | type                    | method                            | location
        "logFile"       | "a/b/c/pancakes.txt"                                                               | File                    | PropertySetInvocation.providerSet | PropertyLocation.script
        "logFile"       | TestValue.set("a/b/c/pancakes.txt")                                                | File                    | PropertySetInvocation.providerSet | PropertyLocation.script
        "logFile"       | "a/b/c/pancakes.txt"                                                               | "Provider<RegularFile>" | PropertySetInvocation.providerSet | PropertyLocation.script
        "pancakeFlavor" | "mint"                                                                             | String                  | PropertySetInvocation.providerSet | PropertyLocation.script
        "pancakeFlavor" | "chocolate"                                                                        | String                  | PropertySetInvocation.providerSet | PropertyLocation.environment
        "pancakeFlavor" | "cow"                                                                              | String                  | PropertySetInvocation.providerSet | PropertyLocation.property
        "pancakeFlavor" | "burger"                                                                           | String                  | PropertySetInvocation.providerSet | PropertyLocation.propertyCommandLine
        "pancakeFlavor" | null                                                                               | String                  | PropertySetInvocation.none        | PropertyLocation.none
        "pancakeFlavor" | null                                                                               | _                       | PropertySetInvocation.none        | PropertyLocation.none
        "pancakeFlavor" | null                                                                               | _                       | _                                 | PropertyLocation.none
        "bake"          | true                                                                               | Boolean                 | PropertySetInvocation.providerSet | PropertyLocation.script
        "bake"          | true                                                                               | Boolean                 | PropertySetInvocation.assignment  | PropertyLocation.script
        "bake"          | true                                                                               | Boolean                 | PropertySetInvocation.assignment  | PropertyLocation.environment
        "bake"          | true                                                                               | Boolean                 | PropertySetInvocation.assignment  | PropertyLocation.property
        "bake"          | true                                                                               | Boolean                 | PropertySetInvocation.assignment  | PropertyLocation.propertyCommandLine
        "tags"          | ["foo", "bar"]                                                                     | "List<String>"          | PropertySetInvocation.providerSet | PropertyLocation.script
        "tags"          | TestValue.set(["foo", "bar"])                                                      | "List<String>"          | PropertySetInvocation.providerSet | PropertyLocation.script
        "tags"          | ["foo", "bar"]                                                                     | "List<String>"          | PropertySetInvocation.providerSet | PropertyLocation.environment
        "tags"          | ["foo", "bar"]                                                                     | "List<String>"          | PropertySetInvocation.providerSet | PropertyLocation.property
        "tags"          | ["foo", "bar"]                                                                     | "List<String>"          | PropertySetInvocation.providerSet | PropertyLocation.propertyCommandLine
        "numbers"       | TestValue.none().expect(PropertyTask.PropertyTaskConventions.numbers.defaultValue) | Integer                 | _                                 | _

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
            .to(location)
            .use(method)
            .withKeyComposedFrom(PropertyTask.extensionName)

        getter = new PropertyGetterTaskWriter(setter)
            .configure({
                it.withFilePathsRelativeToProject()
                    .withFileProvidersRelativeTo("build")
            })
    }

    @Unroll
    def "can set project file with value #value, type #type if #location"() {
        expect:
        runPropertyQuery(getter, setter).matches(value)

        where:
        property  | value                                                                        | type | method                            | location
        "logsDir" | TestValue.set("build/custom/logs").expectAsProjectFile()                     | File | PropertySetInvocation.providerSet | PropertyLocation.script
        "logsDir" | TestValue.projectFile("build/custom/logs").describe("by convenience method") | File | PropertySetInvocation.providerSet | PropertyLocation.script
        "logsDir" | TestValue.projectBuildFile("custom/logs").describe("by convenience method")  | File | PropertySetInvocation.providerSet | PropertyLocation.script
        // Setting it from the properties sets it to build/
        "logsDir" | TestValue.set("custom/logs").expectProjectFile("build/custom/logs")          | File | _                                 | PropertyLocation.property
        // Setting it from the environment sets it to build/
        "logsDir" | TestValue.set("custom/logs").expectProjectFile("build/custom/logs")          | File | PropertySetInvocation.none        | PropertyLocation.environment

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
            .to(location)
            .use(method)
            .withKeyComposedFrom(PropertyTask.extensionName)

        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll
    def "property setter writes #value of type #type with #method"() {

        expect:
        runPropertyQuery(getter, setter).matches(value)

        where:
        property  | value                                                                        | type                   | method
        "exclude" | [osPath("/path/to/dir")]                                                     | "List<File>"           | PropertySetInvocation.providerSet
        "exclude" | [osPath("/path/to/dir")]                                                     | "Provider<List<File>>" | PropertySetInvocation.providerSet
        "exclude" | [osPath("/path/to/dir")]                                                     | "List<File>"           | PropertySetInvocation.setter
        "exclude" | [osPath("/path/to/dir")]                                                     | "Provider<List<File>>" | PropertySetInvocation.setter
        "exclude" | [osPath("/path/to/dir")]                                                     | "List<File>"           | PropertySetInvocation.method
        "exclude" | TestValue.filePath("/path/to/dir").expectList()                              | "File"                 | PropertySetInvocation.method
        "exclude" | [osPath("/path/to/dir")]                                                     | "File..."              | PropertySetInvocation.method
        "exclude" | TestValue.joinFilePaths(['/path/to/dir', '/path/to/dir2'], ",").expectList() | "String"               | PropertySetInvocation.customSetter("excludeOption")

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
            .use(method)

        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll
    def "can preprocess value of type #type"() {

        expect:
        runPropertyQuery(getter, setter).matches(value)

        where:
        property        | value                                | type
        "pancakeFlavor" | TestValue.set("mint").expect("MINT") | String
        "numbers"       | TestValue.set(5).expect(10)          | Integer

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
            .preprocess({ map -> map.transform(String, { String s -> s.toUpperCase() }) })
            .preprocess({ map -> map.transform(Integer, { Integer i -> i * 2 }) })
        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll
    def "invokes wrap constructor fallback for type #type"() {
        expect:
        runPropertyQuery(getter, setter).matches(value)

        where:
        property | value    | type
        "custom" | "foobar" | MockObject

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
            .serialize(PropertyLocation.script, PropertyTypeSerializer.constructor(MockObject, String))
        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll
    def "invokes wrap fallback for type #type"() {
        when:
        def result = runPropertyQuery(getter, setter)

        then:
        result.matches(value)

        where:
        property     | value    | type
        "custom"     | "foobar" | MockObject
        "customEnum" | "foo"    | MockEnum
        "numbers"    | 7        | Integer

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
            .serialize(PropertyLocation.script, PropertyTypeSerializer.enumeration(MockEnum))
            .serialize(PropertyLocation.script, MockObject, { v -> "new ${MockObject.name}(\"${value}\")" })
        getter = new PropertyGetterTaskWriter(setter)
    }

    /**
     * Proves that a project could statically define all their custom serializers, then reuse them on each writer
     */
    @Unroll
    def "can set type #type while using a collection of serializers"() {

        given:
        def serializers = new ArrayList<PropertyTypeSerializer>()
        serializers.add(PropertyTypeSerializer.enumeration(MockEnum))
        serializers.add(PropertyTypeSerializer.constructor(MockObject, String))

        setter.serialize(serializers)

        when:
        def result = runPropertyQuery(getter, setter)

        then:
        result.matches(value)

        where:
        property     | value    | type
        "custom"     | "foobar" | MockObject
        "customEnum" | "foo"    | MockEnum
        "numbers"    | 7        | Integer

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
        getter = new PropertyGetterTaskWriter(setter)
    }

    /**
     * Proves that this API is backwards compatible with projects using the legacy API
     */
    @Unroll
    def "can use legacy serialization callback function for type #type"() {
        given:
        Closure<String> fallback = { Object v, String t, Closure<String> f ->
            switch (t) {
                case MockObject.simpleName:
                    return "new ${MockObject.name}(\"${v}\")"
                case MockEnum.simpleName:
                    return "${MockEnum.canonicalName}.${v.toString()}".toString()
                default:
                    return v.toString()
            }
        }

        setter.serialize(fallback)

        when:
        def result = runPropertyQuery(getter, setter)

        then:
        result.matches(value)

        where:
        property     | value    | type
        "custom"     | "foobar" | MockObject
        "customEnum" | "foo"    | MockEnum
        "numbers"    | 7        | Integer

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll
    def "can set property #path with method #method"() {
        expect:
        def query = runPropertyQuery(getter, setter)
        query.matches(value)

        where:
        property          | nestedProperty | value      | type    | method
        "consoleSettings" | "useFoobar"    | true       | Boolean | PropertySetInvocation.assignment
        "consoleSettings" | "useFoobar"    | true       | Boolean | PropertySetInvocation.assignment.inConfiguration()
        "consoleSettings" | "prefix"       | "pancakes" | String  | PropertySetInvocation.assignment
        "consoleSettings" | "prefix"       | "waffles"  | String  | PropertySetInvocation.assignment.inConfiguration()
        "consoleSettings" | "prefix"       | "pancakes" | String  | PropertySetInvocation.customSetter("setMyPrefix")
        "consoleSettings" | "prefix"       | "waffles"  | String  | PropertySetInvocation.customSetter("setMyPrefix").inConfiguration()

        path = "${property}.get().${nestedProperty}"
        setter = new PropertySetterWriter(subjectUnderTestName, path)
            .set(value, type)
            .use(method)
        getter = new PropertyGetterTaskWriter(setter, "")
    }
}

