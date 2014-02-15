package de.hsrm.cs.jscala.helpers;

/**
 * Used in the code generation to mark functions that don't return anything.
 * Created by Andrei Barsan on 15.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public class Nothing {
    public static final Nothing VAL = new Nothing();
    private Nothing() { }
}
