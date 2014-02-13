package de.hsrm.cs.jscala.helpers;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public abstract class Option<T> {
    public abstract T get();
    public abstract boolean isEmpty();
}
