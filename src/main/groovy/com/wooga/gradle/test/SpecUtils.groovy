package com.wooga.gradle.test

import com.wooga.gradle.PlatformUtils
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

    @Deprecated
    /**
     * @deprecated Please use PlatformUtils.escapedPath instead
     */
    static String escapedPath(String path) {
        PlatformUtils.escapedPath(path)
    }

    static Throwable rootCause(Throwable e) {
        if(e.cause == null || e.cause == e) {
            return e
        }
        return rootCause(e.cause)
    }

    @Deprecated
    /**
     * @deprecated Please use the function declared in PlatformUtils instead
     */
    static boolean isWindows() {
        PlatformUtils.windows
    }

    // TODO: Update the check on the PlatforUtils one
    static boolean isUnix() {
        return PlatformUtils.linux ||
                System.getProperty("os.name").toLowerCase().contains("unix")
    }

    @Deprecated
    /**
     * @deprecated Please use the function declared in PlatformUtils instead
     */
    static boolean isMacOS() {
        PlatformUtils.mac
    }

}
