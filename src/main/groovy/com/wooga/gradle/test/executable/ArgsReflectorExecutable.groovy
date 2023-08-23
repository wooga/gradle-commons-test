package com.wooga.gradle.test.executable

import com.wooga.gradle.test.run.result.TaskResult
import nebula.test.functional.ExecutionResult
import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils

import static com.wooga.gradle.test.SpecUtils.isWindows

/**
 Represents a script executable that "reflects" (prints) all its arguments and current environment at execution time.
 Some ussage examples below:

 <br><br>
 Creates a `ArgsReflectorExecutable` object:
 <pre>{@code def reflectorExec = FakeExecutables.argsReflector("filePath", exitStatus)}</pre>
 <br>
 Executable file that should be used to run this object somewhere
 <pre>{@code reflectorExec.executable}</pre>
 <br>

 First execution result of this file in a given log:
 <pre>{@code ArgsReflectorExecutable.Result result = reflectorExec.firstResult(log)}</pre>
 <br>

 You can also fetch all results inside a given log like this:
 <pre>{@code List<ArgsReflectorExecutable.Result>   results = reflectorExec.allResults(log)}</pre>
 <br>

 With an ArgsReflectorExecutable.Result object, you can get:
 <pre>{@code
 result.args //List<String> of arguments passed to this executable
 result.envs //Map<Sring, String> representing the environment at the executable execution}</pre>

 */
class ArgsReflectorExecutable {

    final String[] argsTokens
    final String[] envTokens
    final String[] fileTokens
    /**
     * executable file that should be used to run this object somewhere
     */
    final File executable

    ArgsReflectorExecutable(File fakeExec, String[] argsTokens, String[] envTokens, int exitCode) {
        this.envTokens = envTokens
        this.argsTokens = argsTokens
        this.fileTokens = ["[[${fakeExec.name}]]", "[[end ${fakeExec.name}]]"]
        this.executable = write(fakeExec, fileTokens, envTokens, argsTokens, exitCode)

    }

    private static File write(File executable, String[] fileTokens, String[] envTokens, String[] argsTokens, int exitCode) {
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
                echo ${fileTokens[0]}
                echo ${argsTokens[0]}
                echo \$@
                echo ${argsTokens[1]}

                echo ${envTokens[0]}
                env
                echo ${envTokens[1]}
                echo ${fileTokens[1]}
                exit ${exitCode}
            """.stripIndent()
        }
    }

    /**
     * @param result nebula integration test exectuion result object
     * @return first execution result from the given gradle execution, null if none was found
     */
    Result firstResult(ExecutionResult result) {
        return firstResult(result.standardOutput)
    }
    /**
     * @param result nebula integration test exectuion result object
     * @return all execution results from the given gradle execution
     */
    Result[] allResults(ExecutionResult result) {
        return allResults(result.standardOutput)
    }
    /**
     * @param taskResult TaskResult object representing a gradle task execution
     * @return first execution result from given task, null if none was found
     */
    Result firstResult(TaskResult taskResult) {
        return firstResult(taskResult.taskLog)
    }
    /**
     * @param taskResult TaskResult object representing a gradle task execution
     * @return all execution results from given task
     */
    Result[] allResults(TaskResult taskResult) {
        return allResults(taskResult.taskLog)
    }
    /**
     * @param stdOutput log string that contains the result of a reflector executable execution
     * @return first execution result within given log, null if none was found
     */
    Result firstResult(String stdOutput) {
        def currentIndex = stdOutput.indexOf(fileTokens[0])
        if (currentIndex > -1) {
            return new Result(stdOutput)
        }
        return null
    }
    /**
     * @param stdOutput log string that contains the result of a reflector executable execution
     * @return all execution results within given log
     */
    Result[] allResults(String stdOutput) {
        def results = new ArrayList<Result>()
        def currentLog = stdOutput
        while(true) {
            def currentIndex = currentLog.indexOf(fileTokens[0])
            if(currentIndex < 0) {
                return results
            }
            results.add(new Result(currentLog))
            currentLog = currentLog.substring(currentIndex + fileTokens[0].size(), currentLog.size())

        }
    }


    class Result {
        /**
         * list of arguments passed to a reflector executable
         */
        final ArrayList<String> args;
        /**
         * environment at reflector executable execution
         */
        final Map<String, String> envs;

        Result(String reflectorStdOut) {
            def fileLog = substringBetween(reflectorStdOut, fileTokens[0], fileTokens[1])
            this.args = loadArgs(fileLog, argsTokens[0], argsTokens[1])
            this.envs = loadEnvs(fileLog, envTokens[0], envTokens[1])
        }

        private static ArrayList<String> loadArgs(String stdOutput,
                                                  String argumentStartToken, String argumentEndToken) {
            def lastExecutionOffset = stdOutput.lastIndexOf(argumentStartToken)
            if (lastExecutionOffset < 0) {
                System.out.println(stdOutput)
                throw new IllegalArgumentException("stdout couldn't match given arguments token ${argumentStartToken}")
            }
            def lastExecTailString = stdOutput.substring(lastExecutionOffset)
            def argsString = substringBetween(lastExecTailString, argumentStartToken, argumentEndToken)
            def parts = argsString.split(" ").
                    findAll { !StringUtils.isEmpty(it) }.collect { it.trim() }
            return parts
        }

        private static Map<String, String> loadEnvs(String stdOutput,
                                                    String environmentStartToken, String environmentEndToken) {
            def argsString = substringBetween(stdOutput, environmentStartToken, environmentEndToken)
            def parts = argsString.split(System.lineSeparator()).
                    findAll { !StringUtils.isEmpty(it) }.collect { it.trim() }
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
