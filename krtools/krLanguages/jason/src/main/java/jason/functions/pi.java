package jason.functions;

import jason.asSemantics.DefaultArithFunction;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

/** 
  <p>Function: <b><code>math.pi</code></b>: encapsulates java Math.PI.
  
  <p>Example:<ul>
  <li> <code>math.pi</code>: returns 3.14.</li>
  </ul>
   
  @author Jomi 
*/
public class pi extends DefaultArithFunction  {

    @Override
	public String getName() {
        return "math.pi";
    }
    
    @Override
    public double evaluate(
    		//TransitionSystem ts
    		BeliefBase bb, Term[] args) throws Exception {
        return Math.PI;
    }

    @Override
    public boolean checkArity(int a) {
        return a == 0;
    }
    
}
