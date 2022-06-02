package com.wooga.gradle.test.writers

import com.wooga.gradle.test.IntegrationHandler

abstract class BasePropertyWriter {

    /**
     * The path to the property (such as foo.bar)
     */
    String path
    /**
     * If set, means that this is writing a gradle task
     */
    // TODO: Remove once we can, left due to older API referencing this directly
    String taskName

    BasePropertyWriter(String path) {
        this.path = path
    }

    abstract PropertyWrite write(IntegrationHandler spec)
}

/**
 *
 */
class PropertyWrite {
    /**
     * Tasks or command line options to be used as inputs by the integration as a result of the write operation
     */
    final List<String> arguments

    PropertyWrite(List<String> arguments) {
        this.arguments = arguments
    }

    PropertyWrite(String... arguments) {
        this(arguments.toList())
    }
}
