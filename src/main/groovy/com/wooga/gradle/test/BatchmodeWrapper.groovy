package com.wooga.gradle.test

import com.wooga.gradle.PlatformUtils

import java.nio.file.Files

class BatchmodeWrapper {

    final String fileName
    Boolean printEnvironment = true
    String text
    Integer exitValue = 0

    BatchmodeWrapper(String fileName) {
        this.fileName = fileName
    }

    File toFile(String path) {
        File wrapper = new File(path, fileName)
        wrapper.createNewFile()
        write(wrapper)
        wrapper
    }

    File toTempFile() {
        File wrapper = Files.createTempFile(fileName, ".bat").toFile()
        write(wrapper)
        wrapper
    }

    BatchmodeWrapper withEnvironment(Boolean printEnvironment) {
        this.printEnvironment = printEnvironment
        this
    }

    BatchmodeWrapper withText(String text) {
        this.text = text
        this
    }

    BatchmodeWrapper withExitValue(Integer value) {
        this.exitValue = value
        this
    }

    void write(File wrapper) {

        // Set default options
        wrapper.deleteOnExit()
        wrapper.executable = true

        // Default write
        if (PlatformUtils.windows) {

            wrapper << """
                    @echo off
                    echo [ARGUMENTS]:
                    echo %*
                """.stripIndent()

            if (printEnvironment) {
                wrapper << """
                    echo [ENVIRONMENT]:
                    set
                """.stripIndent()
            }

        } else {
            wrapper << """
                    #!/usr/bin/env bash
                    echo [ARGUMENTS]:
                    echo \$@
                """.stripIndent()

            if (printEnvironment) {
                wrapper << """
                    echo [ENVIRONMENT]:
                    env
                """.stripIndent()
            }
        }

        // Additional text
        if (text) {
            wrapper << """
            echo ${text}            
            """.stripIndent()

        }

        // Custom write
        onWrite(wrapper)

        // Exit code
        wrapper << """
        exit ${exitValue}
        """.stripIndent()

        wrapper
    }

    /**
     * Override in a derived class to write your own
     * @param file The file that has been generated and written to
     */
    void onWrite(File file) {
    }
}
