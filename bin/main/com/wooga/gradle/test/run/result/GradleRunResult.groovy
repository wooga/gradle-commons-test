package com.wooga.gradle.test.run.result

import com.wooga.gradle.test.GradleSpecUtils
import nebula.test.functional.ExecutionResult

import static com.wooga.gradle.test.GradleSpecUtils.normalizeTaskName

/**
 * Represents the execution results of a gradle execution. Task names can be  either ':task' or just 'task'.
 * Some usage examples below:
 * <br><br>
 * Creates a new gradle run from a gradle execution log
 * <pre>
 * {@code
 String log = "..."
 def result = new GradleRunResult(log)
 }
 * </pre>
 *
 * All tasks in execution order:
 * <pre>{@code result.orderedTasks}</pre>
 <br>
 String with execution logs for a given task {@code "taskName"}:
 * <pre>{@code result["taskName"].taskLog}</pre>
 <br>

 execution order comparision, all of those return booleans
 <pre>
  {@code
 result["dep1"].wasExecutedBefore("dep2", ":taskName")
 result["dep2"].wasExecutedBefore("taskName")
 result[":dep2"].wasExecutedAfter("dep1")
 result[":taskName"].wasExecutedAfter(":dep1", "dep2")
 }
 * </pre>
 */
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

    /**
     * gets the task results of a given task
     * @param taskName name of the task to get results from.
     * @return {@code TaskResult} object representing the gradle execution result for a single task,
     * null if no such task exists in the current execution
     */
    TaskResult getAt(String taskName) {
        return this.tasks[normalizeTaskName(taskName)]
    }
    /**
     * Compares the execution order of two tasks.
     * @param baseTaskName Base task
     * @param otherTaskName  task to be compared against base task
     * @param cmpOp - Closure {@code (int, int -> boolean)} where the comparison operation is done.
     * The int parameters are the task indexes of {@code baseTaskName} and {@code otherTaskName} respectively
     * @return boolean result of cmpOp
     */
    boolean compareExecutionOrder(String baseTaskName, String otherTaskName, Closure<Boolean> cmpOp) {
        return cmpOp(orderedTasks.findIndexOf {
                        normalizeTaskName(it) == normalizeTaskName(baseTaskName)},
                     orderedTasks.findIndexOf {
                        normalizeTaskName(it) == normalizeTaskName(otherTaskName)})
    }


}



