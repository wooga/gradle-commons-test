package com.wooga.gradle.test.writers

import com.wooga.gradle.test.PropertyUtils

/**
 * Defines how a property has its value set in a script
 */
abstract class PropertySetInvocation {

    abstract String getDefinition()

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
        "custom property setter"
    }

    String setterName

    @Override
    String compose(String path, String wrappedValue) {
        def components = path.split(/\./)
        if (components.length > 1 ){
            path = components.take(components.length - 1).join(".") + ".${setterName}"
        } else{
            path = setterName
        }
        "${path}(${wrappedValue})"
    }

    CustomSetterPropertySetInvocation(String setter) {
        setterName = setter
    }
}
