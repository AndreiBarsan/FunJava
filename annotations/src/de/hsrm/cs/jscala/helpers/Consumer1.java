package de.hsrm.cs.jscala.helpers;

/**
 * Created by Andrei Barsan on 15.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
@FunctionalInterface
public interface Consumer1<P1> {
    public void apply(P1 p1);
}
