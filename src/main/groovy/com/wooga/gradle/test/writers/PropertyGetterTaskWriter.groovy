package com.wooga.gradle.test.writers

import com.wooga.gradle.test.GradleSpecUtils
import com.wooga.gradle.test.IntegrationHandler
import com.wooga.gradle.test.queries.PropertyQuery
import groovy.transform.InheritConstructors
import nebula.test.functional.ExecutionResult
import org.gradle.api.Action

import java.util.function.Function

/**
 * Writes a task for querying the value of a property
 * from a given path with the specified invocation.
 */
@InheritConstructors
class PropertyGetterTaskWriter extends BasePropertyQueryTaskWriter {

    //------------------------------------------------------------------------/
    // Properties
    //------------------------------------------------------------------------/
    final String separator = " : "
    final String pattern = "${path}${separator}"

    String typeName
    Action<PropertyQuery> queryConfiguration

    Function<String, String> onFilterStdout

    // By default, evaluate using script behavior
    PropertyEvaluation evaluation = new ScriptPropertyEvaluation()

    /**
     * Associates the type of property this query is checking.
     * It will inherit the typename, the serializers
     */
    PropertyGetterTaskWriter(PropertySetterWriter setter, String invocation = ".getOrNull()", String taskName = null) {
        super(setter.path, invocation, taskName)
        typeName = setter.typeName
        // TODO: Set default stdout filter
    }

    //------------------------------------------------------------------------/
    // Integration Methods
    //------------------------------------------------------------------------/
    /**
     * Writes the task to a gradle build file (In its DSL)
     * @param file The project build file (build.gradle) to write to
     */
    void write(File file) {

        file << """
            task(${taskName}) {
                doLast {
                    def value = ${path}${invocation}
                    println("${path}${separator}" + value)
                }
            }
        """.stripIndent()
    }

    /**
     * @param result The result of a task execution invocation
     * @return An object that contains the result and methods to check against it
     */
    PropertyQuery generateQuery(IntegrationHandler integration, ExecutionResult result) {

        String standardOutput = onFilterStdout != null
            ? onFilterStdout.apply(result.standardOutput)
            : result.standardOutput

        def query = new PropertyQuery(pattern, integration, standardOutput, evaluation, result.success)

        if (typeName != null) {
            query.associate(typeName)
        }
        if (queryConfiguration != null) {
            queryConfiguration.execute(query)
        }
        query
    }

    /**
     * Sets a callback that will invoked when the query object is constructed
     * by this writer (after the execution of the task)
     * @param onConfigure The callback that will configure the query
     */
    PropertyGetterTaskWriter configure(Action<PropertyQuery> onConfigure) {
        this.queryConfiguration = onConfigure
        this
    }

    /**
     * Filters the standard output with the given function
     */
    PropertyGetterTaskWriter filter(Closure<String> onFilterStdout) {
        this.onFilterStdout = onFilterStdout
        this
    }

    /**
     * Filters the standard output to that of the output produced by the task
     * this writer writes to the build file
     */
    PropertyGetterTaskWriter filterTaskOutput() {
        filter({ String stdout ->
            GradleSpecUtils.taskLog(taskName, stdout)
        })
    }

    /**
     * @return The evaluation to be used for checking the value of the property
     */
    PropertyGetterTaskWriter with(PropertyEvaluation evaluation) {
        this.evaluation = evaluation
        this
    }
}

