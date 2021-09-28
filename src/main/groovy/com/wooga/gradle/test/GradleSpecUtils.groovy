package com.wooga.gradle.test

import groovy.json.StringEscapeUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GradleSpecUtils {



    static String[] executedTasks(String gradleStdOut) {
        //example line: "> Task :sonarScannerInstall SKIPPED"
        def tasks = []
        def taskBaseStart = "> Task "
        gradleStdOut.readLines().each {
            if(it.startsWith(taskBaseStart)) {
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
        int taskEndIdx = taskTail.indexOf("> Task")

        def logs = taskEndIdx > 0? taskTail.substring(0, taskEndIdx) : taskTail
        return taskString + logs
    }


}
