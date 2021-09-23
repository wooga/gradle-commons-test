package com.wooga.gradle.test

class ConventionSource {
    private interface ConventionSourceWriter {
        String writeConventionSourceSet(String value)
    }

    private static class ExtensionConventSource implements ConventionSourceWriter {
        private final String extensionName
        private final String property

        @Override
        String writeConventionSourceSet(String value) {
            "${extensionName}.${property} = ${value}"
        }

        ExtensionConventSource(String extensionName, String property) {
            this.extensionName = extensionName
            this.property = property
        }
    }

    private final ConventionSourceWriter impl

    private ConventionSource(ConventionSourceWriter impl) {
        this.impl = impl
    }

    static ConventionSource extension(String extensionName, String property) {
        new ConventionSource(new ExtensionConventSource(extensionName, property))
    }

    String write(String value) {
        impl.writeConventionSourceSet(value)
    }

    String write(File file, String value) {
        file << """${write(value)}"""
    }
}
