package com.wooga.gradle.test

import com.sun.org.apache.xpath.internal.operations.Bool
import com.wooga.gradle.test.mock.MockTask
import com.wooga.gradle.test.mock.MockTaskIntegrationSpec
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import spock.lang.Unroll

class PropertyTask extends MockTask {

    private final Property<String> pancakeFlavor

    @Input
    Property<String> getPancakeFlavor() {
        pancakeFlavor
    }

    private final Property<Boolean> bake

    @Input
    Property<Boolean> getBake() {
        bake
    }

    PropertyTask() {
        pancakeFlavor = project.objects.property(String)
        bake = project.objects.property(Boolean)
    }
}

class PropertyQueryTaskWriterSpec extends MockTaskIntegrationSpec<PropertyTask> {

    @Unroll
    def "property query matches #value -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        result.success
        query.matches(result, expected)

        where:
        value  | expected
        "mint" | "mint"
        null   | null

        property = "pancakeFlavor"
    }

    @Unroll
    def "property query checks property with value '#value' is not null -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        result.success
        expected == query.isNotNull(result)

        where:
        value  | expected
        "mint" | true
        null   | false

        property = "pancakeFlavor"
    }

    @Unroll
    def "property query checks property with value '#value' is null -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.String(value))

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        result.success
        expected == query.isNull(result)

        where:
        value  | expected
        null   | true
        "mint" | false

        property = "pancakeFlavor"
    }

    @Unroll
    def "property query checks property with value '#value' is true -> #expected"() {

        given:
        setSubjectTaskProvider(property, WrappedValue.Boolean(value))

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        result.success
        expected == query.isTrue(result)

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
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        result.success
        expected == query.isFalse(result)

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
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        result.success
        expected == query.contains(result, substring)

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
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        result.success
        def actual = query.getValue(result)
        expected == actual

        where:
        value            | expected
        "chocolate_mint" | "chocolate_mint"
        //null             | null

        property = "pancakeFlavor"
    }


}
