package com.wooga.gradle.test

import com.wooga.gradle.test.mock.MockTask
import org.gradle.api.DefaultTask

import java.lang.reflect.ParameterizedType

import static com.wooga.gradle.test.PropertyUtils.wrapValueBasedOnType

/**
 * Adds support for specifying the subject task of an integration test. (To be used when in a derived {@code IntegrationSpec})
 * @param <T> A task used in a project
 */
trait TaskIntegrationSpec<T extends DefaultTask> {

    abstract File getBuildFile()

    abstract void addTask(String name, String typeName, Boolean force, String... lines)

    Class<T> getSubjectUnderTestClass() {
        if (!_sutClass) {
            try {
                this._sutClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                    .getActualTypeArguments()[0];
            }
            catch (Exception e) {
                this._sutClass = (Class<T>) MockTask
            }
        }
        _sutClass
    }
    private Class<T> _sutClass

    String getSubjectUnderTestName() {
        "${subjectUnderTestClass.simpleName.uncapitalize()}"
    }

    String getSubjectUnderTestTypeName() {
        subjectUnderTestClass.getTypeName()
    }

    void appendToSubjectTask(String... lines) {
        buildFile << """
        $subjectUnderTestName {
            ${lines.join('\n')}
        }
        """.stripIndent()
    }

    void addMockTask(Boolean force, String... lines) {
        addTask(subjectUnderTestName, subjectUnderTestTypeName, force, lines)
    }

    void setSubjectTaskProvider(String name, WrappedValue wrap) {
        appendToSubjectTask("${name}.set(${wrapValueBasedOnType(wrap.value, wrap.typeName)})")
    }
}
