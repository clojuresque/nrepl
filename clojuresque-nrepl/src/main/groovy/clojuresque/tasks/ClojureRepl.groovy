/*-
 * Copyright 2013 © Meikel Brandmeyer.
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

package clojuresque.tasks

import kotka.gradle.utils.ConfigureUtil
import kotka.gradle.utils.Delayed

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

/**
 * Starts a nrepl server for the project.
 *
 * To provide your own handler, eg. to use custom nrepl middlewares,
 * put the code into a separate sourceSet and tell the task to use
 * your specific handler factory.
 *
 * <pre><code>sourceSets { dev }
 *
 * clojureRepl {
 *      handler = "my.repl/handler"
 * }
 * </code></pre>
 *
 * And in <code>src/dev/clojure/my/repl.clj</code>:
 * <pre><code>(ns my.repl
 *   (:require
 *     [clojure.tools.nrepl.server :as repl]))
 *
 * (defn handler
 *   []
 *   (repl/default-handler #'my-middleware))
 * </code></pre>
 *
 * <em>Note:</em> You have to specify the nrepl version to use
 * manually. Eg. by using the “development” configuration or
 * as part of your application.
 *
 * <h2>Caveats</h2>
 * <ul>
 *   <li>The server keeps running in the current console.
 *     Currently there is no way to background the process.</li>
 *   <li>Parallel builds usually work. Unless the <code>build.gradle</code>
 *     is touched between the runs. Then the repl has to be stopped
 *     and restarted afresh to allow again parallel builds.</li>
 * </ul>
 */
class ClojureRepl extends DefaultTask {
    @InputFiles
    @Delayed
    def classpath

    def classpath(Object... fs) {
        classpath = this.getClasspath().plus(project.files(fs))
    }

    @Delayed
    def jvmOptions

    def port
    def handler

    @TaskAction
    void startRepl() {
        def options = [ "--port", port ]
        if (handler != null)
            options += [ "--handler", handler ]

        project.clojureexec {
            ConfigureUtil.configure delegate, this.jvmOptions
            classpath = this.classpath
            main = "clojuresque.tasks.repl/start-repl"
            args = options
        }
    }
}
