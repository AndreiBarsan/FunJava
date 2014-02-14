package de.hsrm.cs.jscala.examples.trees.tests;

import de.hsrm.cs.jscala.PatternMatchException;
import de.hsrm.cs.jscala.annotations.Ctor;
import de.hsrm.cs.jscala.annotations.Data;
import de.hsrm.cs.jscala.api.Matching;
import de.hsrm.cs.jscala.helpers.Function1;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Optional;

import static junit.framework.Assert.*;

/**
 * Created by Andrei Barsan on 14.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
@RunWith(JUnit4.class)
public class NoTypeParamTest {

    @Data
    static class SimpleCase implements Matching<SimpleCase> {
        @Ctor void HoldsInt(Integer value) { };
        @Ctor void HoldsString(String data) { };

        public static <B> Function1<SimpleCase, Optional<B>> caseHoldsInt(Function1<Integer, B> theCase) {
            return (self) -> {
                if(! (self instanceof HoldsInt)) return Optional.empty();
                HoldsInt hi = (HoldsInt) self;
                return Optional.of(theCase.apply(hi.getValue()));
            };
        }

        public static <B> Function1<SimpleCase, Optional<B>> caseHoldsString(Function1<String, B> theCase) {
            return (self) -> {
                if(! (self instanceof HoldsString)) return Optional.empty();
                HoldsString hs = (HoldsString) self;
                return Optional.of(theCase.apply(hs.getData()));
            };
        }
    }

    @Test
    public void basicMatchTest() {
        SimpleCase sc = new HoldsInt(42);
        assertEquals((Integer)42, sc.match(
                SimpleCase.caseHoldsInt(val -> val)
        ));
    }
      
    @Test(expected = PatternMatchException.class)
    public void patternMatchFailTest() {
        SimpleCase sc = new HoldsInt(42);
        sc.match(
                SimpleCase.caseHoldsString(val -> val)
        );
    }
}
