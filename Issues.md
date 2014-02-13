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
    - mkToStringMethod (and the other, similar ones) should be more clever! First, check to see if the base class (e.g. Tree<T>
    already implements toString, and if it does, (maybe) don't output anything);
    - http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial2.html use this to generate the caseXXX methods
    dynamically, by editing class files (after javac compiles everything)