package de.hsrm.cs.jscala.api;

import de.hsrm.cs.jscala.PatternMatchException;
import de.hsrm.cs.jscala.helpers.Function1;
import de.hsrm.cs.jscala.helpers.Option;

/**
 * Created by Andrei Barsan on 13.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public interface Matching<A> {
    /**
     * Simply iterates through the given cases, applying the first pattern that matches. If none is found, a
     * PatternMatchException is thrown.
     */
    default <B> B match(Function1<A, Option<B>>... cases) {
        for (Function1<A, Option<B>> theCase : cases) {
            Option<B> result = theCase.apply((A) this);
            if (!result.isEmpty()) return result.get();
        }

        throw new PatternMatchException("unmatched pattern");
    }
}

