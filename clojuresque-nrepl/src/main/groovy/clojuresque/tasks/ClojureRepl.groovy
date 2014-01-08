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

import clojuresque.Util

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
 * <pre><code> sourceSets { dev }
 *
 * clojureRepl {
 *      handler = "my.repl/handler"
 * }
 * </code></pre>
 *
 * And in <code>src/dev/clojure/my/repl.clj</code>:
 * <pre><code> (ns my.repl
 *   (:require
 *     [clojure.tools.nrepl.server :as repl]))
 *
 * (defn handler
 *   []
 *   (repl/default-handler #'my-middleware))
 * </code></pre>
 *
 * If all you want is to specify custom middleware, there is a
 * short-hand in the <code>middleware</code> option. Here you can
 * specify the fully-qualified names of the middleware in the desired
 * order.
 *
 * <pre><code> clojureRepl {
 *     middleware &lt;&lt; "my.repl/middleware"
 * }
 * </code></pre>
 *
 * <em>Note:</em> You have to specify the nrepl version to use
 * manually. Eg. by using the <code>development</code> configuration or
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
    /**
     * Classpath required to generate the documentation.
     */
    @InputFiles
    @Delayed
    def classpath

    /**
     * Adds the given files to classpath. <code>fs</code> is subject
     * to expansion via <code>project.files</code>.
     *
     * @param  fs   Files/directories to add to the classpath
     * @return      Returns <code>this</code>.
     */
    def classpath(Object... fs) {
        classpath = this.getClasspath().plus(project.files(fs))
        this
    }

    /**
     * A <code>Closure</code> configuring the underlying exec spec.
     * This may be used to set eg. heap sizes etc.
     */
    @Delayed
    def jvmOptions

    /**
     * The port for the repl server to listen on. A string or integer.
     */
    def port

    /**
     * The fully qualified name of the repl handler.
     */
    def handler

    /**
     * A list of fully qualified names of middlewares. <em>Note:</em>
     * unused in case a custom <code>handler</code> is set.
     */
    def middleware = []

    @TaskAction
    void startRepl() {
        def options = [
            port:    port,
            handler: handler,
            middleware: middleware
        ]

        project.clojureexec {
            ConfigureUtil.configure delegate, this.getJvmOptions()
            classpath = this.getClasspath()
            main = "clojuresque.tasks.repl/start-repl"
            standardInput = Util.optionsToStream(options)
        }
    }
}
