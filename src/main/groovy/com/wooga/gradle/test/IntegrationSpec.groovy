/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.wooga.gradle.test

import com.wooga.gradle.test.mock.MockExecutable
import com.wooga.gradle.test.queries.PropertyQuery
import com.wooga.gradle.test.writers.BasePropertyWriter
import com.wooga.gradle.test.writers.TaskScriptWriter
import com.wooga.gradle.test.writers.ValueWrapper
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import nebula.test.functional.ExecutionResult
import org.gradle.internal.impldep.org.apache.http.annotation.Obsolete
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.ProvideSystemProperty

import static com.wooga.gradle.PlatformUtils.windows

class IntegrationSpec extends nebula.test.IntegrationSpec
    implements IntegrationHandler,
        TaskScriptWriter,
        ValueWrapper {

    @Rule
    ProvideSystemProperty properties = new ProvideSystemProperty("ignoreDeprecations", "true")

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def setup() {
        environmentVariables.clear()
    }

    /**
     * @return A path normalized for the current operating system
     */
    static String osPath(String path) {
        if (isWindows()) {
            path = path.startsWith('/') ? "c:" + path : path
        }
        new File(path).path
    }

    /**
     * @param path The path to be composed
     * @return True if the file exists
     */
    Boolean fileExists(String... path) {
        fileExists(path.join(File.separator))
    }

    /**
     * @param fileName The name of the file
     * @param printEnvironment Whether to print the environment during execution
     * @return The file object
     */
    File createMockExecutable(String fileName, Boolean printEnvironment = false) {
        MockExecutable executable = new MockExecutable(fileName)
        executable.printEnvironment = printEnvironment
        executable.toTempFile()
    }

    /**
     * @deprecated Use the better named {@code createMockExecutable}
     */
    @Obsolete
    File generateBatchWrapper(String fileName, Boolean printEnvironment = false)  {
        createMockExecutable(fileName, printEnvironment)
    }

    /**
     * @param fileName The name of the file
     * @param printEnvironment Whether to print the environment during execution
     * @return The file object
     */
    File createMockExecutable(String fileName, String directoryPath, Boolean printEnvironment = false) {
        MockExecutable executable = new MockExecutable(fileName)
        executable.printEnvironment = printEnvironment
        executable.toDirectory(directoryPath)
    }

    /**
     * @deprecated Use the better named {@code createMockExecutable}
     */
    @Obsolete
    File generateBatchWrapper(String fileName, String directoryPath, Boolean printEnvironment = false)  {
        createMockExecutable(fileName, directoryPath, printEnvironment)
    }

    /**
     * @return True if the standard output or standard error contains the given text
     */
    Boolean outputContains(ExecutionResult result, String text) {
        result.standardOutput.contains(text) || result.standardError.contains(text)
    }

    /**
     * @param writers A set of writers
     * @param getterWriter A writer that generates a getter
     * @return The names of the tasks that were written to the build script
     */
    List<String> writeTasks(List<BasePropertyWriter> writers, PropertyGetterTaskWriter getterWriter = null) {

        List<String> taskNames = new ArrayList<String>()

        // Write all the task/arguments generated by the other writers
        for (writer in writers) {
            def write = writer.write(this)
            // Returning a result is optional
            if (write != null) {
                taskNames.addAll(write.arguments)
            }
        }

        // Always add the task from the query last
        if (getterWriter != null) {
            taskNames.addAll(getterWriter.write(this).arguments)
        }

        taskNames
    }

    /**
     * Runs a primary task, as well any tasks generated by the given writers,
     * throwing if not successful
     * @return The result of the execution
     */
    ExecutionResult runTasksSuccessfully(String taskName, List<BasePropertyWriter> writers) {
        List<String> tasksToRun = new ArrayList<String>()
        tasksToRun.add(taskName)
        tasksToRun.addAll(writeTasks(writers))
        runTasksSuccessfully(*tasksToRun)
    }

    /**
     * Runs a primary task, as well a task generated by the given writer,
     * throwing if not successful
     * @return The result of the execution
     */
    ExecutionResult runTasksSuccessfully(String taskName, BasePropertyWriter writer) {
        runTasksSuccessfully(taskName, [writer])
    }

    /**
     * Runs a generated task that will query the value of a single property
     * after a writer has modified the build file before execution
     * @return The result of the query
     */
    PropertyQuery runPropertyQuery(PropertyGetterTaskWriter queryTaskWriter, BasePropertyWriter... additional) {
        runPropertyQuery(queryTaskWriter, additional.toList())
    }

    /**
     * Runs a generated task that will query the value of a single property
     * after a set of writers have modified the build file before execution
     * @return The result of the query
     */
    PropertyQuery runPropertyQuery(PropertyGetterTaskWriter queryTaskWriter, List<BasePropertyWriter> writers) {
        List<String> tasks = writeTasks(writers, queryTaskWriter)
        def exec = runTasksSuccessfully(*tasks)
        queryTaskWriter.generateQuery(this, exec)
    }

    /**
     * Runs a primary task and a generated task that will query the value of a single property
     * after a set of writers have modified the build file before execution
     * @return The result of the query
     */
    PropertyQuery runPropertyQuery(String taskName, PropertyGetterTaskWriter queryTaskWriter, List<BasePropertyWriter> writers) {
        List<String> tasks = writeTasks(writers, queryTaskWriter)
        tasks.add(0, taskName)
        def exec = runTasksSuccessfully(*tasks)
        queryTaskWriter.generateQuery(this, exec)
    }

    /**
     * Runs a primary task and a generated task that will query the value of a single property
     * after a writer has modified the build file before execution
     * @return The result of the query
     */
    PropertyQuery runPropertyQuery(String taskName, PropertyGetterTaskWriter queryTaskWriter, BasePropertyWriter... additional) {
        runPropertyQuery(taskName, queryTaskWriter, additional.toList())
    }
}




