package com.wooga.gradle.test.writers

import nebula.test.Integration
import org.gradle.api.Action

/**
 * Provides functions for composing tasks onto a `build.gradle` script file
 */
trait TaskScriptWriter implements Integration {

    /**
     * @return The name of the variable holding a reference to the task that was written
     */
    String addTask(String name, Class type, Boolean force, String... lines) {
        addTask(name, type.name, force, lines)
    }

    /**
     * @return The name of the variable holding a reference to the task that was written
     */
    String addTask(String name, Class type, String... lines) {
        addTask(name, type.name, false, lines)
    }

    /**
     * @return The name of the variable holding a reference to the task that was written
     */
    String addTask(String name, String typeName, Boolean force, String... lines) {
        new CustomTaskWriter(name, typeName)
            .force(force)
            .withLines(lines)
            .write(buildFile)
    }

    /**
     * Set a task dependency where A depends on B
     */
    void setTaskDependency(String a, String b) {
        buildFile << """ ${a} { dependsOn ${b} }""".stripIndent()
    }

    /**
     * Appends the lines to the given task
     */
    void appendToTask(String taskName, String... lines) {
        buildFile << """
        $taskName {
            ${lines.join(System.lineSeparator())}
        }
        """.stripIndent()
    }

    /**
     * @param configure A closure to configure the writer
     * @return The name of the variable holding a reference to the task that was written
     */
    String writeTask(String name, String typeName, Action<CustomTaskWriter> configure = null) {
        def writer = new CustomTaskWriter(name, typeName)
        if (configure != null) {
            configure.execute(writer)
        }
        writer.write(buildFile)
    }

    /**
     * @param configure A closure to configure the writer
     * @return The name of the variable holding a reference to the task that was written
     */
    String writeTask(String name, Class type, Action<CustomTaskWriter> configure = null) {
        writeTask(name, type.name, configure)
    }
}
