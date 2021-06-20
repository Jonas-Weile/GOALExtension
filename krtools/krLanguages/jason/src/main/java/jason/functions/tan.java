package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

/** 
  <p>Function: <b><code>math.tan(N)</code></b>: encapsulates java Math.tan(N),
  returns the trigonometric tangent of an angle.
  
  @author Jomi 
*/
public class tan extends DefaultArithFunction  {

    @Override
	public String getName() {
        return "math.tan";
    }
    
    @Override
    public double evaluate(
    		//TransitionSystem ts
    		BeliefBase bb, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            return Math.tan(((NumberTerm)args[0]).solve());
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
