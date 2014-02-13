package de.hsrm.cs.jscala.examples.trees;

import de.hsrm.cs.jscala.PatternMatchException;
import de.hsrm.cs.jscala.annotations.Ctor;
import de.hsrm.cs.jscala.annotations.Data;
import de.hsrm.cs.jscala.helpers.*;

@Data public class Tree<T> {

    @Ctor void Branch(Tree<T> left, T data, Tree<T> right) { }
    @Ctor void Empty() { }

    int size(){
        return this.match(
            caseEmpty (()       -> 0),
            caseBranch((l, el, r) -> l.size() + 1 + r.size())
        );
    }

    public <U> Tree<U> map(Function1<T, U> mapFunction) {
        return this.match(
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
        return operation.apply(startValue, this.match(
                caseEmpty(() -> neutralValue),
                caseBranch((l, el, r) -> operation.apply(operation.apply(l.fold(startValue, neutralValue, operation), el), r.fold(startValue, neutralValue, operation)))
        ));
    }

    <B> B match(Function1< Tree<T>, Option<B> >... cases){
        for (Function1< Tree<T>,Option<B> > theCase : cases){
            Option<B> result = theCase.apply(this);
            if (!result.isEmpty()) {
                return result.get();
            }
        }
        throw new PatternMatchException("unmatched pattern");
    }

    @Override
    public String toString() {
        return match(
            caseEmpty( ()           -> "( )" ),
            caseBranch((l, el, r)   -> "( " + l + ", [" + el + "], " + r + " )")
        );
    }

    /*
    This is how a "template" might look, in case we want to dynamically generate such a method:

    public static <A, B> Function1<$TYPE$, Option<B>> case$CURRENT_PATTERN$(Function$CURRENT_PATTERN_MEMBER_COUNT$<$CURRENT_PATTERN_MEMBER_TYPES$> theCase) {
    return (self) -> {
        if(! (self instanceof $CURRENT_PATTERN$) ) return new None();
        else {
            $CURRENT_PATTERN$$CURRENT_PATTERN_TYPE_PARAMS$ matchedBranch = ($CURRENT_PATTERN$$CURRENT_PATTERN_TYPE_PARAMS$) self;
            return new Some(theCase.apply( $CURRENT_PATTERN_MEMBER_GETTERS$ );
            }
        }
    }
     */

    public static <A, B> Function1<Tree<A>, Option<B>> caseBranch(Function3<Tree<A>, A, Tree<A>, B> theCase) {
        return (self) -> {
            if (!(self instanceof Branch)) return new None();
            Branch<A> branch = (Branch<A>) self;
            return new Some(theCase.apply(branch.getLeft(), branch.getData(), branch.getRight()));
        };
    }

    public static <A, B> Function1<Tree<A>, Option<B>> caseEmpty(Function0<B> theCase) {
        return (self) -> {
            if (!(self instanceof Empty)) return new None();
            Empty<A> empty = (Empty<A>) self;
            return new Some(theCase.apply());
        };
    }
}
