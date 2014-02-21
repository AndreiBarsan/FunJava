package de.hsrm.cs.jscala.examples.trees.tests;

// Important to import EVERYTHING from trees so that not just Tree, but the future-generated Branch and Empty get imported
import de.hsrm.cs.jscala.examples.trees.*;

// Access the case methods
import static de.hsrm.cs.jscala.examples.trees.TreeCases.*;


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

    @Test
    public void testEmptyEquals() {
        Tree<Integer> t1 = new Empty<>();
        Tree<Integer> t2 = new Empty<>();

        assertTrue(t1.equals(t2));
        assertTrue(t2.equals(t1));
    }

    @Test
    public void testBranchEquals() {
        Tree<Integer> t1 = new Branch<>(new Empty<>(), 42, new Empty<>());
        Tree<Integer> t2 = new Branch<>(new Empty<>(), 43, new Empty<>());

        assertFalse(t1.equals(t2));
        assertFalse(t2.equals(t1));

        t2 = t2.match(
                caseEmpty(() -> new Empty<>()),
                caseBranch((l, d, r) -> new Branch<>(l, 42, r))
            );

        assertTrue(t1.equals(t2));
        assertTrue(t2.equals(t1));
    }

    @Test
    public void testBranchCondition() {
        Tree<String> someTree = new Branch<>(new Empty<>(), "hello", new Empty<>());

        Integer result = someTree.match(
            caseEmpty(() -> 0),
            caseBranch(t -> t.getData().contains("not here"), t -> 0 ),
            caseBranch(t -> t.getData().contains("ell"), t -> 1 ),
            caseBranch(t -> 0)
        );

        assertEquals(Integer.valueOf(1), result);
    }

    @Test
    public void testBranchConditionVoid() {
        Tree<String> someTree = new Branch<>(new Empty<>(), "hello", new Empty<>());
        someTree.match(
                caseEmptyV(() -> fail("Matched Empty instead of Branch")),
                caseBranchV(t -> t.getData().contains("not here"), t -> fail("Matched condition that shouldn't be matched") ),
                caseBranchV(t -> t.getData().contains("ell"), t -> {} ),
                caseBranchV(t -> fail("Failed to match correct condition"))
        );
    }
}
