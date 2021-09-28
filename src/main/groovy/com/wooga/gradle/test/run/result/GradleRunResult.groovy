package com.wooga.gradle.test.run.result

import com.wooga.gradle.test.GradleSpecUtils
import nebula.test.functional.ExecutionResult

import static com.wooga.gradle.test.GradleSpecUtils.normalizeTaskName

class GradleRunResult {

    final String[] orderedTasks
    final Map<String, TaskResult> tasks

    GradleRunResult(ExecutionResult result) {
        this(result.standardOutput)
    }

    GradleRunResult(String stdOutput) {
        this.orderedTasks = GradleSpecUtils.executedTasks(stdOutput)
        this.tasks = Collections.unmodifiableMap(orderedTasks.collectEntries { taskName ->
            String taskLog = GradleSpecUtils.taskLog(taskName, stdOutput)
            return [(normalizeTaskName(taskName)): new TaskResult(taskName, taskLog, this)]
        })
    }

    TaskResult getAt(String taskName) {
        return this.tasks[normalizeTaskName(taskName)]
    }

    boolean compareExecutionOrder(String baseTaskName, String otherTaskName, Closure<Boolean> cmpOp) {
        return cmpOp(orderedTasks.findIndexOf {
                        normalizeTaskName(it) == normalizeTaskName(baseTaskName)},
                     orderedTasks.findIndexOf {
                        normalizeTaskName(it) == normalizeTaskName(otherTaskName)})
    }


}



