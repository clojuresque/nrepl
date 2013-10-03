# clojuresque – a Clojure plugin for Gradle

## What is Gradle?

[Gradle][] is a build system written in Java and [Groovy][]. One advantage
of [Gradle][] is, that the build script itself is also a [Groovy][] script.
That means whatever can be done in [Groovy][] can be done in the build
script. This is useful to abstract away common patterns in the build like
repetition or conditional decisions.

On top of that [Gradle][] provides a convenient build system which comes
in form of different plugins. Each plugin defines certain conventions which
(if followed) automasie 95% of the build completely. Still the conventions
are freely configurable to be adapted to different project structures.

## What is clojuresque?

[clojuresque][cg] is now a plugin for [Gradle][], which adds [Clojure][clj]
support. It allows compilation with automatic namespace recognition. The
plugin is based on the Java plugin and hooks into the standard configurations
and archives.

## Usage

Create a `build.gradle` script in the root directory of your project. *Note
that gradle derives the project name from the name of this directory!*

    buildscript {
        repositories {
            maven { url 'http://clojars.org/repo' }
        }
        dependencies {
            classpath 'clojuresque:clojuresque-nrepl:1.0.0'
        }
    }
    
    apply plugin: 'clojure-nrepl'

## NRepl

The “nrepl” plugin adds a `clojureRepl` tasks, which starts up a
nrepl process. This can be used by IDEs or similar empowered development
environments and nrepl clients like reply.

## Caveats

 * Due to the way gradle handles the standard input there is no truely
   interactive repl possible. However in the light of reply and similar
   clients this is not really necessary.

 * The started process blocks the terminal for now.

## Issues

This is **alpha** software! Expect problems! Please report issues in the
bugtracker at [bitbucket][bb]. Or email them to me.

General support is available on the [clojuresque google group][cgg].

-- 
Meikel Brandmeyer <mb@kotka.de>
Frankfurt am Main, October 2013

[Gradle]: http://www.gradle.org
[Groovy]: http://groovy.codehaus.org
[clj]:    http://clojure.org
[cg]:     http://bitbucket.org/clojuresque/nrepl
[bb]:     http://bitbucket.org/clojuresque/nrepl/issues
[cgg]:    https://groups.google.com/forum/#!forum/clojuresque
