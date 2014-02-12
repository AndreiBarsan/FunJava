package de.hsrm.cs.jscala.helpers;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public interface Function3<P1, P2, P3, R> {
    public R apply(P1 p1, P2 p2, P3 p3);
}
