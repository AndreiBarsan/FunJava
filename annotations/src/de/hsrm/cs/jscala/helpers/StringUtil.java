package de.hsrm.cs.jscala.helpers;

import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * Created by Andrei Barsan on 20.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public class StringUtil {
    /**
     * Capitalizes the first letter of the given string.
     */
    public static String cfirst(String val) {
        if(val.length() < 1) {
            return val;
        } else {
            return Character.toUpperCase(val.charAt(0)) + val.substring(1);
        }
    }

    /**
     * Finds the nth last index of the given character, in the given string. If the
     * total number of occurrences is smaller than n, it returns -1.
     * n == 1 means lastIndexOf
     */
    public static int lastNthIndexOf(String s, char c, int n) {
        int occ = 0;
        for(int i = s.length() - 1; i >= 0; --i) {
            if(s.charAt(i) == c) {
                occ++;
            }

            if(n == occ) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @see #lastNthIndexOf
     */
    public static int nthIndexOf(String s, char c, int n) {
        int occ = 0;
        for(int i = 0; i < s.length(); ++i) {
            if(s.charAt(i) == c) {
                occ++;
            }

            if(n == occ) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Helper that returns the full type parameter specification of a given type parameter, as a string (includes
     * type boundaries).
     */
    public static String getFullTypeParamName(TypeParameterElement e) {
        StringBuilder sb = new StringBuilder();
        String mainType = e.asType().toString();
        sb.append(mainType);

        List<? extends TypeMirror> bounds = e.getBounds();
        if(bounds.size() == 0) {
            return sb.toString();
        }
        else {
            sb.append(" extends ");
            boolean first = true;
            for(TypeMirror tm : bounds) {
                if(first) {
                    first = false;
                } else {
                    sb.append(" & ");
                }

                sb.append(tm.toString());
            }

            return sb.toString();
        }
    }
}
