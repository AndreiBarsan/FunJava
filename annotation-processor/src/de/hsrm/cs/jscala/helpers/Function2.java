package de.hsrm.cs.jscala.helpers;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public interface Function2<P1, P2, R> {
    public R apply(P1 p1, P2 p2);
}
