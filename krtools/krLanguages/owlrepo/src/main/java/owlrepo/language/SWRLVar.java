package owlrepo.language;

import java.util.Set;

import krTools.language.Var;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class SWRLVar extends SWRLTerm implements Var {

	SWRLVariable var;
	OWLDataFactory df = new OWLDataFactoryImpl();

	public SWRLVar(SWRLVariable var) {
		super(var);
		this.var = var;
	}

	public SWRLVar(String string) {
		super(string);
	}

	public SWRLVariable getVar() {
		return this.var;
	}

	@Override
	public boolean isVar() {
		return true;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public Set<Var> getFreeVar() {
		return super.getFreeVar();
	}

	@Override
	public String toString() {
		return var.getIRI().toString();
	}

	@Override
	public Var getVariant(Set<Var> usedNames) {
		if (usedNames.contains(this)) {
			String oldvar = var.getIRI().toString();
			String[] nmsp = oldvar.split("#");
			if (nmsp.length > 1) {
				IRI newvar = IRI.create(nmsp[0] + "#variant_" + nmsp[1]);
				return new SWRLVar(df.getSWRLVariable(newvar));
			} else {
				System.out.println("WHAT TO DO WITH THIS?" + oldvar);
			}
		}
		return this;
	}

}
