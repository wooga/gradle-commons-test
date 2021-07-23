package com.wooga.gradle.test

enum PropertyLocation {
    none, script, property, environment

    String reason() {
        switch (this) {
            case script:
                return "value is provided in script"
            case property:
                return "value is provided in properties"
            case environment:
                return "value is set in environment"
            default:
                return "no value was configured"
        }
    }
}
