/*-
 * Copyright 2013-2014 © Meikel Brandmeyer.
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package clojuresque

import clojuresque.tasks.ClojureRepl

import org.gradle.api.Plugin
import org.gradle.api.Project

class ClojureReplPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: "de.kotka.clojuresque.base"

        def compileTaskName =
            project.sourceSets.main.getCompileTaskName("clojure")
        def compileTask = project.tasks[compileTaskName]

        project.task("clojureRepl", type: ClojureRepl) {
            port = 7888
            delayedJvmOptions = { compileTask.jvmOptions }
            delayedClasspath  = {
                def sourceRoots = project.sourceSets.collect {
                    it.allSource.srcDirs
                }

                project.files(
                    sourceRoots,
                    project.sourceSets.collect { it.output },
                    project.configurations.testRuntime,
                    project.configurations.development
                )
            }
            description = "Run Clojure repl."
            group = ClojureBasePlugin.CLOJURE_GROUP
        }
    }
}
