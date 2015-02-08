SublimeLinter-contrib-JoLint
================================

This linter plugin for [SublimeLinter][docs] provides an interface to jolint. It will be used with files that have the “__jolie__” syntax.

## Installation
SublimeLinter 3 must be installed in order to use this plugin.

### Linter installation
Before using this plugin, you must ensure that `jolint` is installed on your system. To install `jolint`, do the following:

1. Compile jolint.scala into a jar file, e.g., `jolit.jar`

1. Create a launcher that points to `jolint.jar`. 
E.g., under *nix you can create in your `~/bin` folder a launcher script like the one below

<pre><code>#!/bin/bash 
java -cp path/to/jolient.jar -l $JOLIE_HOME/lib:$JOLIE_HOME/javaServices/*:$JOLIE_HOME/extensions/* -i $JOLIE_HOME/include "$@"</pre></code>

1. make the launcher executable

### Plugin installation

You can install the SublimeLinter plugin by copying/cloning it in your SublimeText packages folder.

[docs]: http://sublimelinter.readthedocs.org 