package de.hsrm.cs.jscala.helpers;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
@FunctionalInterface
public interface Function1<P1, R> {
    public R apply(P1 p1);
}
