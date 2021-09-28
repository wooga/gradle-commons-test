package com.wooga.gradle.test.executable

import com.wooga.gradle.test.run.result.TaskResult
import nebula.test.functional.ExecutionResult
import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils

import static com.wooga.gradle.test.GradleSpecUtils.isWindows

class ArgsReflectorExecutable {

    final String[] argsTokens
    final String[] envTokens
    final String[] fileTokens
    final File executable

    ArgsReflectorExecutable(File fakeExec, String[] argsTokens, String[] envTokens, int exitCode) {
        this.envTokens = envTokens
        this.argsTokens = argsTokens
        this.fileTokens = ["[[${fakeExec.name}]]", "[[end ${fakeExec.name}]]"]
        this.executable = write(fakeExec, fileTokens, exitCode)

    }

    private static File write(File executable, String[] fileTokens, int exitCode) {
        if (isWindows()) {
            executable << """
                @echo off
                echo ${fileTokens[0]}
                echo ${argsTokens[0]}
                echo %*
                echo ${argsTokens[1]}
                echo ${envTokens[0]}
                set
                echo ${envTokens[1]}
                echo ${fileTokens[1]}
                exit ${exitCode}
            """.stripIndent()
        } else {
            executable << """
                #!/usr/bin/env bash
                echo [[${executable.name}]]
                echo [[arguments]]
                echo \$@
                echo [[environment]]
                env
                echo [[end]]
                echo [[end ${executable.name}]]
                exit ${exitCode}
            """.stripIndent()
        }
    }


    Result firstResult(ExecutionResult result) {
        return firstResult(result.standardOutput)
    }

    Result[] allResults(ExecutionResult result) {
        return allResults(result.standardOutput)
    }

    Result firsResult(TaskResult taskResult) {
        return firstResult(taskResult.taskLog)
    }

    Result[] allResults(TaskResult taskResult) {
        return allResults(taskResult.taskLog)
    }

    Result firstResult(String stdOutput) {
        def currentIndex = stdOutput.indexOf(fileTokens[0])
        if(currentIndex > -1) {
            return new Result(stdOutput)
        }
        return null
    }

    Result[] allResults(String stdOutput) {
        def results = new ArrayList<Result>()
        def log = stdOutput
        def currentIndex = log.indexOf(fileTokens[0])
        while(currentIndex > -1) {
            def currentLog = log.substring(currentIndex)
            results.add(new Result(currentLog))
            currentIndex = currentLog.substring(currentIndex + fileTokens[0].length()).
                    indexOf(fileTokens[0])
        }
        return results
    }


    class Result {
        final ArrayList<String> args;
        final Map<String, String> envs;

        Result(String stdOutput) {
            def fileLog = substringBetween(stdOutput, fileTokens[0], fileTokens[1])
            this.args = loadArgs(fileLog, argsTokens[0], argsTokens[1])
            this.envs = loadEnvs(fileLog, envTokens[0], envTokens[1])
        }

        private static ArrayList<String> loadArgs(String stdOutput,
                                                  String argumentStartToken, String argumentEndToken) {
            def lastExecutionOffset = stdOutput.lastIndexOf(argumentStartToken)
            if(lastExecutionOffset < 0) {
                System.out.println(stdOutput)
                throw new IllegalArgumentException("stdout couldn't match given arguments token ${argumentStartToken}")
            }
            def lastExecTailString = stdOutput.substring(lastExecutionOffset)
            def argsString = substringBetween(lastExecTailString, argumentStartToken, argumentEndToken)
            def parts = argsString.split(" ").
                    findAll {!StringUtils.isEmpty(it) }.collect{ it.trim() }
            return parts
        }

        private static Map<String, String> loadEnvs(String stdOutput,
                                                    String environmentStartToken, String environmentEndToken) {
            def argsString = substringBetween(stdOutput, environmentStartToken, environmentEndToken)
            def parts = argsString.split(System.lineSeparator()).
                    findAll {!StringUtils.isEmpty(it) }.collect{ it.trim() }
            return parts.collectEntries {
                return it.split("=", 2)
            }
        }

        private static String substringBetween(String base, String from, String to) {
            def customArgsIndex = base.indexOf(from)
            def tailString = base.substring(customArgsIndex)
            def endIndex = tailString.indexOf(to)
            return tailString.substring(from.length(), endIndex)
        }
    }

}
