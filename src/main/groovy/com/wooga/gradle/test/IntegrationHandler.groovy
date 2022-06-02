package com.wooga.gradle.test

import org.junit.contrib.java.lang.system.EnvironmentVariables

interface IntegrationHandler {
    File getProjectDir()
    File createFile(String path)
    File buildFile
    EnvironmentVariables environmentVariables
}

class CustomIntegrationHandler implements IntegrationHandler {

    File projectDir

    CustomIntegrationHandler(File projectDir) {
        this.projectDir = projectDir
    }

    @Override
    File getProjectDir() {
        projectDir
    }

    @Override
    File createFile(String path) {
        new File(projectDir, path)
    }
}
