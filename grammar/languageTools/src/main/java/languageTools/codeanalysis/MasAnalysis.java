package languageTools.codeanalysis;

import java.io.File;
import java.io.IOException;

import cognitiveKrFactory.InstantiationFailedException;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.mas.Analysis;
import languageTools.analyzer.mas.MASValidator;
import languageTools.program.mas.MASProgram;

public class MasAnalysis {
	private Analysis analysis;

	/**
	 * @param program the {@link MASProgram} to be analyzed
	 * @throws InstantiationFailedException if the provided MAS program is not valid
	 */
	public MasAnalysis(File masFile) throws InstantiationFailedException {
		FileRegistry registry = new FileRegistry();
		MASValidator mas2g;
		try {
			mas2g = new MASValidator(masFile.getCanonicalPath(), registry);
		} catch (IOException e) {
			throw new InstantiationFailedException("cannot find MAS file", e);
		}

		mas2g.validate();
		this.analysis = mas2g.process();
		if (registry.hasAnyError()) {
			throw new InstantiationFailedException(masFile + " is not valid: " + registry.getAllErrors());
		}
		if (registry.hasWarning()) {
			System.out.println(registry.getWarnings());
		}
	}

	@Override
	public String toString() {
		try {
			return (this.analysis == null) ? "no analysis available" : new MasOntology(this.analysis).toString();
		} catch (InstantiationFailedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
