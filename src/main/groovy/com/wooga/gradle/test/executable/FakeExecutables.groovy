package com.wooga.gradle.test.executable

import static com.wooga.gradle.test.SpecUtils.isWindows

/**
 * This static class contains several methods for creating useful scripts to fake executable files.
 *
 //this creates a `ArgsReflectorExecutable` object
 def reflectorExec = FakeExecutables.argsReflector("filePath", exitStatus)

 //executable file that should be used to run this object somewhere
 reflectorExec.executable

 //returns the first execution result of this file in a given log
 ArgsReflectorExecutable.Result result = reflectorExec.firstResult(log)
 //you can also fetch all results inside a given log like this
 List< ArgsReflectorExecutable.Result> results = reflectorExec.allResults(log)

 //with an ArgsReflectorExecutable.Result object, you can get:
 result.args //List<String> of arguments passed to this executable
 result.envs //Map<Sring, String> representing the environment at the executable execution
 */
class FakeExecutables {

    /**
     * Creates an executable script that calls its first argument, passing through the other arguments.
     * @param fakeFilePath where the script should be created
     * @param overwrites if it should overwrite any existing file
     * @return executable file representing created script
     */
    static File runFirstParam(String fakeFilePath, boolean overwrites=true) {
        String osAwareFakePath = isWindows() && !fakeFilePath.endsWith(".bat")?
                "${fakeFilePath}.bat" :
                fakeFilePath
        File fakeExec = setupExecutableFile(new File(osAwareFakePath), overwrites)
        fakeExec.deleteOnExit()
        if (isWindows()) {
            //https://stackoverflow.com/questions/935609/batch-parameters-everything-after-1
            fakeExec << """
                @echo off
                echo [[${fakeExec.name}]]
                for /f "tokens=1,* delims= " %%a in ("%*") do set ALL_BUT_FIRST=%%b 
                call %1 %ALL_BUT_FIRST%
                echo [[end ${fakeExec.name}]]
                exit %errorlevel%
            """.stripIndent()
        } else {
            //running subscript with '.' in practice includes it on the mother script, including its parameters
            fakeExec <<
                    """#!/usr/bin/env bash
                echo [[${fakeExec.name}]]
                fst_param=\$1
                shift
                . "\${fst_param}"
                echo [[end ${fakeExec.name}]]
                exit \$?
            """.stripIndent()
        }
    }


    /**
     * Creates an executable script that logs all of its arguments and its environment at execution time.
     * @param  fakeFilePath where the script should be created
     * @param exitCode which exit code should the script return
     * @param overwrites if it should overwrite any existing file
     * @return FakeExecutable object representing created script
     */
    static ArgsReflectorExecutable argsReflector(String fakeFilePath, int exitCode, boolean overwrites=true) {
        String osAwareFakePath = isWindows() && !fakeFilePath.endsWith(".bat")?
                "${fakeFilePath}.bat" :
                fakeFilePath
        File fakeExec = setupExecutableFile(new File(osAwareFakePath), overwrites)
        fakeExec.deleteOnExit()

        String[] argsTokens = ["[[arguments]]", "[[end arguments]]"]
        String[] envTokens = ["[[environment]]", "[[end environment]]"]
        return new ArgsReflectorExecutable(fakeExec, argsTokens, envTokens, exitCode)
    }

    private static File setupExecutableFile(File fakeExec, boolean overwrites) {
        if (fakeExec.exists()) {
            if (overwrites) {
                fakeExec.delete()
            } else {
                throw new IllegalArgumentException("File ${fakeExec} already exists")
            }
        }
        fakeExec.createNewFile()
        fakeExec.executable = true
        return fakeExec
    }
}
