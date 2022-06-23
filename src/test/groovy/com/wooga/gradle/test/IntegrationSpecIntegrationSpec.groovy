package com.wooga.gradle.test

import com.wooga.gradle.PropertyLookup

class MockConventionExtension {
    public static PropertyLookup name = new PropertyLookup("MOCK_FOOBAR", null, "barfoo")
}

class IntegrationSpecIntegrationSpec extends IntegrationSpec {

    def setupSpec() {
        clearEnvironmentVariables(MockConventionExtension)
    }

    def setup() {
        //environmentVariables.set("MOCK_FOOBAR", "foobar")
    }

    def "declared environment variables get nulled"() {
        expect:
        System.getenv("MOCK_FOOBAR") == null
    }

}
