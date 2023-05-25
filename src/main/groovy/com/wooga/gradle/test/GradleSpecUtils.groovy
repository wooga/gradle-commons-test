package com.wooga.gradle.test

import java.util.regex.Pattern


class GradleSpecUtils {

    final static Pattern taskStartLinePattern = Pattern.compile(/^(> Task )?(?<name>:[^ ]+) ?(?<status>[^ ]*)$/, Pattern.MULTILINE)


    static String[] executedTasks(String gradleStdOut) {
        //example line: "> Task :sonarScannerInstall UP-TO-DATE" or ":skippedTask SKIPPED"
        def tasks = []
        gradleStdOut.readLines().each {
            def taskData = nextTaskStartData(it)
            if(taskData.isPresent()) {
                tasks.add(taskData.get().name)
            }
        }
        return tasks
    }

    static String taskLog(String task, String stdOutput) {
        if(!task.startsWith(":")) {
            task = ":" + task
        }
        int taskBeginIdx = nextTaskStart(stdOutput, task).orElseThrow {
            new IllegalStateException("could not find task log match for task $task")
        }
        def taskTail = stdOutput.substring(taskBeginIdx).trim()
        def noTaskDeclTail = taskTail.readLines().with {
            it.pop()
            return it.join("\n")
        }
        int taskEndIdx = nextTaskStart(noTaskDeclTail).orElse(taskTail.length())

        def logs = taskTail.substring(0, taskEndIdx)
        return logs
    }

    static Optional<Integer> nextTaskStart(String log, String task=null, boolean endIdx = false) {
        return nextTaskStartData(log, task).map {endIdx? it.endIdx as int : it.startIdx as int }
    }

    private static Optional<Map> nextTaskStartData(String log, String task=null) {
        def pattern = taskStartLinePattern
        if(task) {
            pattern = Pattern.compile(/^(> Task )?(?<name>${task}) ?(?<status>[^ ]*)$/, Pattern.MULTILINE)
        }
        def matcher = pattern.matcher(log)
        def idx = -1
        if(matcher.find()) {
            return Optional.of([
                    name: matcher.group("name"),
                    status: matcher.group("status"),
                    startIdx: matcher.start(),
                    endIdx: matcher.end()
            ])
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
