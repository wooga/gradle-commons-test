package com.wooga.gradle.test.run.result

import spock.lang.Specification

class GradleRunResultSpec extends Specification {

    def "gradle tasks shown on log are listed in order"() {
        given: "a gradle execution log with several tasks"
        def dependency1 = new FakeGradleLog.TaskContents("dep1")
        def dependency2 = new FakeGradleLog.TaskContents("dep2", "skipped")
        def taskName = new FakeGradleLog.TaskContents("taskName", "")
        def log = FakeGradleLog.basicLog([dependency1, dependency2, taskName])

        when:
        def result = new GradleRunResult(log)

        then:
        result.orderedTasks.toList() == [":dep1", ":dep2", ":taskName"]
    }

    def "can get log pertaining to a single gradle task execution"() {
        given: "a gradle execution log"
        def taskName = "taskName"
        def taskLog = "Hi I'm a log"
        def log = sampleLog(taskName, "Hi I'm a log")

        when: "creating a new run result object with log of ran task"
        def result = new GradleRunResult(log)

        then: "it contains the ran task logs"
        def expectedLog = """
        > Task :${taskName}
        ${taskLog}        
        """.stripIndent().trim()
        result[taskName].taskLog.startsWith(expectedLog)
    }

    def "can determine if one gradle task was ran before or after another"() {
        given: "a gradle execution log with several tasks"
        def dependency1 = "dep1"
        def dependency2 = "dep2"
        def taskName = "taskName"
        def log = sampleLog(taskName, "", dependency1, dependency2)

        when:
        def result = new GradleRunResult(log)

        then:
        result["dep1"].wasExecutedBefore("dep2", ":taskName")
        result["dep2"].wasExecutedBefore("taskName")
        result[":dep2"].wasExecutedAfter("dep1")
        result[":taskName"].wasExecutedAfter(":dep1", "dep2")
    }


    def sampleLog(String task, String taskLog, String... dependencies) {
        def tasks = dependencies.collect {new FakeGradleLog.TaskContents(it) }
        tasks.add(new FakeGradleLog.TaskContents(task, "", taskLog))

        return FakeGradleLog.basicLog(tasks)
    }
}
