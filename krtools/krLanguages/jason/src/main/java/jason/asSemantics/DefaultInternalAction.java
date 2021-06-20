package jason.asSemantics;

import jason.JasonException;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

import java.io.Serializable;

/**
 * Default implementation of the internal action interface (it simply returns false 
 * for the interface methods).
 * 
 * Useful to create new internal actions.
 * 
 * @author Jomi
 */
public class DefaultInternalAction implements InternalAction, Serializable {
    
    private static final long serialVersionUID = 1L;

    @Override
	public boolean suspendIntention()   { return false;  }
    @Override
	public boolean canBeUsedInContext() { return true;  }

    public int getMinArgs() { return 0; }
    public int getMaxArgs() { return Integer.MAX_VALUE; }
    
    protected void checkArguments(Term[] args) throws JasonException {
        if (args.length < getMinArgs() || args.length > getMaxArgs())
            throw JasonException.createWrongArgumentNb(this);            
    }
    
    @Override
	public Term[] prepareArguments(Literal body, Unifier un) {
        Term[] terms = new Term[body.getArity()];
        for (int i=0; i<terms.length; i++) {
            terms[i] = body.getTerm(i).capply(un);
        }
        return terms;
    }
    
    @Override
	public Object execute(BeliefBase bb, Unifier un, Term[] args) throws Exception {
        return false;
    }
    
    @Override
	public void destroy() throws Exception {
        
    }
}
