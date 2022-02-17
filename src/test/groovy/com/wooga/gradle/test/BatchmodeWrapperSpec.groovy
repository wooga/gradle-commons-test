package com.wooga.gradle.test

import com.wooga.gradle.ArgumentsSpec
import com.wooga.gradle.BaseSpec
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

        execResult.assertNormalExitValue()
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
}
