/*-
 * Copyright 2013 © Meikel Brandmeyer.
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package clojuresque.nrepl

import clojuresque.nrepl.tasks.StartTask
import clojuresque.nrepl.tasks.StopTask

import org.gradle.api.Plugin
import org.gradle.api.Project

public class NReplPlugin implements Plugin<Project> {
    final static String taskGroup = "Clojure NRepl server tasks"

    void apply(Project project) {
        project.apply plugin: "clojure"

        project.task("startNRepl", type: StartTask) {
            replInfo = "nreplPort.txt"
            replClasspath = project.files(
                project.sourceSets.main.clojure.srcDirs,
                project.sourceSets.main.resources.srcDirs,
                project.sourceSets.test.clojure.srcDirs,
                project.sourceSets.test.resources.srcDirs,
                project.configurations.testRuntime,
                project.configurations.development
            )
            group = taskGroup
            description = "Start a nrepl server in the background"
        }

        project.task("stopNRepl", type: StopTask) {
            from project.startNRepl
            group = taskGroup
            description = "Stop a running background nrepl server"
        }
    }
}
