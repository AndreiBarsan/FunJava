package de.hsrm.cs.jscala.examples.trees;

import java.util.Comparator;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public class TreeDemo {
    public static void main(String[] args) {
        Empty e = new Empty();
        Tree<Integer> t = new Branch<Integer>(
                    new Branch(
                            new Branch(e, 16, new Branch(e, 17, e)),
                            19,
                            new Branch(e, 22, new Branch(e, 24, e))
                    ),
                    42,
                    new Branch(e, 45, new Branch(e, 99, e))
                );

        System.out.println("Tree:\n\t" + t);
        System.out.println("Tree size: " + t.size());
        System.out.println("Sum of elements in tree: " + t.fold(0, 0, (a, b) -> a + b));

        t = t.map((el) -> el + 10);
        System.out.println("Added 10 to every element in the tree. Resulting tree:\n\t" + t);
        System.out.println("New sum: " + t.fold(0, 0, (a, b) -> a + b));

        // Comparator<Integer> intComp = (i1, i2) -> (i1 > i2) ? 1 : (i1 < i2) ? -1 : 0;
        t = t.add(128);
        t = t.add(200);
        t = t.add(3);
        t = t.add(31);
        System.out.println("Added more elements to tree: \n\t" + t);
    }
}
