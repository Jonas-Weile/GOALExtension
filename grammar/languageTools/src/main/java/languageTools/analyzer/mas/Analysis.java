package languageTools.analyzer.mas;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Update;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.Validator;
import languageTools.analyzer.actionspec.ActionSpecValidator;
import languageTools.analyzer.actionspec.ActionSpecValidatorSecondPass;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.analyzer.module.ModuleValidatorSecondPass;
import languageTools.analyzer.planner.PlannerValidator;
import languageTools.analyzer.planner.PlannerValidatorSecondPass;
import languageTools.program.Program;
import languageTools.program.actionspec.ActionSpecProgram;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.Module;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.MASProgram;
import languageTools.program.mas.UseClause;
import languageTools.program.planner.PlanningModule;

/**
 * Collects analysis data from all components of the MAS. Needed for validation
 * and reporting.
 *
 */
public class Analysis {
	private final Set<String> actionCalls = new LinkedHashSet<>();
	private final Set<Module> moduleDefinitions = new LinkedHashSet<>();
	private final Set<Module> unusedModuleDefinitions = new LinkedHashSet<>();
	private final Set<ActionSpecProgram> actionSpecDefinitions = new LinkedHashSet<>();
	private final Set<UserSpecAction> actionDefinitions = new LinkedHashSet<>();	
	private final Set<DatabaseFormula> predicateDefinitions = new LinkedHashSet<>();
	private final Set<Query> predicateQueries = new LinkedHashSet<>();
	private final Set<DatabaseFormula> allKnowledge = new LinkedHashSet<>();
	private final Set<DatabaseFormula> allBeliefs = new LinkedHashSet<>();
	private final Set<Update> beliefUpdates = new LinkedHashSet<>();
	private final Set<DatabaseFormula> beliefsUnused = new LinkedHashSet<>();
	private final Map.Entry<File, String> override;
	/**
	 * The list of all validators that are created to recursively validating this
	 * MAS. This is used for global checks on the MAS level.
	 */
	private final List<Validator<?, ?, ?, ?>> subvalidators = new LinkedList<>();
	private final FileRegistry registry;
	private final MASProgram program;
	private Set<UseClause> krGoalFiles = new LinkedHashSet<>();
	private Set<UseClause> krBeliefFiles = new LinkedHashSet<>();
	private Set<UseClause> krKnowledgeFiles = new LinkedHashSet<>();
	private Set<UserSpecAction> unusedActionDefinitions = new LinkedHashSet<>();
	private Set<Query> allPerceptQueries = new LinkedHashSet<>();
	private Set<Query> goalQueries = new LinkedHashSet<>();
	private Set<DatabaseFormula> goalDbfs = new LinkedHashSet<>();
	private Set<Update> insertUpdates = new LinkedHashSet<>();
	private Set<Update> deleteUpdates = new LinkedHashSet<>();
	private Set<Update> adoptUpdates = new LinkedHashSet<>();
	private Set<Update> dropUpdates = new LinkedHashSet<>();
	// list of all modules that is used by some agent as init module
	private Set<Module> initModules = new LinkedHashSet<>();
	// list of all modules that is used by some agent as event module
	private Set<Module> eventModules = new LinkedHashSet<>();

	/**
	 * Create a MAS analysis. After creation you should call {@link #run()} to do
	 * the analysis. FIXME just do it directly?
	 *
	 * @param registry   the {@link FileRegistry}
	 * @param masProgram the {@link MASProgram} to analyse
	 * @param override   can be null or an override of settings
	 */
	public Analysis(FileRegistry registry, MASProgram masProgram, Map.Entry<File, String> override) {
		this.registry = registry;
		this.program = masProgram;
		this.override = override;
	}

	public Set<String> getActionCalls() {
		return Collections.unmodifiableSet(this.actionCalls);
	}

	public Set<Module> getUnusedModuleDefinitions() {
		return Collections.unmodifiableSet(this.unusedModuleDefinitions);
	}

	public Set<Module> getModuleDefinitions() {
		return Collections.unmodifiableSet(this.moduleDefinitions);
	}
	
	
	/**
	 *
	 * @return all action definition programs found
	 */
	public Set<ActionSpecProgram> getActionSpecDefinitions() {
		return Collections.unmodifiableSet(this.actionSpecDefinitions);
	}

	/**
	 *
	 * @return all unused action definitions
	 */
	public Set<UserSpecAction> getUnusedActionDefinitions() {
		return Collections.unmodifiableSet(this.unusedActionDefinitions);
	}

	/**
	 *
	 * @return all action definitions found
	 */
	public Set<UserSpecAction> getActionDefinitions() {
		return Collections.unmodifiableSet(this.actionDefinitions);
	}

	public Set<DatabaseFormula> getPredicateDefinitions() {
		return Collections.unmodifiableSet(this.predicateDefinitions);
	}

	/**
	 *
	 * @return all belief and goal queries from all modules
	 */
	public Set<Query> getPredicateQueries() {
		return Collections.unmodifiableSet(this.predicateQueries);
	}

	/**
	 * @return all knowledge accumulated from all modules
	 */
	public Set<DatabaseFormula> getAllKnowledge() {
		return Collections.unmodifiableSet(this.allKnowledge);
	}

	public Set<DatabaseFormula> getAllBeliefs() {
		return Collections.unmodifiableSet(this.allBeliefs);
	}

	public Set<Update> getBeliefUpdates() {
		return Collections.unmodifiableSet(this.beliefUpdates);
	}

	/**
	 *
	 * @return the formulas that have been defined in the set of database formulas
	 *         but are not used (queried).
	 */
	public Set<DatabaseFormula> getBeliefsUnused() {
		return Collections.unmodifiableSet(this.beliefsUnused);
	}

	public List<Validator<?, ?, ?, ?>> getSubvalidators() {
		return this.subvalidators;
	}

	public FileRegistry getRegistry() {
		return this.registry;
	}

	public MASProgram getProgram() {
		return this.program;
	}

	/**
	 *
	 * @return KR-language files that are used as beliefs
	 */
	public Set<UseClause> getKrBeliefFiles() {
		return Collections.unmodifiableSet(this.krBeliefFiles);
	}

	/**
	 *
	 * @return KR-language files that are used as knowledge
	 */
	public Set<UseClause> getKrKnowledgeFiles() {
		return Collections.unmodifiableSet(this.krKnowledgeFiles);
	}

	/**
	 *
	 * @return KR-language files that are used as goals
	 */
	public Set<UseClause> getKrGoalFiles() {
		return Collections.unmodifiableSet(this.krGoalFiles);
	}

	/**
	 *
	 * @return null, or a map of override information
	 */
	public Map.Entry<File, String> getOverride() {
		return this.override;
	}

	/**
	 *
	 * @return all percept queries from the modules
	 */
	public Set<Query> getPerceptQueries() {
		return Collections.unmodifiableSet(this.allPerceptQueries);
	}

	/**
	 *
	 * @return all goal queries from the modules
	 */
	public Set<Query> getGoalQueries() {
		return Collections.unmodifiableSet(this.goalQueries);
	}

	/**
	 *
	 * @return all goal databaseformulas from the goal files used in the modules.
	 */
	public Set<DatabaseFormula> getGoalDBFs() {
		return Collections.unmodifiableSet(this.goalDbfs);
	}

	/**
	 *
	 * @return all updates from insert actions done in the modules
	 */
	public Set<Update> getInsertUpdates() {
		return Collections.unmodifiableSet(this.insertUpdates);
	}

	/**
	 *
	 * @return all updates from delete actions done in the modules
	 */
	public Set<Update> getDeleteUpdates() {
		return Collections.unmodifiableSet(this.deleteUpdates);
	}

	/**
	 *
	 * @return all updates from adopt actions done in the modules
	 */
	public Set<Update> getAdoptUpdates() {
		return Collections.unmodifiableSet(this.adoptUpdates);
	}

	/**
	 *
	 * @return all updates from drop actions done in the modules
	 */
	public Set<Update> getDropUpdates() {
		return Collections.unmodifiableSet(this.dropUpdates);
	}

	/**
	 *
	 * @return all {@link Module}s that are used by some agent as init module.
	 */
	public Set<Module> getInitModules() {
		return Collections.unmodifiableSet(this.initModules);
	}

	/**
	 *
	 * @return all {@link Module}s that are used by some agent as event module.
	 */
	public Set<Module> getEventModules() {
		return Collections.unmodifiableSet(this.eventModules);
	}

	/**
	 * This runs the actual analysis of the MAS program. SIDE EFFECT: loads the
	 * {@link AgentDefinition}s with KRI
	 *
	 * @return an updated Analysis (this), or null if analysis failed.
	 */
	public void run() {
		MASProgram mas = getProgram();
		if (mas == null) {
			return;
		}
		for (String name : mas.getAgentNames()) {
			AgentDefinition agent = mas.getAgentDefinition(name);
			if (agent == null) {
				continue;
			}
			if (agent.getInitUseClause() != null) {
				fullValidate(agent.getInitUseClause());
				Module init = agent.getInitModule();
				if (init != null) {
					this.actionCalls.add(init.getSignature());
					this.initModules.add(init);
					// for (Rule initrule : init.getRules()) {
					// for (Action<?> initaction : initrule.getAction()) {
					// if (initaction instanceof SendAction) {
					// reportWarning(MASWarning.INIT_MESSAGING,
					// initaction.getSourceInfo());
					// }
					// }
					// }
				}
			}
			if (agent.getEventUseClause() != null) {
				fullValidate(agent.getEventUseClause());
				Module event = agent.getEventModule();
				if (event != null) {
					this.actionCalls.add(event.getSignature());
					this.eventModules.add(event);
				}
			}
			if (agent.getMainUseClause() != null) {
				fullValidate(agent.getMainUseClause());
				Module main = agent.getMainModule();
				if (main != null) {
					this.actionCalls.add(main.getSignature());
				}
			}
			if (agent.getShutdownUseClause() != null) {
				fullValidate(agent.getShutdownUseClause());
				Module shutdown = agent.getShutdownModule();
				if (shutdown != null) {
					this.actionCalls.add(shutdown.getSignature());
				}
			}
		}
		// Set a KR interface for the MAS (based on the first one we can find)
		// CHECK can we do this first?
		KRInterface kri = null;
		for (File file : getRegistry().getSourceFiles()) {
			kri = getRegistry().getProgram(file).getKRInterface();
			if (kri != null) {
				mas.setKRInterface(kri);
				break;
			}
		}
		if (kri == null) {
			return;
		}
		for (String name : mas.getAgentNames()) {
			AgentDefinition agent = mas.getAgentDefinition(name);
			agent.setKRInterface(kri);
		}
		if (getRegistry().hasAnyError()) {
			return;
		}

		// MAS-level validation of unused predicates, actions, and/or modules
		for (Validator<?, ?, ?, ?> use : getSubvalidators()) {
			if (use instanceof ActionSpecValidator) {
				// Add to the set of defined actions.
				ActionSpecValidator spec = (ActionSpecValidator) use;
				ActionSpecProgram program = spec.getProgram();
				this.actionSpecDefinitions.add(program);
				for (UserSpecAction action : program.getActionSpecifications()) {
					this.actionDefinitions.add(action);
				}
				// Fetch KR definitions and usages
				ActionSpecValidatorSecondPass specPass = (ActionSpecValidatorSecondPass) use.getSecondPass();
				this.allKnowledge.addAll(specPass.getKnowledge());
				this.allBeliefs.addAll(specPass.getBeliefs());
				this.predicateDefinitions.addAll(specPass.getKnowledge());
				this.predicateQueries.addAll(specPass.getBeliefQueries());
				// Module validator collects action call contents already.
			} else if (use instanceof PlannerValidator) {
				
				// Add to the set of defined plans.
				PlannerValidator spec = (PlannerValidator) use;
				this.moduleDefinitions.add(spec.getProgram());
				
				// Fetch KR definitions and usages
				PlannerValidatorSecondPass specPass = (PlannerValidatorSecondPass) use.getSecondPass();
				this.allKnowledge.addAll(specPass.getKnowledge());
				this.allBeliefs.addAll(specPass.getBeliefs());
				this.predicateDefinitions.addAll(specPass.getKnowledge());
				this.predicateDefinitions.addAll(specPass.getBeliefs());
				this.predicateQueries.addAll(specPass.getBeliefQueries());
				this.predicateQueries.addAll(specPass.getGoalQueries());

				this.insertUpdates.addAll(specPass.getInsertUpdates());
				this.deleteUpdates.addAll(specPass.getDeleteUpdates());
				this.adoptUpdates.addAll(specPass.getAdoptUpdates());
				this.dropUpdates.addAll(specPass.getDropUpdates());

				this.goalQueries.addAll(specPass.getGoalQueries());
				this.goalDbfs.addAll(specPass.getGoalDbfs());
				
				// Fetch module/action calls
				this.actionCalls.addAll(specPass.getActionsUsed());
				this.beliefUpdates.addAll(specPass.getBeliefUpdates());
				this.allPerceptQueries.addAll(specPass.getPerceptQueries());
				
				
				
			} else if (use instanceof ModuleValidator) {
				// Add to the set of defined modules.
				ModuleValidator spec = (ModuleValidator) use;
				this.moduleDefinitions.add(spec.getProgram());
				// Fetch KR definitions and usages
				ModuleValidatorSecondPass specPass = (ModuleValidatorSecondPass) use.getSecondPass();
				this.allKnowledge.addAll(specPass.getKnowledge());
				this.allBeliefs.addAll(specPass.getBeliefs());
				this.predicateDefinitions.addAll(specPass.getKnowledge());
				this.predicateDefinitions.addAll(specPass.getBeliefs());
				this.predicateQueries.addAll(specPass.getBeliefQueries());
				this.predicateQueries.addAll(specPass.getGoalQueries());

				this.insertUpdates.addAll(specPass.getInsertUpdates());
				this.deleteUpdates.addAll(specPass.getDeleteUpdates());
				this.adoptUpdates.addAll(specPass.getAdoptUpdates());
				this.dropUpdates.addAll(specPass.getDropUpdates());

				this.goalQueries.addAll(specPass.getGoalQueries());
				this.goalDbfs.addAll(specPass.getGoalDbfs());
				// Fetch module/action calls
				this.actionCalls.addAll(specPass.getActionsUsed());
				this.beliefUpdates.addAll(specPass.getBeliefUpdates());
				this.allPerceptQueries.addAll(specPass.getPerceptQueries());
			}
		}

		// Process
		this.unusedModuleDefinitions.addAll(this.moduleDefinitions);
		for (Module module : this.unusedModuleDefinitions.toArray(new Module[this.unusedModuleDefinitions.size()])) {
			if (this.actionCalls.contains(module.getSignature())) {
				this.unusedModuleDefinitions.remove(module);
			}
		}
		this.unusedActionDefinitions.addAll(this.actionDefinitions);
		for (UserSpecAction action : this.unusedActionDefinitions
				.toArray(new UserSpecAction[this.unusedActionDefinitions.size()])) {
			if (this.actionCalls.contains(action.getSignature())) {
				this.unusedActionDefinitions.remove(action);
			}
		}
		Set<String> had = new LinkedHashSet<>();
		for (DatabaseFormula unused : kri.getUnused(this.predicateDefinitions, this.predicateQueries)) {
			if (had.add(unused.getSignature())) {
				this.beliefsUnused.add(unused);
			}
		}
	}

	/**
	 * create a {@link Validator} for the {@link UseClause}. Call
	 * {@link Validator#validate(false)}. If that succeeds with a {@link Program},
	 * recursively validate all useclauses of this program. Then call
	 * {@link Validator#validate(true)} to do the second pass of validation.
	 *
	 * If the {@link Program} for this use is already in the registry, this returns
	 * immediately.
	 *
	 * As side-effects, this updates (1) the {@link #registry} (2) the
	 * {@link #subvalidators}
	 *
	 * @param use the {@link UseClause} to check.
	 */
	private void fullValidate(UseClause use) {
		if (use == null || use.getUseCase() == null) {
			return;
		}
		for (File file : use.getResolvedReference()) {
			// Check for existing...
			Program returned = this.registry.getProgram(file);
			if (returned != null) {
				continue;
			}
			// Parse if we need to (first pass).
			Validator<?, ?, ?, ?> validator = null;
			switch (use.getUseCase()) {
			case MAIN:
			case INIT:
			case EVENT:
			case SHUTDOWN:
			case MODULE:
				try {
					validator = new ModuleValidator(file.getCanonicalPath(), this.registry);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case ACTIONSPEC:
				try {
					validator = new ActionSpecValidator(file.getCanonicalPath(), this.registry);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case PLANNER:
				try {
					validator = new PlannerValidator(file.getCanonicalPath(), this.registry);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case GOALS:
				this.krGoalFiles.add(use);
				break;
			case BELIEFS:
				this.krBeliefFiles.add(use);
				break;
			case KNOWLEDGE:
				this.krKnowledgeFiles.add(use);
				break;
			default:
				break;
			}
			if (validator != null) {
				getSubvalidators().add(validator);
				if (getOverride() != null && validator.getSource().equals(getOverride().getKey())) {
					validator.override(getOverride().getValue());
				}
				// Do a first pass on the current file, and recursively process any
				// use case we encountered in this file's first pass.
				validator.validate(false);
				if (validator.getProgram() != null) {
					for (UseClause sub : validator.getProgram().getUseClauses()) {
						fullValidate(sub);
					}
				}
				// Execute the second pass (so after all dependencies have been
				// fully processed).
				validator.validate(true);
			}
		}
	}
}
