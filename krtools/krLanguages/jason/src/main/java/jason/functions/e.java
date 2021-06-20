package jason.functions;

import jason.asSemantics.DefaultArithFunction;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

/** 
  <p>Function: <b><code>math.e</code></b>: encapsulates java Math.E.
  
  <p>Example:<ul>
  <li> <code>math.e</code>: returns 2.718.</li>
  </ul>
   
  @author Jomi 
*/
public class e extends DefaultArithFunction  {

    @Override
	public String getName() {
        return "math.e";
    }
    
    @Override
    public double evaluate(
    		//TransitionSystem ts
    		BeliefBase bb, Term[] args) throws Exception {
        return Math.E;
    }

    @Override
    public boolean checkArity(int a) {
        return a == 0;
    }
    
}
