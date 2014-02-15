package de.hsrm.cs.jscala.examples.trees;

import de.hsrm.cs.jscala.PatternMatchException;
import de.hsrm.cs.jscala.annotations.Ctor;
import de.hsrm.cs.jscala.annotations.Data;
import de.hsrm.cs.jscala.api.Matching;
import de.hsrm.cs.jscala.helpers.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

// Auto-imports caseEmpty, caseBranch etc. after they get generated
import static de.hsrm.cs.jscala.examples.trees.TreeCases.*;

@Data public class Tree<T extends Comparable<T>> implements Matching<Tree<T>> {

    @Ctor void Branch(Tree<T> left, T data, Tree<T> right) { }
    @Ctor void Empty() { }

    public int size(){
        return match(
            caseEmpty (()         -> 0),
            caseBranch((l, el, r) -> l.size() + 1 + r.size())
        );
    }

    public Tree<T> add(T element) {
        return match(
            caseEmpty (()               -> new Branch(new Empty(), element, new Empty())),
            caseBranch((l, current, r)  -> {
                if(element.compareTo(current) < 0) {
                    return new Branch(l.add(element), current, r);
                }
                else {
                    return new Branch(l, current, r.add(element));
                }
            })

        );
    }

    public boolean contains(T element) {
        return match(
            caseEmpty (()           -> false),
            caseBranch((l, el, r)   -> element.equals(el) ? true : (l.contains(element) || r.contains(element)))
        );
    }

    public <U extends Comparable<U>> Tree<U> map(Function1<T, U> mapFunction) {
        return match(
            caseEmpty (()           -> new Empty<U>()),
            caseBranch((l, el, r)   -> new Branch<U>(l.map(mapFunction), mapFunction.apply(el), r.map(mapFunction)))
        );
    }

    /**
     * @param startValue Starting value for the fold operation.
     * @param neutralValue Neutral value for the operation (e.g. 0 for addition, 1 for multiplication)
     * @param operation The operation used for folding.
     * @return The final result starting with this node, going downwards.
     */
    public T fold(T startValue, T neutralValue, Function2<T, T, T> operation) {
        return operation.apply(startValue, match(
                caseEmpty (() -> neutralValue),
                caseBranch((l, el, r) -> operation.apply(operation.apply(l.fold(startValue, neutralValue, operation), el), r.fold(startValue, neutralValue, operation)))
        ));
    }

    /*
    See: Issues.md for a discussion regarding this.
    @Override
    public String toString() {
        return match(
            caseEmpty( ()           -> "( )" ),
            caseBranch((l, el, r)   -> "( " + l + ", [" + el + "], " + r + " )")
        );
    }
    */

    protected List<T> asList(){
        return asList(new LinkedList<>());
    }

    public List<T> asList(List<T> result){
        return match(
            caseBranch((l,el,r) -> {l.asList(result); result.add(el); r.asList(result); return result; }),
            otherwise ((x)      ->  result)
        );
    }

    /*
    public static <A extends Comparable<A>, B> Function1<Tree<A>, Optional<B>> caseBranch(Function3<Tree<A>, A, Tree<A>, B> theCase) {
        return (self) -> {
            if (!(self instanceof Branch)) return Optional.empty();
            Branch<A> branch = (Branch<A>) self;
            return Optional.of(theCase.apply(branch.getLeft(), branch.getData(), branch.getRight()));
        };
    }

    public static <A extends Comparable<A>, B> Function1<Tree<A>, Optional<B>> caseEmpty(Function0<B> theCase) {
        return (self) -> {
            if (!(self instanceof Empty)) return Optional.empty();
            Empty<A> empty = (Empty<A>) self;
            return Optional.of(theCase.apply());
        };
    }
    */

    public static <A extends Comparable<A>, B> Function1<Tree<A>, Optional<B>> otherwise(Function1<Tree<A>, B> theCase){
        return (self)-> Optional.of(theCase.apply(self));
    }
}
