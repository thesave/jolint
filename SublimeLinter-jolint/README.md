SublimeLinter-contrib-JoLint
================================

This linter plugin for SublimeLinter provides an interface to jolint. It will be used with files that have the “__jolie__” syntax.

## Installation
SublimeLinter 3 must be installed in order to use this plugin.

### Linter installation
Before using this plugin, you must ensure that `jolint` is installed on your system. To install `jolint` follow the installation steps [here](https://github.com/thesave/jolint) to install the jolint executable

### Plugin installation

Before installing the jolint plugin, you need to [install](http://www.sublimelinter.com/en/latest/installation.html) SublimeLinter in Sublime Text. I strongly suggest to use [Package Control](https://packagecontrol.io/installation) for the task.

Then, you can install the SublimeLinter plugin by copying/cloning it in your SublimeText packages folder.
You can access the packages directory from the main menu *Preferences* -> *Browse Packages*.
Once in the Packages folder you can clone the repository with 

    svn co https://github.com/thesave/jolint/trunk/SublimeLinter-jolint
