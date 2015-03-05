# jolint

A linter for the [Jolie](http://jolie-lang.org) language.

## Installation

Path `out/artifacts` contains the last build in a jar executable file
To ease execution, create a bash launcher, e.g., `jolie_launcher.sh`: 

  #!/bin/bash
  java -jar ~path/to/jolint.jar -l $JOLIE_HOME/lib:$JOLIE_HOME/javaServices/*:$JOLIE_HOME/extensions/* -i $JOLIE_HOME/include $1
  
under `bin` folder in your home folder. Remembed to make the launcher executable with `chmod +x jolint_launcher.sh`

## Usage

From shell `jolint yourJolieFile.(i)ol`

### SublimeLinter-jolint plugin

Folder `SublimeLinter-jolint` contains a plugin for SublimeLinter to integrate jolint with sublime text.
The folder also contains detailed instructions for installation.