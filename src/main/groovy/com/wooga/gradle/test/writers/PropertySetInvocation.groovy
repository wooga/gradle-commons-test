package com.wooga.gradle.test.writers

import com.wooga.gradle.test.PropertyUtils
import org.gradle.api.Action

/**
 * Defines how a property has its value set in a script
 */
abstract class PropertySetInvocation {

    abstract String getDefinition()

    /**
     * No operation
     */
    static PropertySetInvocation none = new NonePropertySetInvocation()
    /**
     * foobar.set(value)
     */
    static PropertySetInvocation providerSet = new ProviderPropertySetInvocation()
    /**
     * foobar = value
     */
    static PropertySetInvocation assignment = new AssignmentPropertySetInvocation()
    /**
     * setFoobar(value)
     */
    static PropertySetInvocation setter = new SetterPropertySetInvocation()
    /**
     * Treat the given property name as a method name: "foobar(value)"
     */
    static PropertySetInvocation method = new MethodPropertySetInvocation()

    /**
     * @param path The path to the property
     * @param wrappedValue The value to be set, already escaped/wrapped for gradle script
     * @return A composed script invocation that will set the value of the property
     */
    abstract String compose(String path, String wrappedValue);

    @Override
    String toString() {
        definition
    }

    /**
     * @param name The name of the setter
     * @return A custom setter
     */
    static PropertySetInvocation customSetter(String name) {
        new CustomSetterPropertySetInvocation(name)
    }

    /**
     * @param name The invocation to use
     * @return An invocation inside a configuration block
     */
    static PropertySetInvocation configuration(PropertySetInvocation inner) {
        new ConfigurationPropertySetInvocation(inner)
    }

    /**
     * @param outer The name of the outer property
     * @param inner The inner invocation to use
     * @return This invocation, but invoked inside a configuration block
     */
    PropertySetInvocation inConfiguration(Action<ConfigurationPropertySetInvocation> onConfigure = null) {
        def config = new ConfigurationPropertySetInvocation(this)
        if (onConfigure != null) {
            onConfigure(config)
        }
        config
    }
}

class ConfigurationPropertySetInvocation extends PropertySetInvocation {

    PropertySetInvocation inner
    Boolean compact = false
    Boolean implicit = true

    ConfigurationPropertySetInvocation(PropertySetInvocation inner) {
        this.inner = inner
    }

    @Override
    String getDefinition() {
        "${inner} in configuration block"
    }

    @Override
    String compose(String path, String wrappedValue) {
        // For a configuration block, we need not use the get()
        if (implicit) {
            path = path.replaceAll(".get()", "")
        }
        String[] components = PropertyUtils.getPathComponents(path)
        String outerPath = components.take(components.length - 1).join(".")
        String innerPath = components.last()

        if (compact) {
            return "${outerPath} { ${inner.compose(innerPath, wrappedValue)} }"
        }

        """${outerPath} {
${inner.compose(innerPath, wrappedValue)}
}""".stripIndent().stripMargin().trim()
    }

    ConfigurationPropertySetInvocation asCompact() {
        compact = true
        this
    }
}

/**
 * Sets the value of a property by invoking the provider.set method "foo.set(value)"
 */
class ProviderPropertySetInvocation extends PropertySetInvocation {
    @Override
    String getDefinition() {
        "provider set"
    }

    @Override
    String compose(String path, String wrappedValue) {
        "${path}.set(${wrappedValue})"
    }
}

/**
 * Sets the value of the property by the assignment operator "foo = bar"
 */
class AssignmentPropertySetInvocation extends PropertySetInvocation {
    @Override
    String getDefinition() {
        "assignment"
    }

    @Override
    String compose(String path, String wrappedValue) {
        "${path} = ${wrappedValue}"
    }
}

/**
 * Sets the value of the property by the default property setter "setFoo(bar)"
 */
class SetterPropertySetInvocation extends PropertySetInvocation {
    @Override
    String getDefinition() {
        "default property setter"
    }

    @Override
    String compose(String path, String wrappedValue) {
        "${PropertyUtils.toSetter(path)}(${wrappedValue})"
    }
}

/**
 * Sets the value of the property by using its name as the method "foo(bar)"
 */
class MethodPropertySetInvocation extends PropertySetInvocation {
    @Override
    String getDefinition() {
        "method invocation"
    }

    @Override
    String compose(String path, String wrappedValue) {
        "${path}(${wrappedValue})"
    }
}

/**
 * Sets the value of the property by using a custom setter
 */
class CustomSetterPropertySetInvocation extends PropertySetInvocation {

    @Override
    String getDefinition() {
        "custom property setter '${setterName}'"
    }

    String setterName

    @Override
    String compose(String path, String wrappedValue) {
        def components = PropertyUtils.getPathComponents(path)
        if (components.length > 1) {
            path = components.take(components.length - 1).join(".") + ".${setterName}"
        } else {
            path = setterName
        }
        "${path}(${wrappedValue})"
    }

    CustomSetterPropertySetInvocation(String setter) {
        setterName = setter
    }
}

/**
 * No operation
 */
class NonePropertySetInvocation extends PropertySetInvocation {

    @Override
    String getDefinition() {
        "none"
    }

    @Override
    String compose(String path, String wrappedValue) {
        ""
    }
}
