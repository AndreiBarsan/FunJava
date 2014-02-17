package de.hsrm.cs.jscala;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
@SuppressWarnings("serial") // We never need to serialize this
public class PatternMatchException extends RuntimeException {
    public PatternMatchException(String message) {
        super(message);
    }
}
