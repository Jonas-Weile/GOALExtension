package jason.functions;

import jason.asSemantics.DefaultArithFunction;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

/** 
  <p>Function: <b><code>system.time</code></b>: encapsulates java System.currentTimeMillis(),
  returns the current time in milliseconds.
  
  @see jason.stdlib.time internal action time

  @author Jomi 
*/
public class time extends DefaultArithFunction  {

    @Override
	public String getName() {
        return "system.time";
    }
    
    @Override
    public double evaluate(
    		//TransitionSystem ts
    		BeliefBase bb, Term[] args) throws Exception {
        return System.currentTimeMillis();
    }

    @Override
    public boolean checkArity(int a) {
        return a == 0;
    }
    
}
