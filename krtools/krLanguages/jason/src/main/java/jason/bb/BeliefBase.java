//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

package jason.bb;

//import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;

import java.util.Iterator;


/**
 * Common interface for all kinds of Jason Belief bases, even those
 * customised by the user.
 */
public interface BeliefBase extends Iterable<Literal>, Cloneable {

    public static final Term ASelf    = new Atom("self");
    public static final Term APercept = new Atom("percept");

    /** represents the structure 'source(percept)' */
    public static final Term TPercept = Pred.createSource(APercept);

    /** represents the structure 'source(self)' */
    public static final Term TSelf    = Pred.createSource(ASelf);
    
    /** 
     * Called before the MAS execution with the agent that uses this
     * BB and the args informed in .mas2j project.<br>
     * Example in .mas2j:<br>
     *     <code>agent BeliefBaseClass(1,bla);</code><br>
     * the init args will be ["1", "bla"].
     */
//    public void init(Agent ag, String[] args);
    
    /** Called just before the end of MAS execution */
    public void stop();
    
    /** removes all beliefs from BB */
    public void clear();
    
    /** Adds a belief in the end of the BB, returns true if succeed.
     *  The annots of l may be changed to reflect what was changed in the BB,
     *  for example, if l is p[a,b] in a BB with p[a], l will be changed to
     *  p[b] to produce the event +p[b], since only the annotation b is changed
     *  in the BB. */
    public boolean add(Literal l);

    /** Adds a belief in the BB at <i>index</i> position, returns true if succeed */
    public boolean add(int index, Literal l);


    /** Returns an iterator for all beliefs. */
    @Override
	public Iterator<Literal> iterator();

    /** @deprecated use iterator() instead of getAll */
    @Deprecated
	public Iterator<Literal> getAll();
    
    /** 
     * Returns an iterator for all literals in BB that match the functor/arity 
     * of the parameter.<br>
     */
    public Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi);

    /** 
     * Returns an iterator for all literals relevant for l's predicate
     * indicator, if l is a var, returns all beliefs.<br>
     * 
     * The unifier <i>u</i> may contain values for variables in <i>l</i>.
     *
     * Example, if BB={a(10),a(20),a(2,1),b(f)}, then
     * <code>getCandidateBeliefs(a(5), {})</code> = {{a(10),a(20)}.<br>
     * if BB={a(10),a(20)}, then <code>getCandidateBeliefs(X)</code> =
     * {{a(10),a(20)}. The <code>getCandidateBeliefs(a(X), {X -> 5})</code> 
     * should also return {{a(10),a(20)}.<br>
     */
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u);
    
    /** @deprecated use getCandidateBeliefs(l,null) instead */
    @Deprecated
	public Iterator<Literal> getRelevant(Literal l);

    /**
     * Returns the literal l as it is in BB, this method does not
     * consider annotations in the search. <br> Example, if
     * BB={a(10)[a,b]}, <code>contains(a(10)[d])</code> returns
     * a(10)[a,b].
     */
    public Literal contains(Literal l);
    
    /** Returns the number of beliefs in BB */
    public int size();

    /** Returns all beliefs that have "percept" as source */
    public Iterator<Literal> getPercepts();

    /** Removes a literal from BB, returns true if succeed */
    public boolean remove(Literal l);

    /** Removes all believes with some functor/arity */
    public boolean abolish(PredicateIndicator pi);

    /** Gets the BB as XML */
//    public Element getAsDOM(Document document);

    public BeliefBase clone();
}
