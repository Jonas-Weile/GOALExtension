package jasonkri.language;

import jason.asSyntax.VarTerm;

import java.util.Set;

import krTools.language.Var;
import krTools.parser.SourceInfo;

/**
 * We extend JasonExpression because JasonTerm will not accept VarTerm objects.
 */
public class JasonVar extends JasonTerm implements Var {

	public JasonVar(VarTerm var, SourceInfo info) {
		super(var, info);
	}

	@Override
	public Var getVariant(Set<Var> usedNames) {
		String name = getName();
		SourceInfo info = getSourceInfo();

		int n = 1;
		Var newVar;
		do {
			newVar = new JasonVar(new VarTerm(name + "_" + n), info);
			n++;
		} while (usedNames.contains(newVar));

		return newVar;
	}

	@Override
	public boolean isVar() {
		return true;
	}

}
