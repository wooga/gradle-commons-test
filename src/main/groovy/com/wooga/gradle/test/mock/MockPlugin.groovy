package com.wooga.gradle.test.mock

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class MockPlugin implements Plugin<Project> {

    static final String extensionName = "mockExtension"
    static final String taskName = "mock"

    @Override
    void apply(Project project) {
        def extension = project.extensions.create(MockExtension, extensionName, MockExtension)
        project.tasks.withType(MockTask).configureEach { t ->
        }
    }
}

class MockExtension {
}

class MockTask extends DefaultTask {
}

