package de.hsrm.cs.jscala.helpers;

/**
 * Created by Andrei Barsan on 20.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public class StringUtil {
    public static String cfirst(String val) {
        if(val.length() < 1) {
            return val;
        } else {
            return Character.toUpperCase(val.charAt(0)) + val.substring(1);
        }
    }
}
