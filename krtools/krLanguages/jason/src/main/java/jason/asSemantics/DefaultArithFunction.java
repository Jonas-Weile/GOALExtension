package jason.asSemantics;

import jason.asSyntax.Term;
import jason.bb.BeliefBase;

import java.io.Serializable;

/**
 * 
 * Useful default implementation of all methods of ArithFunction interface.
 * 
 * @author Jomi
 *
 */
public abstract class DefaultArithFunction implements ArithFunction, Serializable {

    @Override
	public String getName() {
        return getClass().getName();
    }
    
    @Override
	public boolean checkArity(int a) {
        return true;
    }

    @Override
	public double evaluate(
    		//TransitionSystem ts
    		BeliefBase bb,
    		Term[] args) throws Exception {
        return 0;
    }
    
    @Override
	public boolean allowUngroundTerms() {
        return false;
    }

    @Override
    public String toString() {
        return "function "+getName();
    }

}
