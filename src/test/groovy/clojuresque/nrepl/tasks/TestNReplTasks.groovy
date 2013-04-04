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

import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

public class TestNReplTasks extends Specification {
    def "tasks communicate via info file"() {
        setup: "the temporary replInfo file"
        def i = File.createTempFile("replInfo.", ".edn")

        and: "the temporary project used to run the tasks"
        def p = ProjectBuilder.builder().build()
        p.apply from:
            Thread.
                currentThread().
                    contextClassLoader.
                        getResource("clojuresque/nrepl/nrepl_test_params.gradle")
        when: "the start task is configured properly"
        def t = p.task("startNRepl", type: StartTask)
        t.replInfo = i
        t.replClasspath = p.files(p.configurations.nrepl)

        and: "the stop task is configured properly"
        def s = p.task("stopNRepl", type: StopTask)
        s.from t

        and: "the start task is executed"
        t.execute()
        Thread.sleep(5000) // XXX: Wait for server spin-up.

        then: "the repl port is written to the replInfo file"
        i.length() > 0

        when: "the port is read"
        def port = findReplInfo(i)

        and: "the connection is tried"
        testConnection(port)

        then: "it succeeds"
        notThrown(ConnectException)

        when: "the stop task is executed"
        s.execute()
        Thread.sleep(5000) // XXX: Wait for server spin-down.

        then: "the repl info file is removed"
        !i.exists()

        when: "the connection is tried again"
        testConnection(port)

        then: "it fails"
        thrown(ConnectException)

        cleanup:
        if (i.exists())
            i.delete()
    }

    def "starting the server respects initialisation"() {
        setup: "the temporary project used to run the tasks"
        def p = ProjectBuilder.builder().build()
        p.apply from:
            Thread.
                currentThread().
                    contextClassLoader.
                        getResource("clojuresque/nrepl/nrepl_test_params.gradle")

        when: "the start task is configured properly"
        def t = p.task("startNRepl", type: StartTask)
        t.replPort = 4711
        t.replClasspath = p.files(p.configurations.nrepl)
        t.init << "(throw (Exception. \"Kill starting process!\"))"

        and: "it is executed"
        t.execute()
        Thread.sleep(5000) // XXX: Wait for server spin-up.

        and: "a connection is tried"
        testConnection(4711)

        then: "it fails"
        thrown(ConnectException)
    }

    def "stop task requires either info file or explicit port"() {
        setup: "the temporary project used to run the tasks"
        def p = ProjectBuilder.builder().build()
        p.apply from:
            Thread.
                currentThread().
                    contextClassLoader.
                        getResource("clojuresque/nrepl/nrepl_test_params.gradle")

        when: "the stop task is not properly configured"
        def s = p.task("stopNRepl", type: StopTask)

        and: "it is executed"
        s.execute()

        then: "an InvalidUserDataException is thrown"
        def e = thrown(GradleException)
        rootCause(e) instanceof InvalidUserDataException
    }

    /* Helper functions: */
    static rootCause(exc) {
        while (exc.cause != null)
            exc = exc.cause
        return exc
    }

    static findReplInfo(file) {
        Integer.parseInt(file.readLines().first())
    }

    static testConnection(port) {
        def s = new Socket("127.0.0.1", port)
        s.close()
    }
}
