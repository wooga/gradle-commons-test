package com.wooga.gradle.test

import com.wooga.gradle.ArgumentsSpec
import com.wooga.gradle.test.mock.MockTask
import com.wooga.gradle.test.mock.MockTaskIntegrationSpec
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import spock.lang.Unroll

class BatchmodeWrapperUsingTask extends MockTask implements ArgumentsSpec {

    RegularFileProperty mockExecutable = objects.fileProperty()

    @Input
    RegularFileProperty getMockExecutable() {
        mockExecutable
    }

    void setMockExecutable(Provider<File> file) {
        mockExecutable.set(file)
    }

    @TaskAction
    def run() {
        ExecResult execResult = project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec exec) {

                if (!mockExecutable.present) {
                    throw new GradleException("No executable was set")
                }

                setEnvironmentDefaults()

                exec.with {
                    executable mockExecutable.get()
                    args arguments.get()
                    environment environment
                    ignoreExitValue = true
                }
            }
        })

        logger.info("exit value was ${execResult.exitValue}")
    }
}

class BatchmodeWrapperSpec extends MockTaskIntegrationSpec<BatchmodeWrapperUsingTask> {

    @Unroll
    def "generates default batch wrapper with printEnv = #printEnv, executes it expecting args `#expectedArgs` and env `#expectedEnv`"() {

        given:
        def wrapper = generateBatchWrapper(fileName, printEnv)
        for (kvp in env) {
            environmentVariables.set(kvp.key, kvp.value)
        }

        and:
        appendToSubjectTask("mockExecutable.set(file(${wrapValueBasedOnType(wrapper.absolutePath, String)}))")
        if (args != _) {
            appendToSubjectTask("arguments(${wrapValueBasedOnType(args, List)})")
        }

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        printEnv ? result.standardOutput.contains(expectedEnv) : true
        (args != _) ? result.standardOutput.contains(expectedArgs) : true

        where:
        fileName | printEnv | env            | expectedEnv | args           | expectedArgs
        "foobar" | true     | ["foo": "bar"] | "foo=bar"   | _              | ""
        "foobar" | false    | ["foo": "bar"] | ""          | _              | ""
        "foobar" | true     | ["foo": "bar"] | "foo=bar"   | ["cat", "dog"] | "cat dog"
        "foobar" | false    | ["foo": "bar"] | "foo=bar"   | _              | ""
    }

    @Unroll
    def "generates batch wrapper with custom text #text"() {

        given:
        def wrapper = new BatchmodeWrapper(fileName).withText(text).toTempFile()

        and:
        appendToSubjectTask("mockExecutable.set(file(${wrapValueBasedOnType(wrapper.absolutePath, String)}))")

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        (text != _) ? outputContains(result, text) : true

        where:
        fileName | text
        "foobar" | "how now brown cow"
        "foobar" | ""
    }

    @Unroll
    def "generates batch wrapper with custom exit value #value"() {
        given:
        def wrapper = new BatchmodeWrapper(fileName).withExitValue(value).toTempFile()

        and:
        appendToSubjectTask("mockExecutable.set(file(${wrapValueBasedOnType(wrapper.absolutePath, String)}))")

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.standardOutput.contains("exit value was ${value}")

        where:
        fileName | value
        "foobar" | 1
        "foobar" | 0
        "foobar" | 2
    }

    @Unroll
    def "can evaluate whether batch wrapper printed arguments #args, environment #env"() {
        given:
        def wrapper = new BatchmodeWrapper(fileName)
                .withEnvironment(printEnv)
                .toTempFile()

        and: "additional arguments and environment variables before"
        environmentVariables.set("TestKey", "TestValue")
        appendToSubjectTask("""arguments(["--test-value1", "true"])""")

        and:
        appendToSubjectTask("mockExecutable.set(file(${wrapValueBasedOnType(wrapper.absolutePath, String)}))")
        if (args != null) {
            appendToSubjectTask("arguments(${wrapValueBasedOnType(args, List)})")
        }
        if (env != null) {
            for (kvp in env) {
                environmentVariables.set(kvp.key, kvp.value)
            }
        }

        and: "additional arguments and environment variables after"
        environmentVariables.set("TestKey", "TestValue")
        appendToSubjectTask("""arguments(["--test-value2","true"])""")

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        //check for the base values so we are sure all arguments are in the output:
        BatchmodeWrapper.containsArguments(result.standardOutput, "--test-value1 true", "--test-value2 true")
        printEnv ? BatchmodeWrapper.containsEnvironment(result.standardOutput, ["TestKey": "TestValue"]) : true

        //check for the actual arguments we are interested in no specific order:
        BatchmodeWrapper.containsArguments(result.standardOutput, args)
        printEnv ? BatchmodeWrapper.containsEnvironment(result.standardOutput, env) : true

        if(args) {
            assert !BatchmodeWrapper.containsAllArguments(result.standardOutput, args)
            assert BatchmodeWrapper.containsAllArguments(result.standardOutput, ["--test-value1", "true"] + args + ["--test-value2", "true"])
        }

        where:
        args                        | printEnv | env
        ["foo", "bar"]              | false    | null
        ["foobar"]                  | false    | null
        ["--pancakes", "--waffles"] | false    | null
        null                        | true     | ["foo": "bar"]
        null                        | true     | ["foo": "bar", "pancakes": "waffles"]
        null                        | false    | ["foo": "bar"]
        ["cat", "dog"]              | true     | ["foo": "bar"]
        null                        | false    | ["foo": "bar"]
        fileName = "foobar"
    }
}
