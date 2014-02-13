Current problems:
=================
    - no real inheritance for the case classes - specific methods inside them are not possible (e.g. custom toStrings)
    - no additional matching options (no match-by-expression like in Scala)


Future ideas:
=============
    - hook into IDE and process anotations on-the-fly, while also registering them in the IDE => enable auto-complete!
    and get rid of the mountains of countless syntax errors
    - maybe allow classes to be annotated with @Case; in that case, only generate fields, getters and setters for the data,
    and keep additional methods implemented within;