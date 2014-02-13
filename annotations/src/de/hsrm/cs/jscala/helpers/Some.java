package de.hsrm.cs.jscala.helpers;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public class Some<T> extends Option<T> {

    private T data;

    public Some(T data) {
        this.data = data;
    }

    @Override
    public T get() {
        return data;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
