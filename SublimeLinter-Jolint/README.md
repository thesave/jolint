SublimeLinter-contrib-JoLint
================================

[![Build Status](https://travis-ci.org/SublimeLinter/SublimeLinter-contrib-JoLint.svg?branch=master)](https://travis-ci.org/SublimeLinter/SublimeLinter-contrib-JoLint)

This linter plugin for [SublimeLinter][docs] provides an interface to [JoLint](__linter_homepage__). It will be used with files that have the “__syntax__” syntax.

## Installation
SublimeLinter 3 must be installed in order to use this plugin.

### Linter installation
Before using this plugin, you must ensure that `jolint` is installed on your system. To install `jolint`, do the following:

1. Compile jolint.scala into a jar file, e.g., `jolit.jar`

1. Create a launcher that points to `jolint.jar`. 
E.g., under *nix you can create in your `~/bin` folder a launcher script like the one below
	
	#!/bin/bash 
	java -jar path/to/joint.jar $1

1. make the launcher executable

### Plugin installation

You can install the SublimeLinter plugin by copying/cloning it in your SublimeText packages folder.

[docs]: http://sublimelinter.readthedocs.org