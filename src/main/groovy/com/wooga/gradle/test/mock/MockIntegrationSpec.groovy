package com.wooga.gradle.test.mock


import com.wooga.gradle.test.IntegrationSpec

abstract class MockIntegrationSpec extends IntegrationSpec {

    def setup() {
        applyPlugin(MockPlugin)
    }
}

