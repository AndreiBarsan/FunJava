package de.hsrm.cs.jscala.examples.trees.tests;

// Important to import EVERYTHING from trees so that not just Tree, but the future-generated Branch and Empty get imported
import de.hsrm.cs.jscala.examples.trees.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by Andrei Barsan on 14.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
@RunWith(JUnit4.class)
public class TreeTests {

    @Test
    public void testTreeContains() {
        Tree<Integer> t = new Branch<>(new Empty<>(), 10, new Empty<>());
        assertTrue(t.contains(10));
        assertFalse(t.contains(42));

        Tree<Integer> te = new Empty<>();
        assertFalse(te.contains(10));

        Tree<Integer> tb = new Branch<>(new Empty<>(), 15, new Branch<>(t, 22, new Branch<>(new Empty<>(), 5, new Empty<>())));
        assertTrue(tb.contains(10));
        assertTrue(tb.contains(5));
        assertTrue(tb.contains(15));
        assertFalse(tb.contains(42));

        assertFalse(t.contains(15));
    }

    @Test
    public void testTreeInsert() {
        Tree<Integer> t = new Empty<>();
        assertFalse(t.contains(42));
        t = t.add(42);
        assertTrue(t.contains(42));

        t = t.add(52);
        assertTrue(t.contains(52));

        t = t.add(15);
        assertTrue(t.contains(15));
    }

    @Test
    public void testTreeFoldRandom() {
        Tree<Integer> t = new Empty<>();
        int expectedSum = 0;
        Random r = new Random();
        for(int i = 0; i < 15; i++) {
            int el = r.nextInt(10000);
            t = t.add(el);
            expectedSum += el;
        }

        int treeFoldSum = t.fold(0, 0, (a, b) -> a + b);
        assertEquals(expectedSum, treeFoldSum);
    }
}
