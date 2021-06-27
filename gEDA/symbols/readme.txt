See http://wiki.geda-project.org/geda:gschem_ug:config

The suite's user configuration file, called $HOME/.gEDA/gafrc

The component-library function is used to add symbol libraries to the "Select Component..." window.
A symbol library is a directory that contains symbol (“.sym”) files.
To add a directory as a symbol library, add the following to your configuration file:
(component-library "/path/to/mysymbols" "My Symbols")
e.g. (component-library "/home/carl/IdeaProjects/mijnsensors_github/gEDA/symbols" "Carl symbols")