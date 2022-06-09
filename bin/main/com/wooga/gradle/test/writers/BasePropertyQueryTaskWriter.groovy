package com.wooga.gradle.test.writers

import com.wooga.gradle.test.IntegrationHandler
import com.wooga.gradle.test.PropertyUtils

abstract class BasePropertyQueryTaskWriter extends BasePropertyWriter {

    String invocation

    BasePropertyQueryTaskWriter(String path, String invocation = ".getOrNull()", String taskName = null) {
        super(path)
        this.taskName = taskName ?: PropertyUtils.toCamelCase("query_${path}")
        this.invocation = invocation
    }

    /**
     * Writes to the build file
     */
    abstract void write(File file);

    @Override
    PropertyWrite write(IntegrationHandler spec) {
        write(spec.buildFile)
        new PropertyWrite(taskName)
    }
}
