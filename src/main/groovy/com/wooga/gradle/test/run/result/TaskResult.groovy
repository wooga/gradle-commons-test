package com.wooga.gradle.test.run.result

class TaskResult {
    final String taskName
    final String taskLog
    final GradleRunResult runResult

    TaskResult(String taskName, String taskLog, GradleRunResult runResult) {
        this.taskName = taskName
        this.taskLog = taskLog
        this.runResult = runResult
    }

    boolean wasExecutedBefore(String otherTaskName) {
        return runResult.compareExecutionOrder(taskName, otherTaskName) {
            taskIndex, otherTaskIndex -> taskIndex < otherTaskIndex
        }
    }

    boolean wasExecutedAfter(String otherTaskName) {
        return runResult.compareExecutionOrder(taskName, otherTaskName) {
            taskIndex, otherTaskIndex -> taskIndex > otherTaskIndex
        }
    }
}