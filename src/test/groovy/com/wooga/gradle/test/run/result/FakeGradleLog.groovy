package com.wooga.gradle.test.run.result

import groovy.transform.MapConstructor


class FakeGradleLog {

    @MapConstructor
    static class TaskContents {
        String name;
        String status = ""
        String logs = ""

        TaskContents(String name, String status="", String logs="") {
            this.name = name
            this.status = status
            this.logs = logs
        }
    }

    List<TaskContents> tasks;

    FakeGradleLog(List<TaskContents> tasks) {
        this.tasks = tasks
    }

    static def basicLog(List<TaskContents> tasks) {
        new FakeGradleLog(tasks).basicLog()
    }

    def basicLog(String primaryTask = tasks.last().name) {
        return """
        > Configure project :
        All projects evaluated.
        Selected primary task '${primaryTask}' from project :
        Tasks to be executed: [${tasks.collect{"task ':${it.name}'"}.join(", ")}]
        Tasks that were excluded: []
        ${
            tasks.collect {taskExecutionLog(it)}.join(System.lineSeparator())
        }
        BUILD SUCCESSFUL in 9s
        1 actionable task: 1 executed
        """.stripIndent()
    }

    def taskExecutionLog(TaskContents task) {
        return """
        :${task.name} (Thread[Execution worker for ':' Thread 2,5,main]) started.
        ${taskDeclaration(task)}
        ${task.logs}        
        """.stripIndent()
    }

    String taskDeclaration(TaskContents task) {
        if(task.status.toUpperCase() == "SKIPPED") {
            return ":${task.name} SKIPPED".trim()
        }
        return "> Task :${task.name} ${task.status.toUpperCase()}".trim()
    }
}
