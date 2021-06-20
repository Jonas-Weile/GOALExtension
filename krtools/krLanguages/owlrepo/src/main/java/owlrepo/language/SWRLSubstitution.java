package owlrepo.language;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.swrlapi.sqwrl.values.SQWRLEntityResultValue;
import org.swrlapi.sqwrl.values.SQWRLLiteralResultValue;
import org.swrlapi.sqwrl.values.SQWRLResultValue;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * A Substitution for SWRL holding a map of Vars to Terms In OWL we do not allow
 * substitution of a variable with a function term (atom: Human(Me)), so it can
 * only be variable to argument = variable/ IRI named individual / constant -
 * literal Since Arguments are not a separate KR language objects, it is
 * included in Term, but inside we differentiate between argument and atoms
 * 
 * @author timi
 *
 */
public class SWRLSubstitution implements Substitution {
	HashMap<SWRLVar, SWRLTerm> substitutions;

	Set<SWRLVar> variables;
	OWLDataFactory df;

	public SWRLSubstitution() {
		substitutions = new HashMap<SWRLVar, SWRLTerm>();
		variables = new HashSet<SWRLVar>();
		df = new OWLDataFactoryImpl();
	}

	public SWRLSubstitution(Map<Var, Term> map) {
		this();
		if (map != null) {
			Set<Var> vars = map.keySet();
			Iterator<Var> it = vars.iterator();
			while (it.hasNext()) {
				Var v = it.next();
				SWRLVar var = (SWRLVar) v;
				SWRLTerm atom = (SWRLTerm) map.get(v);
				substitutions.put(var, atom);
			}
			variables = substitutions.keySet();
		}
	}

	public SWRLSubstitution(SWRLVariable var, SWRLArgument value) {
		this();
		SWRLVar nvar = new SWRLVar(var);
		variables.add(nvar);
		substitutions.put(nvar, new SWRLTerm(value));

	}

	public SWRLSubstitution(SWRLVar var, SWRLTerm value) {
		this();
		variables.add(var);
		substitutions.put(var, value);

	}

	public SWRLSubstitution(SWRLVariable var, SQWRLResultValue resultvalue) {
		this();
		try {
			SWRLVar nvar = new SWRLVar(var);
			variables.add(nvar);
			if (resultvalue.isLiteral()) {
				SQWRLLiteralResultValue literalvalue = resultvalue
						.asLiteralResult();
				OWLLiteral lit = literalvalue.getOWLLiteral();
				// boolean value = literalvalue.getBoolean();
				substitutions.put(nvar,
						new SWRLTerm(df.getSWRLLiteralArgument(lit)));
			} else if (resultvalue.isEntity()) {
				SQWRLEntityResultValue entityvalue = resultvalue
						.asEntityResult();
				IRI value = entityvalue.getIRI();
				substitutions.put(
						nvar,
						new SWRLTerm(df.getSWRLIndividualArgument(df
								.getOWLNamedIndividual(value))));
			}
			// {
			// SQWRLClassResultValue classvalue = value.asClassResult();
			// classvalue.getIRI();
			// } else if (value.isDataProperty()){
			// SQWRLDataPropertyResultValue datavalue =
			// value.asDataPropertyResult();
			// datavalue.getIRI();
			// } else if (value.isObjectProperty()){
			// SQWRLObjectPropertyResultValue objectvalue =
			// value.asObjectPropertyResult();
			// objectvalue.getIRI();
			// } else else if (value.isIndividual()){
			// SQWRLIndividualResultValue indivalue =
			// value.asIndividualResult();
			// indivalue.getIRI()
			// } else if (value.isAnnotationProperty()) {
			// SQWRLAnnotationPropertyResultValue annotvalue =
			// value.asAnnotationPropertyResult();
			// annotvalue.getIRI()
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void addSWRLSubstitution(SWRLVariable var, SWRLArgument atom) {
		substitutions.put(new SWRLVar(var), new SWRLTerm(atom));
		variables = substitutions.keySet();
	}

	public void addSWRLSubstitution(SWRLVar var, SWRLTerm atom) {
		substitutions.put(var, atom);
		variables = substitutions.keySet();
	}

	public void addSWRLSubstitution(SWRLSubstitution subst) {
		for (SWRLVar var : subst.getSWRLVariables()) {
			SWRLTerm arg = subst.getSWRLTerm(var);
			substitutions.put(var, arg);
		}
		variables = substitutions.keySet();
	}

	public SWRLTerm getSWRLTerm(SWRLVar var) {
		return substitutions.get(var);
	}

	@Override
	public Set<Var> getVariables() {
		HashSet<Var> newvars = new HashSet<Var>();
		Iterator<SWRLVar> it = variables.iterator();
		while (it.hasNext()) {
			newvars.add(it.next());
		}
		return newvars;
	}

	public Set<SWRLVar> getSWRLVariables() {
		return variables;
	}

	@Override
	public Term get(Var var) {
		// check if needs an if and return null or does automatically
		SWRLVar v = ((SWRLVar) var);
		SWRLTerm term = substitutions.get(v);
		if (term != null)
			return term;
		return null;
	}

	public SWRLTerm getSWRLTerm(Var var) {
		// check if needs an if and return null or does automatically
		return (SWRLTerm) get(var);
	}

	@Override
	public void addBinding(Var var, Term term) {
		SWRLVar nvar = (SWRLVar) var;
		SWRLTerm nterm = (SWRLTerm) term;
		if (!substitutions.containsKey(nvar) && (nterm instanceof SWRLArgument))
			substitutions.put(nvar, nterm);
	}

	@Override
	public Substitution combine(Substitution substitution) {
		//add them?
		// System.out.println("Combine: " + this.toString() + " and " +
		// substitution.toString());
		SWRLSubstitution comb = new SWRLSubstitution();
		comb.addSWRLSubstitution(this);
		comb.addSWRLSubstitution((SWRLSubstitution) substitution);
		// System.out.println("COMBINED SUBSTITUTIONS: " + comb.toString());
		return comb;
	}

	@Override
	public boolean remove(Var var) {
		SWRLVariable nvar = ((SWRLVar) var).getVar();
		if (substitutions.containsKey(nvar)) {
			substitutions.remove(nvar);
			return true;
		}
		return false;
	}

	@Override
	// Removes all bindings in this Substitution that do not bind any of the
	// given Vars.
	public boolean retainAll(Collection<Var> variables) {
		Set<Var> removevars = getVariables();
		removevars.removeAll(variables);
		if (removevars.isEmpty())
			return false;
		else
			variables.removeAll(removevars);

		Iterator<Var> iterator = removevars.iterator();
		while (iterator.hasNext()) {
			SWRLVar v = (SWRLVar) iterator.next();
			substitutions.remove(v.getVar());
		}
		return true;
	}

	@Override
	public Substitution clone() {
		return this;
	}

	@Override
	public String toString(){
		String s = "";
		for (SWRLVar var : this.variables)
			s += var + " / " + this.substitutions.get(var).toString() + " + ";
		if (s == "")
			s = "empty substitution";
		return s;
	}
}
