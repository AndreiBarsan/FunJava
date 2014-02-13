package de.hsrm.cs.jscala.helpers;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public class None<T> extends Option {

    public None() { }

    @Override
    public T get() {
        throw new RuntimeException("get() on None.");
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
