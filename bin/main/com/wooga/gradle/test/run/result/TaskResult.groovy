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
    /**
     * Checks if given tasks were executed before this one
     * @param otherTaskNames task names of tasks to compare against this one
     * @return true if all given tasks were executed before this, false otherwise
     */
    boolean wasExecutedBefore(String... otherTaskNames) {
        return otherTaskNames.every {otherTaskName ->
            return runResult.compareExecutionOrder(taskName, otherTaskName) {
                taskIndex, otherTaskIndex -> taskIndex < otherTaskIndex
            }
        }
    }
    /**
     * Checks if given tasks were executed after this one
     * @param otherTaskNames task names of tasks to compare against this one
     * @return true if all given tasks were executed after this, false otherwise
     */
    boolean wasExecutedAfter(String... otherTaskNames) {
        return otherTaskNames.every {otherTaskName ->
            return runResult.compareExecutionOrder(taskName, otherTaskName) {
                taskIndex, otherTaskIndex -> taskIndex > otherTaskIndex
            }
        }
    }
}
