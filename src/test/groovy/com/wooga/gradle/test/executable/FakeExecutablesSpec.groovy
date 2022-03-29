package com.wooga.gradle.test.executable

import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

class FakeExecutablesSpec extends Specification {

    @Requires({ os.windows })
    @Unroll
    def "creates executable which logs its own arguments and environment on windows"() {
        given: "a file path"
        and: "a expected exit status"

        when: "creating and executing args reflector executable"
        def reflectorExec = FakeExecutables.argsReflector(filePath, exitStatus)
        String[] envs = environment.toArray(new String[0])
        def (process, log) = runWindowsBatScript(reflectorExec.executable, arguments, envs)

        then: "executable was executed"
        log.size() > 0
        log.contains("[[end ${filePath}.bat]]")
        log.contains("[[${filePath}.bat]]")

        and: "exit status is the expected one"
        exitStatus == process.exitValue()

        and: "used arguments are recorded on result"
        def result = reflectorExec.firstResult(log)
        result.args == arguments

        and: "used environment are recorded on result"
        result.envs.entrySet().containsAll(environment.collectEntries {
            return it.split("=")
        }.entrySet())

        where:
        filePath | exitStatus | arguments                   | environment
        "script" | 0          | ["arg1=a", "arg2", "arg:3"] | ["a=b"]
        "scrept" | 0          | ["arg", "--arg2"]           | ["a=b", "b=c"]
        "scropt" | 1          | ["arg1=a", "arg2", "arg:3"] | []
        "scrupt" | 1          | ["arg", "arg2-b"]           | ["c=d"]
    }


    @Requires({ !os.windows })
    @Unroll
    def "creates executable which logs its own arguments and environment on unix"() {
        given: "a file path"
        and: "a expected exit status"

        when: "creating and executing args reflector executable"
        def executable = FakeExecutables.argsReflector(filePath, exitStatus)
        String[] envs = environment.toArray(new String[0])
        def process = Runtime.getRuntime().
                exec("sh ${executable.executable.absolutePath} ${arguments.join(" ")}".toString(), envs)
        def log = stringFromStream(process.inputStream)
        process.waitFor(1000, TimeUnit.MILLISECONDS)

        then: "executable was executed"
        log.size() > 0
        log.contains("[[end ${filePath}]]")
        log.contains("[[${filePath}]]")
        and: "exit status is the expected one"
        exitStatus == process.exitValue()
        and: "used arguments are recorded on result"
        def result = executable.firstResult(log)
        result.args == arguments
        and: "used environment are recorded on result"
        result.envs.entrySet().containsAll(environment.collectEntries {
            return it.split("=")
        }.entrySet())

        where:
        filePath | exitStatus | arguments                   | environment
        "script" | 0          | ["arg1=a", "arg2", "arg:3"] | ["a=b"]
        "scrept" | 0          | ["arg", "--arg2"]           | ["a=b", "b=c"]
        "scropt" | 1          | ["arg1=a", "arg2", "arg:3"] | []
        "scrupt" | 1          | ["arg", "arg2-b"]           | ["c=d"]
    }

    @Requires({ os.windows })
    @Unroll
    def "creates custom reflector executable on windows"() {
        given: "a executable script file"
        File fakeExecutable = new File("${filePath}.bat")
        fakeExecutable.executable = true
        fakeExecutable.createNewFile()
        fakeExecutable.deleteOnExit()
        and: "some delimiter tokens"
        String[] argsTokens = ["[[startargs]]", "[[endargs]]"]
        String[] envTokens = ["[[startenv]]", "[[endenv]]"]
        and: "some exit code"

        when: "creating and executing args reflector executable"
        def reflectorExec = new ArgsReflectorExecutable(fakeExecutable, argsTokens, envTokens, exitStatus)
        String[] envs = environment.toArray(new String[0])
        def (process, log) = runWindowsBatScript(reflectorExec.executable, arguments, envs)

        then: "executable was executed"
        log.size() > 0
        log.contains("[[end ${filePath}.bat]]")
        log.contains("[[${filePath}.bat]]")
        argsTokens.every { log.contains(it) }
        envTokens.every { log.contains(it) }
        and: "exit status is the expected one"
        exitStatus == process.exitValue()
        and: "used arguments are recorded on result"
        def result = reflectorExec.firstResult(log)
        result.args == arguments
        and: "used environment are recorded on result"
        result.envs.entrySet().containsAll(environment.collectEntries {
            return it.split("=")
        }.entrySet())

        where:
        filePath      | exitStatus | arguments                   | environment
        "path"        | 0          | ["arg1=a", "arg2", "arg:3"] | ["a=b"]
        "other"       | 0          | ["arg", "--arg2"]           | ["a=b", "b=c"]
        "screept"     | 1          | ["arg1=a", "arg2", "arg:3"] | []
        "otherscript" | 1          | ["arg", "arg2-b"]           | ["c=d"]
    }

    @Requires({ os.windows })
    @Unroll
    def "creates script that runs executable passed as parameter"() {
        given: "a executable script file"
        def runFirstExec = FakeExecutables.runFirstParam(filePath)
        and: "a executable to pass as parameter"
        and: "parameters for this executable"
        when:
        def arguments = [fileToRun.executable.absolutePath] + toRunParams
        def (process, log) = runWindowsBatScript(runFirstExec, arguments, null)

        then: "run first param executable was executed"
        log.size() > 0
        log.contains("[[${filePath}.bat]]")
        and: "parameter executable was executed with remaining params"
        def result = fileToRun.firstResult(log)
        result.args == toRunParams
        process.exitValue() == expectedExitStatus
        where:
        filePath      | fileToRun                                           | toRunParams | expectedExitStatus
        "script"      | FakeExecutables.argsReflector("reflector", 0)       | ["a", "b"]  | 0
        "otherscript" | FakeExecutables.argsReflector("otherreflector", 10) | ["a", "b"]  | 10
    }


    String stringFromStream(InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line + System.lineSeparator());
        }
        return output.toString()
    }

    List<?> runWindowsBatScript(File script, List<String> arguments, String[] envs = null) {
        def process = Runtime.getRuntime().
                exec("cmd.exe /c \"${script.absolutePath} ${arguments.join(" ")}\"".toString(), envs)
        def log = stringFromStream(process.inputStream)
        process.waitFor(1000, TimeUnit.MILLISECONDS)
        return [process, log]
    }

}
