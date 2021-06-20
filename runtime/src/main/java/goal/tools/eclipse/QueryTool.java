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
package goal.tools.eclipse;

import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import goal.core.agent.Agent;
import goal.core.agent.GOALInterpreter;
import goal.tools.debugger.SteppingDebugger;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALUserError;
import krTools.exceptions.ParserException;
import krTools.language.Substitution;
import krTools.language.Term;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.errors.Message;
import languageTools.errors.module.ModuleErrorStrategy;
import languageTools.parser.GOALLexer;
import languageTools.parser.MOD2GParser;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.ExitModuleAction;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.actions.UserSpecOrModuleCall;
import languageTools.program.agent.msc.Macro;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.mas.AgentDefinition;
import mentalState.MentalStateWithEvents;

public class QueryTool {
	private final Agent<? extends GOALInterpreter<?>> agent;

	public QueryTool(final Agent<? extends GOALInterpreter<?>> agent) {
		this.agent = agent;
	}

	public String doquery(final String userEnteredQuery) throws GOALUserError {
		final MentalStateCondition msc = parseMSC(userEnteredQuery);
		final Object debugger = this.agent.getController().getDebugger();
		if (debugger instanceof SteppingDebugger) {
			final Substitution[] substitutions = ((SteppingDebugger) debugger).query(msc);
			if (substitutions == null) {
				return "Query could not be executed";
			} else if (substitutions.length == 0) {
				return "No solutions";
			} else {
				String resulttext = "";
				for (final Substitution s : substitutions) {
					resulttext = resulttext + s + "\n";
				}
				return resulttext;
			}
		} else {
			return "Unexpected debugger: " + debugger;
		}
	}

	public String doaction(final String userEnteredAction) throws GOALUserError, GOALActionFailedException {
		final ActionCombo action = parseAction(userEnteredAction);
		final MentalStateWithEvents mentalState = this.agent.getController().getRunState().getMentalState();
		if (mentalState == null) {
			throw new GOALUserError("agent '" + this.agent.getId() + "' has not yet initialized its databases");
		} else {
			this.agent.getController().doPerformAction(action);
			return "Executed " + action;
		}
	}

	/**
	 * Creates an embedded module parser that can parse the given string.
	 *
	 * @param pString is the string to be parsed.
	 * @return a MOD2GParser.
	 */
	private MOD2GParser prepareModuleParser(ModuleValidator validator, String pString) {
		CharStream charstream = CharStreams.fromString(pString, validator.getFilename());
		GOALLexer lexer = new GOALLexer(charstream);
		lexer.removeErrorListeners();
		lexer.addErrorListener(validator);
		MOD2GParser parser = new MOD2GParser(new CommonTokenStream(lexer));
		parser.setErrorHandler(new ModuleErrorStrategy());
		parser.removeErrorListeners();
		parser.addErrorListener(validator);
		return parser;
	}

	/**
	 * Parse a string to a {@link MentalStateCondition}.
	 *
	 * @param mentalStateCondition Input string that should represent a mental state
	 *                             condition.
	 * @return The mental state condition that resulted from parsing the input
	 *         string.
	 * @throws GOALException   When the parser throws a RecognitionException, which
	 *                         should have been buffered and ignored.
	 * @throws ParserException
	 */
	public MentalStateCondition parseMSC(String mentalStateCondition) throws GOALUserError {
		// Try to parse the MSC.
		AgentDefinition agent = this.agent.getController().getProgram();
		FileRegistry tempRegistry = new FileRegistry(agent.getRegistry());
		ModuleValidator sub = new ModuleValidator("query-condition", tempRegistry);
		Module temp = new Module(tempRegistry, null);
		temp.setKRInterface(agent.getKRInterface());
		sub.overrideProgram(temp);

		MOD2GParser parser = prepareModuleParser(sub, mentalStateCondition);
		MentalStateCondition msc = sub.visitMsc(parser.msc());

		// check for errors in the parser
		if (!tempRegistry.getAllErrors().isEmpty()) {
			String msg = "'" + mentalStateCondition + "' cannot be parsed: ";
			for (Message err : tempRegistry.getAllErrors()) {
				msg += err.toString() + ". ";
			}
			throw new GOALUserError(msg);
		}
		// Check for macros, which cannot be resolved here because the proper
		// context is lacking.
		if (msc == null) {
			throw new GOALUserError("not a valid mental state condition");
		} else {
			for (MentalFormula formula : msc.getSubFormulas()) {
				if (formula instanceof Macro) {
					throw new GOALUserError("cannot use macros here", formula.getSourceInfo());
				}
			}
			return msc;
		}
	}

	/**
	 * Parse string as a mental action.
	 */
	@SuppressWarnings("unchecked")
	public ActionCombo parseAction(String action) throws GOALUserError {
		// Try to parse the action
		AgentDefinition agent = this.agent.getController().getProgram();
		FileRegistry tempRegistry = new FileRegistry(agent.getRegistry());
		ModuleValidator sub = new ModuleValidator("query-action", tempRegistry);
		Module temp = new Module(agent.getRegistry(), null);
		temp.setKRInterface(agent.getKRInterface());
		sub.overrideProgram(temp);

		MOD2GParser parser = prepareModuleParser(sub, action);
		ActionCombo combo = sub.visitActioncombo(parser.actioncombo());

		// Module calls are not allowed in the query tool, so in case the action
		// is not a mental action we will assume it is a user-specified action
		// that can be send to the environment.
		if (combo == null || combo.size() == 0) {
			throw new GOALUserError("not a valid action");
		} else {
			ActionCombo returned = new ActionCombo(null);
			for (Action<?> act : combo.getActions()) {
				if (act instanceof UserSpecOrModuleCall) {
					returned.addAction(new UserSpecAction(act.getName(), (List<Term>) act.getParameters(), true, null,
							null, null, null));
				} else if (!(act instanceof ExitModuleAction || act instanceof ModuleCallAction)) {
					returned.addAction(act);
				} else {
					throw new GOALUserError("cannot call module actions here.");
				}
			}
			return returned;
		}
	}
}
