# jolint

A linter for the [Jolie](http://jolie-lang.org) language.
[image demo](!https://cloud.githubusercontent.com/assets/905938/6501587/784b0f00-c31a-11e4-8109-102363ffaca7.png)
## Installation

Path `out/artifacts` contains the last build in a jar executable file
To ease execution, create a bash launcher, e.g., `jolie_launcher.sh`: 

```bash
#!/bin/bash
JOLIE_HOME="/usr/lib/jolie" # or the path to your jolie installation folder
java -jar ~path/to/jolint.jar -l $JOLIE_HOME/lib:$JOLIE_HOME/javaServices/*:$JOLIE_HOME/extensions/* -i $JOLIE_HOME/include $1
```

under `bin` folder in your home folder. Remembed to make the launcher executable with `chmod +x jolint_launcher.sh`

## Usage

From shell `jolint yourJolieFile.(i)ol`

### SublimeLinter-jolint plugin

Folder `SublimeLinter-jolint` contains a plugin for SublimeLinter to integrate jolint with sublime text.
The folder also contains detailed instructions for installation.
