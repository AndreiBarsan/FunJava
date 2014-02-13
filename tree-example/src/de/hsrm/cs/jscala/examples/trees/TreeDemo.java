package de.hsrm.cs.jscala.examples.trees;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public class TreeDemo {
    public static void main(String[] args) {
        Empty e = new Empty();
        Tree t = new Branch(
                    new Branch(
                            new Branch(e, 15, new Branch(e, 10, e)),
                            22,
                            new Branch(e, 27, new Branch(e, 5, e))
                    ),
                    42,
                    new Branch(e, 16, new Branch(e, 2, e))
                );

        System.out.println("Tree: " + t);
        System.out.println("Tree size: " + t.size());
    }
}
