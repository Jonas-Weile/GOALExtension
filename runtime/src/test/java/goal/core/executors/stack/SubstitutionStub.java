package goal.core.executors.stack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;

/**
 * Stubbing class for Substitution.
 *
 * <h1>Explanation</h1> It would be better if we could use a mock instead of
 * this stub. However, mocking Substitution poses problems, because of the
 * functions that can modify Substitution itself. For example, suppose we have a
 * mock M for the empty substitution. Now someone calls M.add([X/3]). We can not
 * return a new Substitution mocking [X/3] because add is void.
 *
 * @author W.Pasman 24sep15
 */
public class SubstitutionStub implements Substitution {
	@Override
	public int hashCode() {
		return this.map.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof SubstitutionStub)) {
			return false;
		}
		SubstitutionStub other = (SubstitutionStub) obj;
		if (this.map == null) {
			if (other.map != null) {
				return false;
			}
		} else if (!this.map.equals(other.map)) {
			return false;
		}
		return true;
	}

	private final Map<Var, Term> map;

	/**
	 * Empty substi
	 */
	public SubstitutionStub() {
		this.map = new LinkedHashMap<>();
	}

	/**
	 * Substitution with one element
	 */
	public SubstitutionStub(Var v, Term t) {
		this();
		addBinding(v, t);
	}

	/**
	 * Constructor that takes a complete map of var/term values.
	 *
	 * @param m
	 */
	public SubstitutionStub(Map<Var, Term> m) {
		this.map = m;
	}

	@Override
	public List<Var> getVariables() {
		return new ArrayList<>(this.map.keySet());
	}

	@Override
	public Term get(Var var) {
		return this.map.get(var);
	}

	@Override
	public void addBinding(Var var, Term term) {
		if (!this.map.containsKey(var)) {
			this.map.put(var, term);
		}
	}

	@Override
	public Substitution combine(Substitution substitution) {
		for (Var var : substitution.getVariables()) {
			if (this.map.containsKey(var)) {
				return null;
			}
		}
		Substitution newsubst = substitution.clone();
		for (Var var : this.map.keySet()) {
			newsubst.addBinding(var, this.map.get(var));
		}
		return newsubst;
	}

	@Override
	public boolean remove(Var var) {
		return (this.map.remove(var) != null);
	}

	@Override
	public boolean retainAll(Collection<Var> variables) {
		boolean removedSomething = false;
		Set<Var> check = this.map.keySet();
		for (Var var : check) {
			if (!variables.contains(var)) {
				removedSomething |= remove(var);
			}
		}
		return removedSomething;
	}

	@Override
	public Substitution clone() {
		return new SubstitutionStub(new LinkedHashMap<>(this.map));
	}

	@Override
	public String toString() {
		return this.map.toString();
	}
}
