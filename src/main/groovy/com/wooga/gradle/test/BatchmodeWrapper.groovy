package com.wooga.gradle.test

import com.wooga.gradle.PlatformUtils

import java.nio.file.Files

class BatchmodeWrapper {

    final static String ARGUMENTS_START_MARKER = "[ARGUMENTS]:"
    final static String ARGUMENTS_END_MARKER = "[ARGUMENTS END]"
    final static String ENVIRONMENT_START_MARKER = "[ENVIRONMENT]:"
    final static String ENVIRONMENT_END_MARKER = "[ENVIRONMENT END]"

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
                    echo ${ARGUMENTS_START_MARKER}
                    echo %*
                    echo ${ARGUMENTS_END_MARKER}
                """.stripIndent()

            if (printEnvironment) {
                wrapper << """
                    echo ${ENVIRONMENT_START_MARKER}
                    set
                    echo ${ENVIRONMENT_END_MARKER}
                """.stripIndent()
            }

        } else {
            wrapper << """
                    #!/usr/bin/env bash
                    echo ${ARGUMENTS_START_MARKER}
                    echo \$@
                    echo ${ARGUMENTS_END_MARKER}
                """.stripIndent()

            if (printEnvironment) {
                wrapper << """
                    echo ${ENVIRONMENT_START_MARKER}
                    env
                    echo ${ENVIRONMENT_END_MARKER}
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
     * @return True if the text (emitted by this wrapper previously) contains the all given arguments in correct order.
     */
    static Boolean containsAllArguments(String standardOutput, Iterable<String> values, String separator = " ") {
        StringBuilder builder = new StringBuilder()
        builder.append("${ARGUMENTS_START_MARKER}${System.lineSeparator()}")

        if (values != null) {
            builder.append(values.join(separator))
        }

        String expected = builder.toString()
        standardOutput.contains(expected)
    }

    private static String findSection(String value, String startMarker, String endMarker = null) {
        def startIndex = value.indexOf(startMarker)
        if (startIndex < 0) {
            return null
        }

        def endIndex = (endMarker) ? value.indexOf(endMarker, startIndex) : Integer.MAX_VALUE
        if (endIndex < 0) {
            return null
        }
        value.substring(startIndex, endIndex)
    }

    /**
     * @param standardOutput The standard output a wrapper generated when executed
     * @param value A {@code String} value to check
     * @return True if the text (emitted by this wrapper previously) contains the argument string.
     */
    static Boolean containsArguments(String standardOutput, String... values) {
        containsArguments(standardOutput, values.toList())
    }

    /**
     * @param standardOutput The standard output a wrapper generated when executed
     * @param values A {@code Iterable<String>} value to check
     * @return True if the text (emitted by this wrapper previously) contains the argument strings in any order.
     */
    static Boolean containsArguments(String standardOutput, Iterable<String> values) {
        // Skip if not given
        if (values == null || values.size() == 0) {
            true
        }

        String start = "${ARGUMENTS_START_MARKER}${System.lineSeparator()}"
        String end = "${ARGUMENTS_END_MARKER}${System.lineSeparator()}"

        String arguments = findSection(standardOutput, start, end)
        if (!arguments) {
            return false
        }
        values.every { arguments.contains(it) }
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

        String start = "${ENVIRONMENT_START_MARKER}${System.lineSeparator()}"
        String end = "${ENVIRONMENT_END_MARKER}${System.lineSeparator()}"

        String environmentText = findSection(standardOutput, start, end)
        if (!environmentText) {
            return false
        }

        // Compose the environment that was printed
        for (kvp in env) {
            def printedKvp = "${kvp.key}=${kvp.value}"
            if (!environmentText.contains(printedKvp)) {
                return false
            }
        }

        true
    }
}
