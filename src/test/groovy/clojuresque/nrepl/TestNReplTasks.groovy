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

import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

public class TestNReplTasks extends Specification {
    def "starting and stopping the nrepl server works"() {
        setup:
        def i = File.createTempFile("replInfo.", ".edn")
        def p = ProjectBuilder.builder().build()
        p.apply from:
            Thread.
                currentThread().
                    contextClassLoader.
                        getResource("clojuresque/nrepl/nrepl_test_params.gradle")

        when:
        def t = p.task("startNRepl", type: StartTask)
        t.replInfo = i
        t.replClasspath = p.files(p.configurations.nrepl)
        def s = p.task("stopNRepl", type: StopTask)
        s.from t

        t.execute()
        // XXX: What for info file to be written.
        Thread.sleep(5000)

        then:
        i.length() > 0

        when:
        s.execute()

        then:
        !i.exists()

        cleanup:
        if (i.exists())
            i.delete()
    }

    def "starting the server respects initialisation"() {
        setup:
        def i = File.createTempFile("replInfo.", ".edn")
        def p = ProjectBuilder.builder().build()
        p.apply from:
            Thread.
                currentThread().
                    contextClassLoader.
                        getResource("clojuresque/nrepl/nrepl_test_params.gradle")

        when:
        def t = p.task("startNRepl", type: StartTask)
        t.replInfo = i
        t.replClasspath = p.files(p.configurations.nrepl)
        t.init << "(throw (Exception. \"Kill starting process!\"))"

        t.execute()
        // XXX: Wait for info file to be written.
        Thread.sleep(5000)

        then:
        i.length() == 0

        cleanup:
        i.delete()
    }
}
