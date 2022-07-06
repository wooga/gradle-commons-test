package com.wooga.gradle.test


import org.gradle.api.DefaultTask
import spock.lang.Unroll

class IntegrationSpecIntegrationSpec extends IntegrationSpec {

    @Unroll
    def "can write task with name #taskName of type #type"() {
        given:
        def task = writeTask(taskName, type) {
            it.force(force)
                .register(register)
                .withLines(lines)
        }

        when:
        def result = runTasks(taskName)

        then:
        result.success

        where:
        taskName | type        | register | force | lines
        "foobar" | DefaultTask | false    | true  | null
        "foobar" | DefaultTask | false    | false | null
        "foobar" | DefaultTask | false    | false | """println("foo")"""
        "foobar" | DefaultTask | true     | true  | null
        "foobar" | DefaultTask | true     | false | """println("foo24")"""
    }

    def "can add task"() {
        given:
        def task = addTask(taskName, DefaultTask, true)

        expect:
        runTasksSuccessfully(taskName)
        task == "foobarTask"

        where:
        taskName = "foobar"
    }

    def "can append to task"() {
        given:
        def task = addTask(taskName, DefaultTask, true)
        appendToTask(taskName, """println("foo")""")

        when:
        def result = runTasksSuccessfully(taskName)

        then:
        result.standardOutput.contains("foo")

        where:
        taskName = "foobar"
    }

    @Unroll
    def "can set task dependency directly(#directly)"() {
        given: "a task A that prints a message"
        def aTaskName = "A"
        def a = writeTask(aTaskName, DefaultTask, {
            it.withLines("""println("${message}")""")
        })

        and: "a task that depends on A"
        def bTaskName = "B"
        def b = writeTask(bTaskName, DefaultTask, {
            if (directly) {
                it.dependsOn(a)
            }
        })
        if (!directly) {
            setTaskDependency(bTaskName, aTaskName)
        }

        when: "running B will have A executed, printing foo"
        def result = runTasksSuccessfully(bTaskName)

        then:
        result.standardOutput.contains(message)

        where:
        message | directly
        "foo"   | true
        "bar"   | false


    }

}
