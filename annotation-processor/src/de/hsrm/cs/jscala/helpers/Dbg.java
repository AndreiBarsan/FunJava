package de.hsrm.cs.jscala.helpers;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public class Dbg {
    /**
     * Since the annotation processing is run as part of the compilation process, exception stack traces don't get printed
     * out in their entirety. This is why this method should be used whenever an exception gets caught.
     */
    public static void printException(ProcessingEnvironment env, Exception e) {
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement ste : e.getStackTrace()) {
            sb.append("\t" + ste.toString() + "\n");
        }
        env.getMessager().printMessage(Diagnostic.Kind.ERROR, "An error has occurred: " + e.getMessage() + "\n" + sb.toString());
    }
}
