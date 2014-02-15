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

// Required for the branches!
import static de.hsrm.cs.jscala.examples.trees.tests.SimpleCaseCases.*;

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
    }

    @Test
    public void basicMatchTest() {
        SimpleCase sc = new HoldsInt(42);
        assertEquals((Integer)42, sc.match(
            caseHoldsInt(val -> val)
        ));
    }

    @Test
    public void basicFallthroughTest() {
        String quote = "Now I am become Death, the destroyer of worlds.";
        SimpleCase sc = new HoldsString(quote);
        assertEquals(quote, sc.match(
            caseHoldsInt(val -> val),
            caseHoldsString(val -> val)
        ));
    }

    @Test
    public void trivialComputationInCaseTest() {
        SimpleCase sc = new HoldsInt(100);
        assertEquals(50, sc.match(
           caseHoldsInt(val -> {
               int vaux = val;
               for(int i = 0; i < 5; i++) {
                   vaux -= 10;
               }
               return vaux;
           }),
           caseHoldsString(val -> "Nope.")
        ));
    }
      
    @Test(expected = PatternMatchException.class)
    public void patternMatchFailTest() {
        SimpleCase sc = new HoldsInt(42);
        sc.match(
                caseHoldsString(val -> val)
        );
    }

    @Test
    public void otherwiseTest() {
        SimpleCase sc = new HoldsInt(0);
        assertEquals((Integer) 2, sc.match(
                caseHoldsString(val -> 1),
                otherwise(val -> 2)
        ));
    }

    @Test
    public void otherwiseVoidTest() {
        SimpleCase sc = new HoldsInt(42);
        sc.match(
            caseHoldsStringV(val -> fail("Int holder matched String case.")),
            otherwiseV(val -> { })
        );
    }
}
