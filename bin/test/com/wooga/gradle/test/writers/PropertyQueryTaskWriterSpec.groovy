package com.wooga.gradle.test.writers


import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.WrappedValue
import com.wooga.gradle.test.mock.MockTaskIntegrationSpec
import spock.lang.Unroll

/**
 * Test mostly for legacy API
 */
class PropertyQueryTaskWriterSpec extends MockTaskIntegrationSpec<PropertyTask> {

    @Unroll
    def "property query matches #value -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def writer = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        writer.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, writer.taskName)

        then:
        match == writer.matches(result, expected)

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
        def writer = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        writer.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, writer.taskName)

        then:
        expected == writer.isNull(result)

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
        def writer = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        writer.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, writer.taskName)

        then:
        expected == writer.isNotNull(result)

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
        def writer = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        writer.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, writer.taskName)

        then:
        expected == writer.isTrue(result)

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
        def writer = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        writer.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, writer.taskName)

        then:
        expected == writer.isFalse(result)

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
        def writer = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        writer.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, writer.taskName)

        then:
        expected == writer.contains(result, substring)

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
        def writer = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        writer.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, writer.taskName)

        then:
        expected == writer.getValue(result)

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
        def writer = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        writer.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, writer.taskName)

        then:
        expected == writer.getValue(result)

        where:
        value            | expected         | setter
        "chocolate_mint" | "chocolate_mint" | PropertySetInvocation.assignment
        "strawberry"     | "strawberry"     | PropertySetInvocation.providerSet

        property = "pancakeFlavor"
    }
}

