Important:
==========
	- "main" home repo is the eclipse one;
	- library work will be done in the idea project, since idea is better at keeping
	the annotation processing and the annotation processing library jar in sync;
	- IMPORTANT: work done in idea's repo will be pulled into the eclipse repo and uploaded
	to github that way!

Current problems:
=================
    - no real inheritance for the case classes - specific methods inside them are not possible (e.g. custom toStrings)
    - no current use for the visitor/welcom pattern

Applications:
=============
    - it's important to find nice uses of pattern matching in Java so that the demo application doesn't suck
    - Expressions
        - Add(left: Expression, right: Expression)
        - Mul( " )
        - X()
        - Const(value: Int)
        - Neg(ex: Expression)
        - eval(Expression e, int x) { e.match(
            caseX( () -> x ),
            caseConst( (val) -> val ),
            caseAdd( (l, r) -> eval(l, x) + eval(r, x) ),
            caseMult( (l, r) -> eval(l, x) * eval(r, x) ),
            caseNeg( (e) -> -eval(e, x) )
            }
        - deriv - computes the derivative



Future ideas:
=============
    - maybe allow classes to be annotated with @Case; in that case, only generate fields, getters and setters for the data,
    and keep additional methods implemented within; when a reference to case class is caputred, custom @Case methods would be callable
    - mkToStringMethod (and the other, similar ones) should be more clever! First, check to see if the base class (e.g. Tree<T>
    already implements toString, and if it does, (maybe) don't output anything);
    [v] captureCaseXXX holds both the reference to the matched thing, as well as the individual fields
    [v] captureXXX (maybe) could ONLY have a reference to what the case class that matched (see a few tasks above)
    [v] done the above ones, without having to use different method names
    [v] add overloads to the captureXXX methods that allow an extra something -> boolean predicate to match by condition
    [v] hook into IDE and process anotations on-the-fly, while also registering them in the IDE => enable auto-complete!
    and get rid of the mountains of countless syntax errors - already exists in Eclipse, no need to reinvent the wheel in IDEA

Eclipse-support
===============
    - done integrating;
    - develop plugin to allow simplified access to these features
    - problem refreshing lib; 
    - will develop plugin and demo app in eclipse AFTER the main annotation processing
    jar is finished

cglib
=====
    - interceptors for method and interface calls, 100% at runtime; not really what we'd like, since we also want
    code autocomplete and code generation at compile time;

Apache Velocity
===============
    - template engine that could help us generate code in a nicer fashion (no more out.write("blah...");
    - it *can* be used to generate source code!
    - DONE!
    
Javassist
=========
    - http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial2.html
    - design a custom post-build step that runs a program based on javassist which generates the proper code; 
    HOWEVER, this would not work since doing so would require the initial build to succeed; which it won't do,
    since we're referring to stuff that would need to be generated first;
    