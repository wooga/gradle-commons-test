package com.wooga.gradle.test

import spock.lang.Unroll

class IntegrationSpecIntegrationSpec extends IntegrationSpec {

    @Unroll("Normalizes #prefix file path #filePath")
    def "handles project file paths as expected"() {

        given: "a file path"
        String baseDir = filePath.startsWith("/") ? diskVolume : projectDir
        String expected = absolute
            ? new File(diskVolume, filePath).path
            : new File(baseDir, filePath).path

        when:
        String actual = normalizePath(filePath)

        then:
        actual == expected

        where:
        filePath | absolute
        "/a/b/c" | true
        "a/b/c"  | false
        prefix = absolute ? "absolute" : "relative"
    }
}
