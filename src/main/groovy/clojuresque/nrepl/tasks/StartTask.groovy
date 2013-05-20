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

package clojuresque.nrepl.tasks

import clojuresque.nrepl.Utils

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class StartTask extends DefaultTask {
    def replInfo
    def replClasspath
    def replPort
    def init = []

    def getReplInfo() {
        def info = Utils.lazyValue(this.replInfo)
        info ? project.file(info) : null
    }

    def getReplClasspath() {
        def path = Utils.lazyValue(this.replClasspath)
        path ? project.files(path) : null
    }

    def getReplPort() {
        Utils.lazyValue(this.replPort, 0)
    }

    @TaskAction
    void startNRepl() {
        ant.java(
            classname: "clojure.main",
            spawn:     true,
            fork:      true
        ) {
            classpath path: getReplClasspath().asPath
            arg value: "-e"
            arg value: this.commandString()
        }
    }

    String commandString() {
        def info = getReplInfo()
        def port = getReplPort()

        String.format('''
                (do
                  %s
                  (ns clojuresque.nrepl-server)
                  (require 'clojure.tools.nrepl.server)
                  (def server
                    (clojure.tools.nrepl.server/start-server :port %d))
                  (defn shutdown
                    []
                    (.close server)
                    (shutdown-agents))
                  %s)
                ''',
                init.join(" "),
                port,
                info ?
                    String.format('''
                        (with-open [writer (java.io.FileWriter. "%s")]
                          (binding [*out* writer]
                            (prn (:port server))
                            (flush)))
                        ''',
                        info.
                            path.
                            replaceAll("\\\\", "\\\\").
                            replaceAll("\"", "\\\"")) :
                    "")
    }
}
