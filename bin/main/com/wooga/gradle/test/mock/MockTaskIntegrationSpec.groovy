package com.wooga.gradle.test.mock

import com.wooga.gradle.test.TaskIntegrationSpec

abstract class MockTaskIntegrationSpec<T extends MockTask> extends MockIntegrationSpec implements TaskIntegrationSpec<T> {

    def setup() {
        addMockTask(false)
    }

}
