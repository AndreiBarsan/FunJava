Project created by Andrei Barsan under the supervision of Prof. Dr. Sven Eric Panitz, at Hochschule RheinMain.
Git Repo created on February 12, 2014.

The purpose of this project is to implement as many Scala-like features in Java, taking advantage of some of the facilities
provided by Java 8. Among these are things like *case classes* and *pattern matching*.

Making the whole thing work is a tad tricky, as it is still a rather new project. The idea is to get your IDE to first build
the annotation processor, then use it to process the annotations in the tree example (or another example project), and only
then build and run that example.