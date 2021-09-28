package com.wooga.gradle.test

import groovy.json.StringEscapeUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SpecUtils {

    static File emptyTmpFile(String first, String... more) {
        return emptyTmpFile(Paths.get(first, more))
    }

    static File emptyTmpFile(Path path) {
        File file = path.toFile()
        file.createNewFile()
        file.deleteOnExit()
        return file
    }

    static File emptyTmpFile() {
        return Files.createTempFile("tmp", "").toFile()
    }

    static String escapedPath(String path) {
        if (isWindows()) {
            return StringEscapeUtils.escapeJava(path)
        }
        return path
    }

    static Throwable rootCause(Throwable e) {
        if(e.cause == null || e.cause == e) {
            return e
        }
        return rootCause(e.cause)
    }

    static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows")
    }

    static boolean isUnix() {
        return System.getProperty("os.name").toLowerCase().contains("linux") ||
                System.getProperty("os.name").toLowerCase().contains("unix")
    }

    static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac")
    }

}
