/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
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
package goal.tools;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableSet;

import goal.tools.debugger.BreakPoint;
import goal.tools.debugger.BreakPoint.Type;
import goal.tools.errorhandling.Warning;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.program.Program;
import languageTools.program.actionspec.ActionSpecProgram;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.rules.Rule;
import languageTools.program.mas.MASProgram;
import languageTools.program.test.TestProgram;

/**
 * Manages breakpoints set by a user in a source file.
 */
public class BreakpointManager {
	/**
	 * Map that keeps track for each file (not necessarily an agent file!) for
	 * which {@link IParsedObject}s present in that file a breakpoint has been
	 * set.
	 */
	private final Set<SourceInfo> breakpoints;
	/**
	 * A registry of source files and associated parsed programs.
	 */
	private final FileRegistry registry;

	/**
	 * Creates a breakpoint manager.
	 *
	 * @param registry
	 *            A registry of source files that also provides the parsed
	 *            programs for each source file.
	 */
	public BreakpointManager(FileRegistry registry) {
		this.registry = registry;
		this.breakpoints = Collections.newSetFromMap(new ConcurrentHashMap<SourceInfo, Boolean>());
	}

	// FIXME: only here to fix DebugTool
	public FileRegistry getRegistry() {
		return this.registry;
	}

	public void clear() {
		this.breakpoints.clear();
	}

	/**
	 * Provides the set of breakpoints that have set for the given file.
	 *
	 * @param file
	 *            A source file in which breakpoints can (have) be(en) set.
	 * @return A set of {@link SourceInfo} objects that indicate where
	 *         breakpoints have been set, or {@code null} if no breakpoints have
	 *         been set for the file.
	 */
	public Set<SourceInfo> getBreakpoints() {
		return ImmutableSet.copyOf(this.breakpoints);
	}

	/**
	 * Sets breakpoints for the given file based on the given set of
	 * breakpoints. Removes or overwrites all breakpoints that were previously
	 * set in the file.
	 * <p>
	 * Matches the given line numbers with objects on which breakpoints may be
	 * set but the actual location of the breakpoints that are set may have
	 * different line numbers.
	 * </p>
	 *
	 * @param file
	 *            The file in which the breakpoints should be set.
	 * @param bpts
	 *            A set of breakpoints.
	 */
	public void setBreakpoints(File file, Set<BreakPoint> bpts) {
		// Get the program associated with the file and compute the set of
		// possible breakpoint locations.
		Program program = this.registry.getProgram(file);
		List<SourceInfo> pbplocs = getBreakpointObjects(program, false);
		List<SourceInfo> pcbplocs = getBreakpointObjects(program, true);

		// Find a corresponding breakpoint location that matches with each
		// breakpoint.
		Set<SourceInfo> bplocs = new LinkedHashSet<>();
		SourceInfo location;
		for (BreakPoint bpt : bpts) {
			try {
				if (bpt.getType() == Type.CONDITIONAL) {
					location = addBreakpoint(file, pcbplocs, bpt);
				} else {
					location = addBreakpoint(file, pbplocs, bpt);
				}
			} catch (IOException e) {
				location = null;
			}
			if (location == null) {
				new Warning("could not add " + bpt + ".").emit();
			} else {
				bplocs.add(location);
			}
		}
		this.breakpoints.addAll(bplocs);
	}

	/**
	 * Match a breakpoint with a possible breakpoint location in the file.
	 *
	 * @param pbplocs
	 *            Possible (ordered) set of locations for breakpoints.
	 * @param bpt
	 *            The breakpoint that should be added.
	 *
	 * @return
	 *         <ul>
	 *         <li>-1 if there is a reference, but there is no breakpoint object
	 *         after or on the indicated line</li>
	 *         <li>A number &ge; lineNumber indicating the line on which a
	 *         breakpoint was set.</li>
	 *         </ul>
	 */
	private static SourceInfo addBreakpoint(File file, List<SourceInfo> pbplocs, BreakPoint bpt) throws IOException {
		for (SourceInfo bp : pbplocs) {
			// We may assume that possible breakpoint locations have been
			// ordered, so
			// the first match is the first object after the given line in the
			// given file.
			if (definedAfter(file.getCanonicalPath(), bpt.getLine(), bp)) {
				return bp;
			}
		}
		return null;
	}

	/**
	 * Determines if a source is located after a certain line in a certain file.
	 *
	 * @param source
	 *            The referenced file path.
	 * @param line
	 *            The referenced line number.
	 * @param bp
	 *            The source to check.
	 * @return {@code true} iff the given source is located in the given file,
	 *         after or at the start of the given line.
	 */
	private static boolean definedAfter(String source, int line, SourceInfo bp) {
		if (source.equals(bp.getSource())) {
			return bp.getLineNumber() >= line;
		} else {
			return false;
		}
	}

	/**
	 * Collects the objects in the program on which a breakpoint can be set.
	 *
	 * @param program
	 *            A program.
	 * @param conditionalOnly
	 *            {@code true} means that only code locations relevant for
	 *            setting conditional breakpoints should be returned.
	 * @return A list of code locations in the program on which a breakpoint can
	 *         be set.
	 */
	private List<SourceInfo> getBreakpointObjects(Program program, boolean conditionalOnly) {
		List<SourceInfo> objects = new LinkedList<>();
		if (program instanceof MASProgram || program instanceof TestProgram) {
			// Breakpoints cannot be set in a MAS or test file.
			return objects;
		} else if (program instanceof Module) {
			// Collect possible breakpoint locations in a module file.
			for (Rule rule : ((Module) program).getRules()) {
				// Add breakpoint location for rule condition.
				if (!conditionalOnly) {
					objects.add(rule.getCondition().getSourceInfo());
				}
				// Add breakpoint location for action of rule.
				for (Action<?> action : rule.getAction()) {
					// Add rules of nested rules section but do not add start of
					// nested rules section itself.
					if (action instanceof ModuleCallAction && ((ModuleCallAction) action).getTarget().isAnonymous()) {
						objects.addAll(getBreakpointObjects(((ModuleCallAction) action).getTarget(), conditionalOnly));
					} else {
						objects.add(action.getSourceInfo());
					}
				}
			}
		} else if (program instanceof ActionSpecProgram) {
			// Collect possible breakpoint locations in an action specification
			// file.
			for (UserSpecAction actionSpec : ((ActionSpecProgram) program).getActionSpecifications()) {
				// Add pre- and post-conditions.
				objects.add(actionSpec.getPrecondition().getSourceInfo());
				if (actionSpec.getPositivePostcondition() != null) {
					objects.add(actionSpec.getPositivePostcondition().getSourceInfo());
				}
				if (actionSpec.getNegativePostcondition() != null) {
					objects.add(actionSpec.getNegativePostcondition().getSourceInfo());
				}
			}
		}

		Collections.sort(objects);
		return objects;
	}

}