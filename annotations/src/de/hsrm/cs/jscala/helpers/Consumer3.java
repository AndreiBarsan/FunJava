package de.hsrm.cs.jscala.helpers;

/**
 * Created by Andrei Barsan on 15.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public interface Consumer3<P1, P2, P3> {
    public void apply(P1 p1, P2 p2, P3 p3);
}
