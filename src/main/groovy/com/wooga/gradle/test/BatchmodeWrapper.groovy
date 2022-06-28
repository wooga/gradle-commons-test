package com.wooga.gradle.test

import com.wooga.gradle.PlatformUtils
import org.spockframework.lang.Wildcard

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

    BatchmodeWrapper withEnvironment(Boolean printEnvironment = true) {
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

    /**
     * @param standardOutput The standard output a wrapper generated when executed
     * @return True if the text (emitted by this wrapper previously) contains the given arguments
     */
    static Boolean containsArguments(String standardOutput, List<String> values, String separator = " ") {
        StringBuilder builder = new StringBuilder()
        builder.append("[ARGUMENTS]:${System.lineSeparator()}")

        if (values != null) {
            builder.append(values.join(separator))
        }

        String expected = builder.toString()
        standardOutput.contains(expected)
    }

    /**
     * @param standardOutput The standard output a wrapper generated when executed
     * @return True if the text (emitted by this wrapper previously) contains the given environment variables
     */
    static Boolean containsEnvironment(String standardOutput, Map<String, ?> env) {

        // Skip if not given
        if (env == null || env.size() == 0) {
            true
        }

        // Only check the substring start from the environment
        String environmentDeclaration = "[ENVIRONMENT]:${System.lineSeparator()}"
        def environmentStartIndex = standardOutput.indexOf(environmentDeclaration)
        if (environmentStartIndex < 0) {
            return false
        }
        String environmentText = standardOutput.substring(environmentStartIndex)

        // Compose the environment that was printed
        for (kvp in env) {
            def printedKvp = "${kvp.key}=${kvp.value}"
            if (!environmentText.contains(printedKvp)){
                return false
            }
        }

        true
    }
}
