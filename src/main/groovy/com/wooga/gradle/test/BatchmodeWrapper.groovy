package com.wooga.gradle.test


import com.wooga.gradle.test.mock.MockExecutable
import org.gradle.internal.impldep.org.apache.http.annotation.Obsolete

/**
 * @deprecated Replaced by {@link com.wooga.gradle.test.mock.MockExecutable}, whose name brings more clarity to its use case.
 */
@Obsolete
class BatchmodeWrapper extends MockExecutable {
    BatchmodeWrapper(String fileName) {
        super(fileName)
    }
}
