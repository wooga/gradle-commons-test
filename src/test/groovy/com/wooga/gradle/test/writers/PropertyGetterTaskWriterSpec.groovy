package com.wooga.gradle.test.writers

import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.WrappedValue
import com.wooga.gradle.test.mock.MockTaskIntegrationSpec
import org.gradle.api.file.Directory
import spock.lang.Unroll

class PropertyGetterTaskWriterSpec extends MockTaskIntegrationSpec<PropertyTask> {

    @Unroll
    def "property query matches #value -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer).withoutAssertions()

        then:
        match == query.matches(expected)

        where:
        value  | expected | match
        "mint" | "mint"   | true
        null   | null     | true
        "foo"  | "bar"    | false

        property = "pancakeFlavor"
    }

    @Unroll
    def "property query checks property with value '#value' is null -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer).withoutAssertions()

        then:
        query.success
        expected == query.isNull()

        where:
        value  | expected
        null   | true
        "mint" | false

        property = "pancakeFlavor"
    }

    @Unroll
    def "property query checks property with value '#value' is not null -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer).withoutAssertions()

        then:
        query.success
        expected == query.isNotNull()

        where:
        value  | expected
        "mint" | true
        null   | false

        property = "pancakeFlavor"
    }

    @Unroll
    def "property query checks property with value '#value' is true -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.Boolean(value))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer).withoutAssertions()

        then:
        query.success
        expected == query.isTrue()

        where:
        value | expected
        true  | true
        false | false

        property = "bake"
    }

    @Unroll
    def "property query checks property with value '#value' is false -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.Boolean(value))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer).withoutAssertions()

        then:
        query.success
        expected == query.isFalse()

        where:
        value | expected
        true  | false
        false | true

        property = "bake"
    }

    @Unroll
    def "property query checks property with value '#value' contains substring '#substring' -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer).withoutAssertions()

        then:
        query.success
        expected == query.contains(substring)

        where:
        value            | substring | expected
        "chocolate_mint" | "mint"    | true
        "chocolate_mint" | "peach"   | false
        // TODO: returns true?
        //null             | ""        | false

        property = "pancakeFlavor"
    }

    @Unroll
    def "property query returns value of property '#value' -> '#expected'"() {
        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer).withoutAssertions()

        then:
        query.success
        expected == query.getValue()

        where:
        value            | expected
        "chocolate_mint" | "chocolate_mint"
        //null             | null

        property = "pancakeFlavor"
    }

    @Unroll
    def "property query with #setter setter returns value of property '#value' -> '#expected'"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer).withoutAssertions()

        then:
        query.success
        expected == query.getValue()

        where:
        value            | expected         | setter
        "chocolate_mint" | "chocolate_mint" | PropertySetInvocation.assignment
        "strawberry"     | "strawberry"     | PropertySetInvocation.providerSet

        property = "pancakeFlavor"
    }

    @Unroll
    def "property query can be used to check file #path"() {
        given:
        setSubjectTaskProvider(property, WrappedValue.File(path))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer).withoutAssertions()

        then:
        query.success
        query.matchesPath(path)
        query.endsWithPath(path)

        where:
        property  | path
        "logFile" | "a/b/c/pancakes.txt"
    }

    @Unroll
    def "property query can have custom matchers with #type"() {

        given:
        setSubjectTaskProvider(property, new WrappedValue(value, type))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer)
                .withFilePathsRelativeToProject()
                .withFileProvidersRelativeTo(projectSubDir)
                .withDirectoryPathsRelativeToProject()
                .withoutAssertions()

        then:
        query.matches(value, type)

        where:
        property        | value                | type
        "logFile"       | "a/b/c/pancakes.txt" | File
        "logFile"       | "a/b/c/pancakes.txt" | "Provider<RegularFile>"
        "pancakeFlavor" | "mint"               | String
        "bake"          | true                 | Boolean

        projectSubDir = "build"
    }


    @Unroll
    def "property query can have preset #type serializers for root project dir"() {
        given:
        setSubjectTaskProvider(property, new WrappedValue(value, type))

        when:
        def writer = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
        def query = runPropertyQuery(writer)
                .withFilePathsRelativeToProject()
                .withFileProvidersRelativeTo("build") //cant test the "relativeToProject() due to a bug in wrapValueBasedOnType()
                .withDirectoryPathsRelativeToProject()
                .withDirectoryProvidersRelativeTo("build") //cant test the "relativeToProject() due to a bug in wrapValueBasedOnType()
                .withoutAssertions()

        then:
        query.matches(value, type)

        where:
        property    | value          | type
        "logFile"   | "pancakes.txt" | File
        "logFile"   | "pancakes.txt" | "Provider<RegularFile>"
        "targetDir" | "pancakes"     | Directory
        "targetDir" | "pancakes"     | "Provider<Directory>"
    }

    @Unroll
    def "property query can filter input, replacing #value with #expected"() {
        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def writer = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        writer.filter({ it.replaceAll(value, expected) })
        def query = runPropertyQuery(writer)

        then:
        query.matches(expected)

        where:
        value          | expected
        "chocolate"    | "strawberry"
        "onrageminten" | "peanut butter"

        property = "pancakeFlavor"
    }

    @Unroll
    def "property query can filter stdout to only the task output, checking value '#value' -> '#expected'"() {
        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        expect:
        runPropertyQuery(getter).withoutAssertions().matches(expected)

        where:
        value | expected
        "foo" | "foo"
        "bar" | "bar"

        property = "pancakeFlavor"
        getter = new PropertyGetterTaskWriter("${subjectUnderTestName}.${property}")
                .filterTaskOutput()
    }
}
