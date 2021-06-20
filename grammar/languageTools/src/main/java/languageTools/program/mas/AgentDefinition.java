/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package languageTools.program.mas;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.parser.ParsedObject;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.program.Program;
import languageTools.program.ProgramMap;
import languageTools.program.agent.Module;
import languageTools.program.kr.KRProgram;
import languageTools.program.mas.UseClause.UseCase;

/**
 * Container class for keeping a record of the agent definition section in a MAS
 * file. Stores the agent's name and (resolved) references (to files) with their
 * use cases. A use case indicates what the file should be used for (e.g. as
 * knowledge).
 */
public class AgentDefinition extends Program {
	/**
	 * Name of the agent defined.
	 */
	private final String name;

	/**
	 * Creates a new agent definition with given name.
	 *
	 * @param agent The name of the agent defined.
	 */
	public AgentDefinition(final String name, final FileRegistry registry, final SourceInfo info) {
		super(registry, info);
		this.name = name;
	}

	@Override
	public ProgramMap getMap() {
		final ProgramMap main = super.getMap();
		if (main.isEmpty()) {
			main.register(this);
			for (final File source : this.registry.getSourceFiles()) {
				final Program sub = this.registry.getProgram(source);
				main.merge(sub.getMap());
			}
		}
		return main;
	}

	@Override
	protected void register(final ParsedObject object) {
		throw new RuntimeException("cannot register objects on a defintion");
	}

	/**
	 * @return The name of the definition.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Adds a use clause. Fails to add clause if init, event, or main module already
	 * have been added.
	 *
	 * @param clause A use clause.
	 * @return {@code true} if the clause was successfully added; {@code false}
	 *         otherwise.
	 */
	@Override
	public boolean addUseClause(final UseClause clause) {
		switch (clause.getUseCase()) {
		case INIT:
			return (getInitUseClause() == null) ? super.addUseClause(clause) : false;
		case EVENT:
			return (getEventUseClause() == null) ? super.addUseClause(clause) : false;
		case MAIN:
			return (getMainUseClause() == null) ? super.addUseClause(clause) : false;
		case SHUTDOWN:
			return (getShutdownUseClause() == null) ? super.addUseClause(clause) : false;
		default: // A MAS file should not have a clause for any other cases.
			return false;
		}
	}

	/**
	 * @return A use clause with init use case, {@code null} otherwise.
	 */
	public UseClause getInitUseClause() {
		for (final UseClause useClause : getUseClauses()) {
			if (useClause.getUseCase() == UseCase.INIT) {
				return useClause;
			}
		}
		return null;
	}

	/**
	 * @return A use clause with event use case, {@code null} otherwise.
	 */
	public UseClause getEventUseClause() {
		for (final UseClause useClause : getUseClauses()) {
			if (useClause.getUseCase() == UseCase.EVENT) {
				return useClause;
			}
		}
		return null;
	}

	/**
	 * @return A use clause with main use case, {@code null} otherwise.
	 */
	public UseClause getMainUseClause() {
		for (final UseClause useClause : getUseClauses()) {
			if (useClause.getUseCase() == UseCase.MAIN) {
				return useClause;
			}
		}
		return null;
	}

	/**
	 * @return A use clause with shutdown use case, {@code null} otherwise.
	 */
	public UseClause getShutdownUseClause() {
		for (final UseClause useClause : getUseClauses()) {
			if (useClause.getUseCase() == UseCase.SHUTDOWN) {
				return useClause;
			}
		}
		return null;
	}

	/**
	 * @return The file with a module that should be used as init module, or
	 *         {@code null} if there is no such file.
	 */
	public File getInitModuleFile() {
		final UseClause init = getInitUseClause();
		final List<File> resolved = (init == null) ? new ArrayList<>(0) : init.getResolvedReference();
		return resolved.isEmpty() ? null : resolved.get(0);
	}

	/**
	 * @return The module used for initializing the agent, or {@code null} if there
	 *         is no such module (or the init module has not been parsed yet).
	 */
	public Module getInitModule() {
		final File file = getInitModuleFile();
		return (file == null) ? null : (Module) this.registry.getProgram(file);
	}

	/**
	 * @return The file with a module that should be used as event module, or
	 *         {@code null} if there is no such file.
	 */
	public File getEventModuleFile() {
		final UseClause event = getEventUseClause();
		final List<File> resolved = (event == null) ? new ArrayList<>(0) : event.getResolvedReference();
		return resolved.isEmpty() ? null : resolved.get(0);
	}

	/**
	 * @return The module used for processing events of the agent, or {@code null}
	 *         if there is no such module (or the event module has not been parsed
	 *         yet).
	 */
	public Module getEventModule() {
		final File file = getEventModuleFile();
		return (file == null) ? null : (Module) this.registry.getProgram(file);
	}

	/**
	 * @return The file with a module that should be used as main module, or
	 *         {@code null} if there is no such file.
	 */
	public File getMainModuleFile() {
		final UseClause main = getMainUseClause();
		final List<File> resolved = (main == null) ? new ArrayList<>(0) : main.getResolvedReference();
		return resolved.isEmpty() ? null : resolved.get(0);
	}

	/**
	 * @return The module used for main decision making of the agent, or
	 *         {@code null} if there is no such module (or the main module has not
	 *         been parsed yet).
	 */
	public Module getMainModule() {
		final File file = getMainModuleFile();
		return (file == null) ? null : (Module) this.registry.getProgram(file);
	}

	/**
	 * @return The file with a module that should be used as shutdown module, or
	 *         {@code null} if there is no such file.
	 */
	public File getShutdownModuleFile() {
		final UseClause shutdown = getShutdownUseClause();
		final List<File> resolved = (shutdown == null) ? new ArrayList<>(0) : shutdown.getResolvedReference();
		return resolved.isEmpty() ? null : resolved.get(0);
	}

	/**
	 * @return The module used for shutting down the agent of the agent, or
	 *         {@code null} if there is no such module (or the shutdown module has
	 *         not been parsed yet).
	 */
	public Module getShutdownModule() {
		final File file = getShutdownModuleFile();
		return (file == null) ? null : (Module) this.registry.getProgram(file);
	}

	/**
	 * Recursively searches for (implicitly) referenced modules. Terminates if no
	 * new modules are found. Starts with init, event and main module as initial set
	 * of modules to start search with.
	 *
	 * @return List of all (indirectly) referenced modules from init, event and main
	 *         modules.
	 */
	// FIXME: now only used in learner for initialization purposes, whereas
	// adaptive module should initialize its own learner (but that requires
	// persistence of the module executors).
	public Set<Module> getAllReferencedModules() {
		final Set<Module> modules = new LinkedHashSet<>();
		final Module init = getInitModule();
		if (init != null) {
			init.referencedModules(modules);
		}
		final Module event = getEventModule();
		if (event != null) {
			event.referencedModules(modules);
		}
		final Module main = getMainModule();
		if (main != null) {
			main.referencedModules(modules);
		}
		return modules;
	}

	private List<DatabaseFormula> getKnowledge(final Program program, final Set<File> had) {
		final List<DatabaseFormula> knowledge = new LinkedList<>();
		for (final UseClause useClause : program.getUseClauses()) {
			for (final File source : useClause.getResolvedReference()) {
				final Program subprogram = this.registry.getProgram(source);
				if (useClause.getUseCase().equals(UseCase.KNOWLEDGE) && subprogram instanceof KRProgram) {
					knowledge.addAll(((KRProgram) subprogram).getDBFormulas());
				} else if (subprogram != null && had.add(subprogram.getSourceFile())) {
					knowledge.addAll(getKnowledge(subprogram, had));
				}
			}
		}
		return knowledge;
	}

	public List<DatabaseFormula> getAllKnowledge() {
		return getKnowledge(this, new LinkedHashSet<File>());
	}

	private Set<File> getKnowledgeFiles(final Program program, final Set<File> had) {
		final Set<File> knowledgeFiles = new LinkedHashSet<>();
		for (final UseClause useClause : program.getUseClauses()) {
			for (final File source : useClause.getResolvedReference()) {
				final Program subprogram = this.registry.getProgram(source);
				if (useClause.getUseCase().equals(UseCase.KNOWLEDGE) && subprogram instanceof KRProgram) {
					knowledgeFiles.add(subprogram.getSourceFile());
				} else if (subprogram != null && had.add(subprogram.getSourceFile())) {
					knowledgeFiles.addAll(getKnowledgeFiles(subprogram, had));
				}
			}
		}
		return knowledgeFiles;
	}

	public Set<File> getAllKnowledgeFiles() {
		return getKnowledgeFiles(this, new LinkedHashSet<>());
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int hashCode() {
		return ((this.name == null) ? 0 : this.name.hashCode());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof AgentDefinition)) {
			return false;
		}
		final AgentDefinition other = (AgentDefinition) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
