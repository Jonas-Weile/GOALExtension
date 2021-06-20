package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

/** 
  <p>Function: <b><code>math.atan(N)</code></b>: encapsulates java Math.atan(N),
  returns the arc tangent of a value; the returned angle is in the range -pi/2 through pi/2.
  
  @author Jomi 
*/
public class atan extends DefaultArithFunction  {

    @Override
	public String getName() {
        return "math.atan";
    }
    
    @Override
    public double evaluate(
    		//TransitionSystem ts
    		BeliefBase bb,
    		Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            return Math.atan(((NumberTerm)args[0]).solve());
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
