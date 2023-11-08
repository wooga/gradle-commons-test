package com.wooga.gradle.test.wrapper

import com.wooga.gradle.PlatformUtils

class TypeSerializers {

    static GradleScriptWrapper populateWithSerializers(GradleScriptWrapper wrapper) {
        return wrapper.with {
            addSerializer("Closure", TypeSerializers.&closure)
            addSerializer("Callable", TypeSerializers.&callable)
            addSerializer("Object", TypeSerializers.&object)
            addSerializer("Directory", TypeSerializers.&directory)
            addSerializer("RegularFile", TypeSerializers.&regularFile)
            addSerializer("File", TypeSerializers.&file)
            addSerializer("Provider", TypeSerializers.&provider)
            addSerializer("Provider<RegularFile>", TypeSerializers.&regularFileProvider)
            addSerializer("Provider<Directory>", TypeSerializers.&directoryProvider)
            addSerializer(new TypeDescriptor("java.lang.String"), TypeSerializers.&string)
            addSerializer("String", TypeSerializers.&string)
            addSerializer(new TypeDescriptor("[]"), TypeSerializers.&array)
            addSerializer(new TypeDescriptor("..."), TypeSerializers.&varArgs)
            addSerializer(new TypeDescriptor("java.util.ArrayList"), TypeSerializers.&list)
            addSerializer("List", TypeSerializers.&list)
            addSerializer("Map", TypeSerializers.&map)
        }
    }

    static String closure(GradleScriptWrapper wrapper, TypeDescriptor type, Object rawValue) {
        def returnType = type.subtype ?: rawValue.class.typeName
        return "{${wrapper.wrap(rawValue, returnType)}}"
    }

    static String callable(GradleScriptWrapper wrapper, TypeDescriptor type, Object rawValue) {
        def returnType = type.subtype ?: rawValue.class.typeName
        return "new java.util.concurrent.Callable<${returnType}>() {@Override ${returnType} call() throws Exception {${wrapper.wrap(rawValue, returnType)}}}"
    }

    static String object(GradleScriptWrapper wrapper, TypeDescriptor _, Object rawValue) {
        "new Object() {@Override String toString() { ${wrapper.wrap(rawValue, "String")} }}"
    }

    static String directory(GradleScriptWrapper wrapper, TypeDescriptor _, Object rawValue) {
        "project.layout.projectDirectory.dir(${wrapper.wrap(rawValue, "String")})"
    }

    static String regularFile(GradleScriptWrapper wrapper, TypeDescriptor _, Object rawValue) {
        "project.layout.projectDirectory.file(${wrapper.wrap(rawValue, "String")})"
    }

    static String file(GradleScriptWrapper wrapper, TypeDescriptor _, Object rawValue) {
        "new File(${wrapper.wrap(rawValue, String.class)})"
    }

    static String provider(GradleScriptWrapper wrapper, TypeDescriptor type, Object rawValue) {
        "project.provider(${wrapper.wrap(rawValue, "Closure<${type.subtype}>")})"
    }

    static String regularFileProvider(GradleScriptWrapper wrapper, TypeDescriptor _, Object rawValue) {
        "project.provider{ project.layout.projectDirectory.file(${wrapper.wrap(rawValue, "String")}) }"
    }

    static String directoryProvider(GradleScriptWrapper wrapper, TypeDescriptor _, Object rawValue) {
        "project.provider{ project.layout.projectDirectory.dir(${wrapper.wrap(rawValue, "String")}) }"
    }

    static String string(GradleScriptWrapper _, TypeDescriptor __, Object rawValue) {
        "'${PlatformUtils.escapedPath(rawValue.toString())}'"
    }

    static String array(GradleScriptWrapper wrapper, TypeDescriptor type, Object rawValue) {
        "${wrapper.wrap(rawValue, "List<${type.subtype}>")} as ${type.subtype}[]"
    }

    static String varArgs(GradleScriptWrapper wrapper, TypeDescriptor type, Object rawValue) {
        "${rawValue.collect { wrapper.wrap(it, type.subtype) }.join(", ")}"
    }

    static String list(GradleScriptWrapper wrapper, TypeDescriptor type, Object rawValue) {
        def returnType = type.subtype ?: String.class.typeName
        // If the value is not a list itself, add it to a list
        if (rawValue instanceof List) {
            return "[${rawValue.collect { wrapper.wrap(it, returnType) }.join(", ")}]"
        } else {
            return "[${wrapper.wrap(rawValue, returnType)}]"
        }
    }

    static String map(GradleScriptWrapper wrapper, TypeDescriptor type, Object rawValue) {
        String value
        if (type.subtype) {
            def split = type.allSubtypes(2)
            def keyType = split.first()
            def valueType = split.last()
            value = "[" + rawValue.collect { k, v -> "${wrapper.wrap(k, keyType)} : ${wrapper.wrap(v, valueType)}" }.join(", ") + "]"
        } else {
            value = "[" + rawValue.collect { k, v -> "${wrapper.wrap(k, k.getClass())} : ${wrapper.wrap(v, v.getClass())}" }.join(", ") + "]"
        }
        return value == "[]" ? "[:]" : value
    }
}
