package com.wooga.gradle.test.run.result

import com.wooga.gradle.test.GradleSpecUtils
import nebula.test.functional.ExecutionResult

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
            return [taskName: new TaskResult(taskName, taskLog, this)]
        })
    }

    TaskResult getAt(String taskName) {
        return this.tasks[taskName]
    }

    boolean compareExecutionOrder(String baseTaskName, String otherTaskName, Closure<Boolean> cmpOp) {
        return cmpOp(orderedTasks.findIndexOf {it == baseTaskName},
                        orderedTasks.findIndexOf {it == otherTaskName})
    }
}



