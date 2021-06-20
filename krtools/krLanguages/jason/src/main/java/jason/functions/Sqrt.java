package jason.functions;

import jason.JasonException;
import jason.asSemantics.DefaultArithFunction;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

/** 
  <p>Function: <b><code>math.sqrt(N)</code></b>: encapsulates java Math.sqrt(N);
  returns the correctly rounded positive square root of N.
  
  <p>Example:<ul>
  <li> <code>math.sqrt(9)</code>: returns 3.</li>
  </ul>
   
  @author Jomi 
*/
public class Sqrt extends DefaultArithFunction  {

    @Override
	public String getName() {
        return "math.sqrt";
    }
    

    @Override
    public double evaluate(
    		//TransitionSystem ts
    		BeliefBase bb, Term[] args) throws Exception {
        if (args[0].isNumeric()) {
            double n = ((NumberTerm)args[0]).solve();
            return Math.sqrt(n);
        } else {
            throw new JasonException("The argument '"+args[0]+"' is not numeric!");
        }
    }

    @Override
    public boolean checkArity(int a) {
        return a == 1;
    }
    
}
