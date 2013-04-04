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
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction

public class StopTask extends DefaultTask {
    final static byte[] command =
        "d4:code35:(clojuresque.nrepl-server/shutdown)2:op4:evale".bytes

    def replInfo
    def replPort

    def getReplInfo() {
        def info = Utils.lazyValue(this.replInfo)
        info ? project.file(info) : null
    }

    def getReplPort() {
        Utils.lazyValue(this.replPort, 0)
    }

    void from(startTask) {
        replInfo = { -> startTask.replInfo }
        replPort = { -> startTask.replPort }
    }

    @TaskAction
    void stopNRepl() {
        int  port = getReplPort()
        File info = getReplInfo()

        if (info != null && info.exists())
            port = Integer.parseInt(info.readLines().get(0))

        if (port > 0) {
            Socket s = new Socket("127.0.0.1", port)
            s.withStreams { input, output ->
                output.write(command)
                output.flush()
            }

            info.delete()
        } else
            throw new InvalidUserDataException("Neither replInfo file nor port given. Cannot stop process!")
    }
}
