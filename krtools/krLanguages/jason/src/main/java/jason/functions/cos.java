package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

/** 
  <p>Function: <b><code>math.cos(N)</code></b>: encapsulates java Math.cos(N),
  returns the trigonometric cosine of an angle.
  
  @author Jomi 
*/
public class cos extends DefaultArithFunction  {

    @Override
	public String getName() {
        return "math.cos";
    }
    
    @Override
    public double evaluate(
    		//TransitionSystem ts
    		BeliefBase bb,
    		Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            return Math.cos(((NumberTerm)args[0]).solve());
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
