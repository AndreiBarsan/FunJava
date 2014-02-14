package de.hsrm.cs.jscala.examples.trees.tests;

import de.hsrm.cs.jscala.PatternMatchException;
import de.hsrm.cs.jscala.annotations.Ctor;
import de.hsrm.cs.jscala.annotations.Data;
import de.hsrm.cs.jscala.api.Matching;
import de.hsrm.cs.jscala.helpers.Function0;
import de.hsrm.cs.jscala.helpers.Function1;
import de.hsrm.cs.jscala.helpers.None;
import de.hsrm.cs.jscala.helpers.Some;
import de.hsrm.cs.jscala.helpers.Option;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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

        public static <B> Function1<SimpleCase, Option<B>> caseHoldsInt(Function1<Integer, B> theCase) {
            return (self) -> {
                if(! (self instanceof HoldsInt)) return new None();
                HoldsInt hi = (HoldsInt) self;
                return new Some(theCase.apply(hi.getValue()));
            };
        }

        public static <B> Function1<SimpleCase, Option<B>> caseHoldsString(Function1<String, B> theCase) {
            return (self) -> {
                if(! (self instanceof HoldsString)) return new None();
                HoldsString hs = (HoldsString) self;
                return new Some(theCase.apply(hs.getData()));
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
