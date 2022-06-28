package com.wooga.gradle.test.writers

/**
 * Writes a gradle task onto a build script
 */
class CustomTaskWriter {

    /**
     * The name of the task (as known by the gradle project)
     */
    String name
    /**
     * The type of the task
     */
    String typeName
    /**
     * The lines to be written into the task declaration
     */
    List<String> lines

    Boolean _force
    Boolean _register

    CustomTaskWriter(String name, String typeName) {
        this.name = name
        this.typeName = typeName
        this.lines = new ArrayList<String>()
    }

    CustomTaskWriter(String name, Class type) {
        this(name, type.name)
    }

    CustomTaskWriter withLines(Iterable<String> values) {
        if (values != null) {
            this.lines.addAll(values)
        }
        this
    }

    CustomTaskWriter withLines(String... values) {
        if (values != null) {
            this.lines.addAll(values)
        }
        this
    }

    /**
     * Forces this task to always run (not get skipped)
     */
    CustomTaskWriter force(Boolean value = true) {
        _force = value
        this
    }

    /**
     * Uses the newer task registration API when creating this task
     */
    CustomTaskWriter register(Boolean value = true) {
        _register = value
        this
    }

    /**
     * Adds a task dependency
     */
    CustomTaskWriter dependsOn(String taskName) {
        withLines("dependsOn ${taskName}")
    }

    /**
     * @param buildFile The gradle build script to write to
     * @return The name of the variable referencing this task
     */
    def write(File buildFile) {
        String variableName = composeVariableName(name)
        StringBuilder builder = new StringBuilder()
        String newLine = System.lineSeparator()

        if (_register) {
            builder.append("""
def ${variableName} = tasks.register(\"${name}\"${typeName != null ? ", ${typeName}" : ""}) {
""")
        } else {
            builder.append("""
def ${variableName} = task (${name}, type: ${typeName}) {
""")
        }

        if (_force) {
            builder.append("onlyIf = {true}${newLine}")
        }

        if (lines.size() > 0) {
            builder.append("${lines.join(newLine)}${newLine}")
        }

        builder.append("}${newLine}")
        buildFile << builder.toString()
        variableName
    }

    static String composeVariableName(String taskName) {
        "${taskName}Task"
    }
}
