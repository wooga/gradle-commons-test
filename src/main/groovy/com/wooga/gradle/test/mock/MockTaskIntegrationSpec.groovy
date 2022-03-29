package com.wooga.gradle.test.mock

import com.wooga.gradle.test.WrappedValue

import java.lang.reflect.ParameterizedType

abstract class MockTaskIntegrationSpec<T extends MockTask> extends MockIntegrationSpec {

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

    def setup() {
        addMockTask(false)
    }

    void addMockTask(Boolean force, String... lines) {
        addTask(subjectUnderTestName, subjectUnderTestTypeName, force, lines)
    }

    void appendToSubjectTask(String... lines) {
        buildFile << """
        $subjectUnderTestName {
            ${lines.join('\n')}
        }
        """.stripIndent()
    }

    void setSubjectTaskProvider(String name, WrappedValue wrap){
        appendToSubjectTask("${name}.set(${wrapValueBasedOnType(wrap.value, wrap.typeName)})")
    }

    def runSubjectTaskSuccessfully() {
        runTasksSuccessfully(subjectUnderTestName)
    }

    void addTask(String name, String typeName, Boolean force, String... lines) {
        lines = lines ?: []
        buildFile << """
        task (${name}, type: ${typeName}) {                       
            ${force ? "onlyIf = {true}\n" : ""}${lines.join('\n')}
        }
        """.stripIndent()
    }
}
