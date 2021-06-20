package swiprolog.language.impl;

import krTools.language.Term;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologTerm;
import swiprolog.language.PrologUpdate;
import swiprolog.language.PrologVar;

public class PrologImplFactory {
	private final static String DYNAMIC = "dynamic";
	private final static String THREADLOCAL = "thread_local";

	private PrologImplFactory() {

	}

	public static PrologCompound getAtom(final String name, final SourceInfo info) {
		return new PrologAtomImpl(name, info);
	}

	public static PrologCompound getCompound(String name, final Term[] args, final SourceInfo info) {
		if (args.length == 1 && name.equals(DYNAMIC)) {
			name = THREADLOCAL;
		}
		return new PrologCompoundImpl(name, args, info);
	}

	public static PrologDBFormula getDBFormula(final PrologCompound compound) {
		return new PrologDBFormulaImpl(compound);
	}

	public static PrologTerm getNumber(final double value, final SourceInfo info) {
		return new PrologFloatImpl(value, info);
	}

	public static PrologTerm getNumber(final long value, final SourceInfo info) {
		return new PrologIntImpl(value, info);
	}

	public static PrologTerm getNumber(final int value, final SourceInfo info) {
		return new PrologIntImpl(value, info);
	}

	public static PrologQuery getQuery(final PrologCompound compound) {
		return new PrologQueryImpl(compound);
	}

	public static PrologUpdate getUpdate(final PrologCompound compound) {
		return new PrologUpdateImpl(compound);
	}

	public static PrologVar getVar(final String name, final SourceInfo info) {
		return new PrologVarImpl(name, info);
	}
}
