package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

/** 
  <p>Function: <b><code>math.log(N)</code></b>: encapsulates java Math.log(N),
  returns the natural logarithm (base e) of N.
  
  @author Jomi 
*/
public class log extends DefaultArithFunction  {

    @Override
	public String getName() {
        return "math.log";
    }
    
    @Override
    public double evaluate(
    		//TransitionSystem ts
    		BeliefBase bb, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            double n = ((NumberTerm)args[0]).solve();
            return Math.log(n);
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
