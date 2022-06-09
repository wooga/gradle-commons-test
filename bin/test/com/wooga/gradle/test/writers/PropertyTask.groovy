package com.wooga.gradle.test.writers

import com.wooga.gradle.BaseSpec
import com.wooga.gradle.PropertyLookup
import com.wooga.gradle.test.mock.MockTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option

class PropertyTask extends MockTask implements BaseSpec {

    static final String extensionName = "Foobar"

    private final Property<String> pancakeFlavor

    @Input
    Property<String> getPancakeFlavor() {
        pancakeFlavor
    }

    private final Property<Boolean> bake

    @Input
    Property<Boolean> getBake() {
        bake
    }

    void setBake(Provider<Boolean> value) {
        bake.set(value)
    }

    private final RegularFileProperty logFile

    @Optional
    @InputFile
    RegularFileProperty getLogFile() {
        logFile
    }

    private final ListProperty<String> tags

    ListProperty<String> getTags() {
        tags
    }

    private final Property<Integer> numbers

    @Input
    Property<Integer> getNumbers() {
        numbers
    }

    void setNumbers(Provider<Integer> value) {
        numbers.set(value)
    }

    private final Property<MockObject> custom

    @Input
    Property<MockObject> getCustom() {
        custom
    }

    void setCustom(Provider<MockObject> value) {
        custom.set(value)
    }

    private final Property<MockEnum> customEnum

    @Input
    Property<MockEnum> getCustomEnum() {
        customEnum
    }

    void setCustomEnum(Provider<MockEnum> value) {
        customEnum.set(value)
    }

    private final ListProperty<File> exclude

    @Input
    @Optional
    ListProperty<File> getExclude() {
        exclude
    }

    void setExclude(Provider<Iterable<File>> values) {
        exclude.set(values)
    }

    void setExclude(Iterable<File> values) {
        exclude.set(values)
    }

    void exclude(File value) {
        exclude.add(value)
    }

    void exclude(File... values) {
        exclude.addAll(values)
    }

    void exclude(Iterable<File> values) {
        exclude.addAll(values)
    }

    @Option(option = "exclude", description = """
    Can be used with --all-projects and --yarn-workspaces to indicate sub-directories and files to
    exclude. Must be comma separated.
    Use the exclude option with --detection-depth to ignore directories at any depth.
    """)
    void excludeOption(String excludes) {
        exclude.set(excludes.trim().split(',').collect {
            new File(it)
        })
    }

    private final DirectoryProperty logsDir = objects.directoryProperty()

    DirectoryProperty getLogsDir() {
        logsDir
    }

    void setLogsDir(File value) {
        logsDir.set(value)
    }

    void setLogsDir(Provider<Directory> value) {
        logsDir.set(value)
    }

    PropertyTask() {
        bake = project.objects.property(Boolean)
        logFile = project.objects.fileProperty()
        tags = project.objects.listProperty(String)
        pancakeFlavor = project.objects.property(String)
        numbers = project.objects.property(Integer)
        custom = project.objects.property(MockObject)
        customEnum = project.objects.property(MockEnum)
        exclude = project.objects.listProperty(File)

        numbers.convention(PropertyTaskConventions.numbers.getIntegerValueProvider(project))
        bake.convention(PropertyTaskConventions.bake.getBooleanValueProvider(project))
        logsDir.convention(PropertyTaskConventions.logsDir.getDirectoryValueProvider(project))
        pancakeFlavor.convention(PropertyTaskConventions.pancakeFlavor.getStringValueProvider(project))
        // TODO: Implement getListValueProvider<T>, getListStringValueProvider (common use case),
        //  getValueProvider that takes a serializer
        tags.convention(PropertyTaskConventions.tags.getStringValueProvider(project).map({
            it.trim().split(',')
        }).map({ it.toList() }))
    }

    static class PropertyTaskConventions {
        static PropertyLookup numbers = new PropertyLookup("FOOBAR_NUMBERS", "foobar.numbers", 7)
        static PropertyLookup bake = new PropertyLookup("FOOBAR_BAKE", "foobar.bake", null)
        static PropertyLookup logsDir = new PropertyLookup("FOOBAR_LOGS_DIR", "foobar.logsDir", null)
        static PropertyLookup pancakeFlavor = new PropertyLookup("FOOBAR_PANCAKE_FLAVOR", "foobar.pancakeFlavor", null)
        static PropertyLookup tags = new PropertyLookup("FOOBAR_TAGS", "foobar.tags", null)
    }
}

enum MockEnum {
    foo,
    bar,
    foobar
}

class MockObject {
    String name

    MockObject(String name) {
        this.name = name
    }

    @Override
    String toString() {
        return name
    }
}
