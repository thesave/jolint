# jolint

A linter for the [Jolie](http://jolie-lang.org) language.

<img width=50%" src="https://cloud.githubusercontent.com/assets/905938/6501587/784b0f00-c31a-11e4-8109-102363ffaca7.png" alt="demo image">

## Installation

Path `out/artifacts` contains the last build in a executable jar file.

To quicken the launch of `jolint` you can create a bash launcher, e.g., `jolie_launcher.sh`: 

```bash
#!/bin/bash
JOLIE_HOME="/usr/lib/jolie" # or the path to your jolie installation folder
java -jar ~path/to/jolint.jar -l $JOLIE_HOME/lib:$JOLIE_HOME/javaServices/*:$JOLIE_HOME/extensions/* -i $JOLIE_HOME/include $1
```

under `bin` in your home folder. Remembed to make the launcher executable with `chmod +x jolint_launcher.sh`

## Usage

From shell `jolint yourJolieFile.(i)ol`

### SublimeLinter-jolint plugin

Repository [SublimeLinter-jolint](https://github.com/thesave/SublimeLinter-jolint) contains a plugin and the detailed instructions to integrate jolint in Sublime Text.
