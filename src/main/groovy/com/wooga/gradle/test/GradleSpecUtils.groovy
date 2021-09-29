package com.wooga.gradle.test


import java.util.regex.Pattern

class GradleSpecUtils {

    static final Pattern endTaskPattern = Pattern.compile("^.*task.*completed.\$")

    static String[] executedTasks(String gradleStdOut) {
        //example line: "> Task :sonarScannerInstall SKIPPED"
        def tasks = []
        def taskBaseStart = "> Task "
        gradleStdOut.readLines().each {
            if(it.stripIndent().startsWith(taskBaseStart)) {
                def taskName = it.replace(taskBaseStart, "").split(" ")[0].trim()
                tasks.add(taskName)
            }
        }
        return tasks
    }

    static String taskLog(String task, String stdOutput) {
        if(!task.startsWith(":")) {
            task = ":" + task
        }
        String taskString = "> Task ${task}"
        int taskBeginIdx = stdOutput.indexOf(taskString) + taskString.length()
        String taskTail = stdOutput.substring(taskBeginIdx)
        int taskEndIdx = taskEndIndex(taskTail, task).orElseThrow {
            new IllegalStateException("could not find task end match for task $task")
        }

        def logs = taskTail.substring(0, taskEndIdx)
        return taskString + logs
    }

    static Optional<Integer> taskEndIndex(String log, String taskName) {
        taskName = taskName.replace(":", "")
        def matcher = Pattern.compile(".*$taskName.*completed.+").matcher(log)
        if(matcher.find()) {
            return Optional.of(matcher.end(0))
        }
        return Optional.empty()
    }

    static String normalizeTaskName(String taskName) {
        if(taskName.contains(":")) {
            return taskName.substring(taskName.indexOf(":")+1)
        }
        return taskName
    }


}
