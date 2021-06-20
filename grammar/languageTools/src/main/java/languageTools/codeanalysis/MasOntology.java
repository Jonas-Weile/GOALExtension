package languageTools.codeanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import cognitiveKr.CognitiveKR;
import cognitiveKrFactory.CognitiveKRFactory;
import cognitiveKrFactory.InstantiationFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Update;
import languageTools.analyzer.mas.Analysis;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.Module;
import languageTools.program.mas.UseClause;

/**
 * A formatter that turns an {@link Analysis} into an ontology-style text
 * describing the MAS.
 *
 */
public class MasOntology {
	private static final String BR = "\n";
	private Analysis analysis;
	private CognitiveKR cognitivekr;

	public MasOntology(Analysis analysis) throws InstantiationFailedException {
		this.analysis = analysis;
		this.cognitivekr = CognitiveKRFactory.getCognitiveKR(analysis.getProgram().getKRInterface());
	}

	/**
	 *
	 * @param file the file to get short path of
	 * @return short path with only parent directory and the file's filename.
	 */
	private String shortPath(File file) {
		if (file == null) {
			return "";
		} else {
			File parent = file.getParentFile();
			return (parent == null) ? file.getName() : (parent.getName() + File.separator + file.getName());
		}
	}

	@Override
	public String toString() {
		String str = "Analysis of mas: " + shortPath(this.analysis.getProgram().getSourceFile()) + BR;
		str += "environment: " + shortPath(this.analysis.getProgram().getEnvironmentfile()) + BR;
		str += "agents: " + this.analysis.getProgram().getAgentNames() + BR;

		Set<String> modNames = new TreeSet<>();
		for (Module module : this.analysis.getModuleDefinitions()) {
			modNames.add("" + module.getName());
		}
		str += "modules: " + modNames + BR;

		str += "KR files:" + BR;
		Set<UseClause> set = new LinkedHashSet<>();
		set.addAll(this.analysis.getKrBeliefFiles());
		set.addAll(this.analysis.getKrGoalFiles());
		set.addAll(this.analysis.getKrKnowledgeFiles());

		for (UseClause use : set) {
			List<String> usedAs = new ArrayList<>();
			if (this.analysis.getKrBeliefFiles().contains(use)) {
				usedAs.add("belief");
			}
			if (this.analysis.getKrGoalFiles().contains(use)) {
				usedAs.add("goal");
			}
			if (this.analysis.getKrKnowledgeFiles().contains(use)) {
				usedAs.add("knowledge");
			}
			for (final File f : use.getResolvedReference()) {
				str += "  " + f.getName() + " used as " + usedAs + BR;
			}
		}

		str += "Predicate info:" + BR;
		// Unused predicates
		Set<String> unused = new LinkedHashSet<>();
		for (DatabaseFormula formula : this.analysis.getBeliefsUnused()) {
			// CHECK should we include only the top level formula?
			unused.addAll(this.cognitivekr.getUsedSignatures(formula));
		}
		str += "  Unused predicates:" + unused + BR;

		// percept predicates
		Set<String> perceptQs = new TreeSet<>();
		for (Query query : this.analysis.getPerceptQueries()) {
			perceptQs.addAll(this.cognitivekr.getUsedSignatures(query));
		}
		str += "  All percept predicates:" + setToString(perceptQs, 4) + BR;

		// belief queries.
		Set<String> beliefPreds = new TreeSet<>();
		for (DatabaseFormula belief : this.analysis.getAllBeliefs()) {
			beliefPreds.addAll(this.cognitivekr.getUsedSignatures(belief));
			;
		}
		for (DatabaseFormula belief : this.analysis.getAllKnowledge()) {
			beliefPreds.addAll(this.cognitivekr.getUsedSignatures(belief));
		}
		for (Query query : this.analysis.getPredicateQueries()) {
			beliefPreds.addAll(this.cognitivekr.getUsedSignatures(query));
		}
		str += "  All belief predicates:" + setToString(beliefPreds, 4) + BR;

		// goal predicates
		Set<String> goalPreds = new TreeSet<>();
		for (Query query : this.analysis.getGoalQueries()) {
			goalPreds.addAll(this.cognitivekr.getUsedSignatures(query));
		}
		for (DatabaseFormula dbf : this.analysis.getGoalDBFs()) {
			goalPreds.addAll(this.cognitivekr.getUsedSignatures(dbf));
		}
		str += "  All goal predicates:" + setToString(goalPreds, 4) + BR;

		// insert updates
		Set<String> insertPreds = new TreeSet<>();
		for (Update update : this.analysis.getInsertUpdates()) {
			insertPreds.addAll(this.cognitivekr.getUsedSignatures(update));
		}
		str += "  All insert predicates: " + setToString(insertPreds, 4) + BR;

		// delete updates
		Set<String> deletePreds = new TreeSet<>();
		for (Update update : this.analysis.getDeleteUpdates()) {
			deletePreds.addAll(this.cognitivekr.getUsedSignatures(update));
		}
		str += "  All delete predicates: " + setToString(deletePreds, 4) + BR;

		// adopt updates
		Set<String> adoptPreds = new TreeSet<>();
		for (Update update : this.analysis.getAdoptUpdates()) {
			adoptPreds.addAll(this.cognitivekr.getUsedSignatures(update));
		}
		str += "  All adopt predicates: " + setToString(adoptPreds, 4) + BR;

		// drop updates
		Set<String> dropPreds = new TreeSet<>();
		for (Update update : this.analysis.getDropUpdates()) {
			dropPreds.addAll(this.cognitivekr.getUsedSignatures(update));
		}
		str += "  All drop predicates: " + setToString(dropPreds, 4) + BR;

		str += "Action info:" + BR;
		// notice, an action can be both used and unused. Unfortunately we can't
		// find the used anymore.
		Set<String> usedActions = new TreeSet<>();
		for (UserSpecAction action : this.analysis.getActionDefinitions()) {
			usedActions.add(action.getSignature());
		}
		str += "  Used actions:" + setToString(usedActions, 4) + BR;

		Set<String> unusedActions = new TreeSet<>();
		for (UserSpecAction action : this.analysis.getUnusedActionDefinitions()) {
			unusedActions.add(action.getSignature());
		}
		str += "  Unused actions:" + setToString(unusedActions, 4) + BR;

		str += "Rule counts" + BR;
		str += "  event modules: " + ruleCount(this.analysis.getEventModules()) + BR;
		str += "  init modules: " + ruleCount(this.analysis.getInitModules()) + BR;

		Set<Module> remaining = new LinkedHashSet<>(this.analysis.getModuleDefinitions());
		remaining.removeAll(this.analysis.getEventModules());
		remaining.removeAll(this.analysis.getInitModules());
		str += "  other modules:" + BR;
		for (Module module : remaining) {
			str += "    " + module.getSignature() + ":" + module.getRules().size() + BR;
		}

		return str;
	}

	/**
	 * @param modules the modules to count
	 * @return total #rules in the set of modules.
	 */
	private int ruleCount(Set<Module> modules) {
		int ruleCount = 0;
		for (Module mod : modules) {
			ruleCount += mod.getRules().size();
		}
		return ruleCount;
	}

	/**
	 * @param list        elements to put in the string
	 * @param indentation the indentation for each element
	 * @return string with each element of the list on a new line, prepended with
	 *         #indentation whitespaces
	 */
	private String setToString(Set<String> list, int indentation) {
		String ind$ = "\n", res = "";

		for (int n = 0; n < indentation; n++) {
			ind$ += " ";
		}

		for (String element : list) {
			res += ind$ + element;
		}
		return res;
	}
}
