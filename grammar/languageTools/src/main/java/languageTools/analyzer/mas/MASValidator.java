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

package languageTools.analyzer.mas;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import cognitiveKr.CognitiveKR;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.Validator;
import languageTools.analyzer.ValidatorSecondPass;
import languageTools.errors.ValidatorWarning;
import languageTools.errors.mas.MASError;
import languageTools.errors.mas.MASErrorStrategy;
import languageTools.errors.mas.MASWarning;
import languageTools.parser.InputStreamPosition;
import languageTools.parser.MAS2GLexer;
import languageTools.parser.MAS2GParser;
import languageTools.parser.MAS2GParser.AgentContext;
import languageTools.parser.MAS2GParser.AlphaContext;
import languageTools.parser.MAS2GParser.ConstantContext;
import languageTools.parser.MAS2GParser.ConstraintContext;
import languageTools.parser.MAS2GParser.DecayContext;
import languageTools.parser.MAS2GParser.EntityContext;
import languageTools.parser.MAS2GParser.EntitynameContext;
import languageTools.parser.MAS2GParser.EntitytypeContext;
import languageTools.parser.MAS2GParser.EnvironmentContext;
import languageTools.parser.MAS2GParser.EpsilonContext;
import languageTools.parser.MAS2GParser.FunctionContext;
import languageTools.parser.MAS2GParser.GammaContext;
import languageTools.parser.MAS2GParser.InitExprContext;
import languageTools.parser.MAS2GParser.InitKeyValueContext;
import languageTools.parser.MAS2GParser.InstructionContext;
import languageTools.parser.MAS2GParser.LaunchRuleContext;
import languageTools.parser.MAS2GParser.ListContext;
import languageTools.parser.MAS2GParser.MasContext;
import languageTools.parser.MAS2GParser.MaxconstraintContext;
import languageTools.parser.MAS2GParser.NameconstraintContext;
import languageTools.parser.MAS2GParser.NrconstraintContext;
import languageTools.parser.MAS2GParser.PolicyContext;
import languageTools.parser.MAS2GParser.RefContext;
import languageTools.parser.MAS2GParser.StringContext;
import languageTools.parser.MAS2GParser.UseCaseContext;
import languageTools.parser.MAS2GParser.UseClauseContext;
import languageTools.parser.MAS2GVisitor;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.Module;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.Entity;
import languageTools.program.mas.LaunchInstruction;
import languageTools.program.mas.LaunchRule;
import languageTools.program.mas.MASProgram;
import languageTools.program.mas.UseClause;
import languageTools.program.mas.UseClause.UseCase;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.mas.AgentSymbol;
import languageTools.utils.Extension;
import languageTools.utils.ReferenceResolver;

/**
 * Validates a MAS file and constructs a MAS program.
 */
public class MASValidator extends Validator<MAS2GLexer, MAS2GParser, MASErrorStrategy, MASProgram>
		implements MAS2GVisitor<Object> {
	private static final String STAR = "*";

	private MAS2GParser parser;

	private static MASErrorStrategy strategy = null;

	/**
	 * Symbol table with (possible) agent names.
	 */
	private final SymbolTable agentNames = new SymbolTable();

	/**
	 * Creates a MAS validator for file with given name.
	 *
	 * @param filename Name of a file.
	 */
	public MASValidator(String filename, FileRegistry registry) {
		super(filename, registry);
	}

	@Override
	protected MASErrorStrategy getTheErrorStrategy() {
		if (strategy == null) {
			strategy = new MASErrorStrategy();
		}
		return strategy;
	}

	/**
	 * @return Symbol table with agent file references.
	 */
	public SymbolTable getSymbolTable() {
		return this.agentNames;
	}

	@Override
	protected MAS2GLexer getNewLexer(CharStream stream) {
		return new MAS2GLexer(stream);
	}

	@Override
	protected MAS2GParser getNewParser(TokenStream stream) {
		this.parser = new MAS2GParser(stream);
		return this.parser;
	}

	@Override
	protected ParseTree startParser() {
		return this.parser.mas();
	}

	@Override
	protected MASProgram getNewProgram(File masfile) throws IOException {
		return new MASProgram(this.registry, new InputStreamPosition(0, 0, 0, 0, masfile.getCanonicalPath()));
	}

	@Override
	public MASProgram getProgram() {
		return (MASProgram) super.getProgram();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation calls {@link ParseTree#accept} on the specified
	 * tree.
	 * </p>
	 */
	@Override
	public Void visit(ParseTree tree) {
		tree.accept(this);
		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitMas(MasContext ctx) {
		if (ctx.environment() != null) {
			visitEnvironment(ctx.environment());
		}

		for (AgentContext agentCtx : ctx.agent()) {
			visitAgent(agentCtx);
		}

		// TODO VALIDATE: Check whether KR files used by different agents belong
		// to one and the same KRT

		if (ctx.policy() != null) {
			visitPolicy(ctx.policy());
		}

		return null; // Java says must return something even when Void
	}

	// -------------------------------------------------------------
	// ENVIRONMENT section
	// -------------------------------------------------------------

	@Override
	public Void visitEnvironment(EnvironmentContext ctx) {
		/**
		 * Get the path to the environment interface file. Should be a jar file.
		 *
		 * The MAS file is used as an absolute reference to resolve relative references.
		 */
		File environmentfile = null;
		String filename = "";
		if (ctx.ref() != null) {
			filename = visitRef(ctx.ref());
			List<File> resolved = ReferenceResolver.resolveReference(filename, Extension.JAR,
					getPathRelativeToSourceFile(""));
			environmentfile = (resolved.size() == 1) ? resolved.get(0) : null;
		}
		if (environmentfile == null || !environmentfile.isFile()) {
			reportError(MASError.ENVIRONMENT_COULDNOT_FIND, ctx, filename);
		} else {
			getProgram().setEnvironmentfile(environmentfile);
		}

		/**
		 * Get list of key-value initialization parameters.
		 */
		for (InitKeyValueContext pair : ctx.initKeyValue()) {
			visitInitKeyValue(pair);
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public String visitRef(RefContext ctx) {
		if (ctx == null) {
			return "";
		} else if (ctx.string() != null) {
			return visitString(ctx.string());
		} else {
			String path = "";
			for (String component : ctx.getText().split("\\.")) {
				path = FilenameUtils.concat(path, component);
			}
			return path;
		}
	}

	@Override
	public Void visitInitKeyValue(InitKeyValueContext ctx) {
		boolean problem = (ctx.exception != null);

		// We can't check whether specified environment interface supports
		// parameter key
		// Just check whether it's not used more than once
		String key = null;
		if (ctx.ID() != null) {
			key = ctx.ID().getText();
			if (getProgram().getInitParameters().containsKey(key)) {
				problem = reportWarning(MASWarning.INIT_DUPLICATE_KEY, ctx.ID(), key);
			}
		}

		// Get parameter value
		Object value = visitInitExpr(ctx.initExpr());

		// If value equals null, we did not recognize a valid initialization
		// parameter
		if (value == null && ctx.initExpr() != null) {
			problem = reportError(MASError.INIT_UNRECOGNIZED_PARAMETER, ctx.initExpr(), ctx.initExpr().getText());
		}

		// Add key-value pair as initialization parameter to MAS program (only
		// if no problems were detected).
		if (!problem && (key != null) && (value != null)) {
			getProgram().addInitParameter(key, value);
		}

		return null; // Java says must return something even when Void
	}

	/**
	 * @return {@code null} if no valid parameter was recognized.
	 */
	@Override
	public Object visitInitExpr(InitExprContext ctx) {
		if (ctx != null) {
			if (ctx.constant() != null) {
				return visitConstant(ctx.constant());
			} else if (ctx.function() != null) {
				return visitFunction(ctx.function());
			} else if (ctx.list() != null) {
				return visitList(ctx.list());
			}
		}
		return null;
	}

	@Override
	public Object visitConstant(ConstantContext ctx) {
		if (ctx.ID() != null) {
			return ctx.ID().getText();
		} else if (ctx.FLOAT() != null) {
			return Double.parseDouble(ctx.FLOAT().getText());
		} else if (ctx.INT() != null) {
			return Integer.parseInt(ctx.INT().getText());
		} else if (ctx.string() != null) {
			return visitString(ctx.string());
		} else {
			// We did not recognize a valid initialization parameter.
			reportError(MASError.INIT_UNRECOGNIZED_PARAMETER, ctx, ctx.getText());
			return null;
		}
	}

	@Override
	public Object visitFunction(FunctionContext ctx) {
		boolean problem = false;

		// Get function name
		String name = "";
		if (ctx.ID() == null) {
			problem = true;
		} else {
			name = ctx.ID().getText();
		}

		// Get function parameters
		int nrOfPars = (ctx.initExpr() == null) ? 0 : ctx.initExpr().size();
		Object[] parameters = new Object[nrOfPars];
		for (int i = 0; i < nrOfPars; i++) {
			InitExprContext expr = ctx.initExpr(i);
			if (expr == null) {
				problem = true;
				continue;
			}
			parameters[i] = visitInitExpr(expr);
		}

		if (problem) {
			return null;
		} else {
			return new AbstractMap.SimpleEntry<>(name, parameters);
		}
	}

	@Override
	public Object visitList(ListContext ctx) {
		boolean problem = false;

		int nrOfPars = (ctx.initExpr() == null) ? 0 : ctx.initExpr().size();
		List<Object> list = new ArrayList<>(nrOfPars);
		for (int i = 0; i < nrOfPars; i++) {
			InitExprContext expr = ctx.initExpr(i);
			if (expr == null) {
				problem = true;
				continue;
			}
			list.add(visitInitExpr(expr));
		}

		if (problem) {
			return null;
		} else {
			return list;
		}
	}

	// -------------------------------------------------------------
	// AGENT DEFINITIONS
	// -------------------------------------------------------------

	@Override
	public Object visitAgent(AgentContext ctx) {
		String agentName;
		if (ctx.ID() == null) {
			// Something bad went wrong, we cannot continue
			return null;
		} else {
			// Get agent name
			agentName = ctx.ID().getText();
		}

		// Get the use clauses and resolve references
		AgentDefinition agentDf = processAgentDefinition(agentName, ctx.useClause(), getSourceInfo(ctx));

		// Construct agent symbol and add it to symbol table for later reference
		// (if key does not yet exist).
		// VALIDATE: Check whether agent has already been defined
		if (!this.agentNames.define(new AgentSymbol(agentName, agentDf, getSourceInfo(ctx.ID())))) {
			// Record problem to inform user that we do not overwrite first
			// agent definition
			// and simply ignore second agent definition with same name
			reportError(MASError.AGENT_DUPLICATE_NAME, ctx.ID(), agentName);
		}

		// TODO: VALIDATE: Check whether options of module are compatible with
		// init, event,
		// and main role:
		// init: must have focus=NONE, exit=ALWAYS, order=LINEARALL
		// event: must have focus=NONE, exit=ALWAYS, order=LINEARALL
		// main: must have focus=NONE

		// Add agent definition to MAS program
		getProgram().addAgentDefinition(agentDf);

		return null; // Java says must return something even when Void
	}

	/**
	 * Processes the use clauses in an agent definition section.
	 *
	 * @param agentName The name of the agent that is defined.
	 * @param clauses   List of use clauses in the definition section.
	 */
	public AgentDefinition processAgentDefinition(String agentName, List<UseClauseContext> clauses, SourceInfo info) {
		AgentDefinition agentDf = new AgentDefinition(agentName, this.registry, info);
		for (UseClauseContext useClause : clauses) {
			UseClause clause = visitUseClause(useClause);
			// Check if something bad went wrong while parsing
			if (clause == null) {
				continue;
			}

			List<URI> files = clause.resolveReference();
			if (files.isEmpty()) {
				reportError(MASError.REFERENCE_COULDNOT_FIND, useClause, clause.getReference());
			}

			// Add use clause to agent definition
			// VALIDATE: Check for duplicate init, event, or main modules
			if (!agentDf.addUseClause(clause)) {
				reportError(MASError.USECASE_DUPLICATE, useClause, clause.getReference());
			}
		}
		return agentDf;
	}

	@Override
	public UseClause visitUseClause(UseClauseContext ctx) {
		String typeString = visitUseCase(ctx.useCase());
		String ref = visitRef(ctx.ref());

		UseCase type = UseCase.getUseCase(typeString);
		// Something bad went wrong while parsing
		if (type == null) {
			reportError(MASError.USECASE_INVALID, ctx, typeString, ref);
			return null;
		} else {
			return new UseClause(ref, type, getPathRelativeToSourceFile(""), getSourceInfo(ctx));
		}
	}

	@Override
	public String visitUseCase(UseCaseContext ctx) {
		if (ctx != null) {
			// Remove 'module' part if present
			return StringUtils.remove(ctx.getText(), "module");
		} else {
			return "";
		}
	}

	// -------------------------------------------------------------
	// LAUNCH POLICY section
	// -------------------------------------------------------------

	@Override
	public Object visitPolicy(PolicyContext ctx) {
		for (LaunchRuleContext rule : ctx.launchRule()) {
			visitLaunchRule(rule);
		}

		List<ValidatorWarning> warnings = new LinkedList<>();
		List<LaunchRule> rules = getProgram().getLaunchRules();
		SourceInfo info = getSourceInfo(ctx);

		warnings.addAll(MASValidatorTools.checkAgentsUsed(rules, info, getProgram().getAgentNames()));
		if (getProgram().hasEnvironment()) {
			warnings.addAll(MASValidatorTools.checkEnvironmentUsed(rules, info));
		}
		warnings.addAll(MASValidatorTools.checkLaunchRulesReachable(rules, info));
		// Missing agent definitions handled while processing launch
		// instructions

		warnings.stream().forEach(warning -> reportWarning(warning));
		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitLaunchRule(LaunchRuleContext ctx) {
		// Process conditional part of launch rule.
		Entity entity = null;
		if (ctx.entity() != null) {
			entity = visitEntity(ctx.entity());
		}

		// Process instructions of launch rule.
		List<LaunchInstruction> instructions = new ArrayList<>(ctx.instruction().size());
		for (InstructionContext instruction : ctx.instruction()) {
			instructions.add(visitInstruction(instruction));
		}
		LaunchRule rule = new LaunchRule(entity, instructions);

		// VALIDATE: A launch rule cannot have an empty list of launch
		// instructions
		if (!instructions.isEmpty()) {
			getProgram().addLaunchRule(rule);
		}

		// VALIDATE: An unconditional launch rule should not use wild cards
		for (LaunchInstruction launch : rule.getInstructions()) {
			if (!rule.isConditional() && launch.getGivenName(STAR, 0).equals(STAR)) {
				reportError(MASError.LAUNCH_INVALID_WILDCARD, ctx,
						ctx.getText().substring(0, ctx.getText().length() - 1));
			}
		}

		if (rule.isConditional() && !getProgram().hasEnvironment()) {
			reportWarning(MASWarning.LAUNCH_CONDITIONAL_RULE, ctx);
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public Entity visitEntity(EntityContext ctx) {
		Entity entity = new Entity();

		if (ctx.entityname() != null) {
			entity.setName(visitEntityname(ctx.entityname()));
		}
		if (ctx.entitytype() != null) {
			entity.setType(visitEntitytype(ctx.entitytype()));
		}

		return entity;
	}

	@Override
	public String visitEntitytype(EntitytypeContext ctx) {
		return (ctx == null || ctx.ID() == null) ? "" : ctx.ID().getText();
	}

	@Override
	public String visitEntityname(EntitynameContext ctx) {
		return (ctx == null || ctx.ID() == null) ? "" : ctx.ID().getText();
	}

	@Override
	public LaunchInstruction visitInstruction(InstructionContext ctx) {
		String agentName = (ctx.ID() == null) ? "" : ctx.ID().getText();
		LaunchInstruction instruction = new LaunchInstruction(agentName);

		// Resolve agent reference.
		AgentSymbol symbol = (AgentSymbol) this.agentNames.resolve(agentName);
		// VALIDATE: Check if definition is available for name
		if (symbol == null) {
			reportError(MASError.LAUNCH_MISSING_AGENTDF, ctx, agentName);
		} else {
			instruction.addAgentDf(symbol.getAgentDf());
		}

		// Process constraints.
		int namec = 0, nrc = 0, maxc = 0, alphac = 0, gammac = 0, epsilonc = 0, decayc = 0;
		for (ConstraintContext constraintctx : ctx.constraint()) {
			Map.Entry<String, Object> constraint = visitConstraint(constraintctx);
			instruction.addConstraint(constraint);
			switch (constraint.getKey()) {
			case "name":
				++namec;
				break;
			case "nr":
				++nrc;
				break;
			case "max":
				++maxc;
				break;
			case "alpha":
				++alphac;
				break;
			case "gamma":
				++gammac;
				break;
			case "epsilon":
				++epsilonc;
				break;
			case "decay":
				++decayc;
				break;
			}
		}
		// VALIDATE: Check for duplicate constraints
		if (namec > 1 || nrc > 1 || maxc > 1 || alphac > 1 || gammac > 1 || epsilonc > 1 || decayc > 1) {
			reportWarning(MASWarning.CONSTRAINT_DUPLICATE, ctx, ctx.getText().substring(0, ctx.getText().length() - 1));
		}

		// VALIDATE: Check whether given names in launch rules might result in
		// naming conflicts.
		String name = instruction.getGivenName(null, 0);
		if (name != null && name != agentName) {
			if (this.agentNames.resolve(name) != null) {
				// Record potential naming conflict or redundant use of name
				reportError(MASError.AGENT_DUPLICATE_GIVENNAME, ctx.ID(), name);
			}
		}

		return instruction;
	}

	@Override
	public Map.Entry<String, Object> visitConstraint(ConstraintContext ctx) {
		String key = "";
		Object value = null;

		if (ctx.nameconstraint() != null) {
			key = "name";
			value = visitNameconstraint(ctx.nameconstraint());
		} else if (ctx.nrconstraint() != null) {
			key = "nr";
			value = visitNrconstraint(ctx.nrconstraint());
		} else if (ctx.maxconstraint() != null) {
			key = "max";
			value = visitMaxconstraint(ctx.maxconstraint());
		} else if (ctx.alpha() != null) {
			key = "alpha";
			value = visitAlpha(ctx.alpha());
		} else if (ctx.gamma() != null) {
			key = "gamma";
			value = visitGamma(ctx.gamma());
		} else if (ctx.epsilon() != null) {
			key = "epsilon";
			value = visitEpsilon(ctx.epsilon());
		} else if (ctx.decay() != null) {
			key = "decay";
			value = visitDecay(ctx.decay());
		}

		return new AbstractMap.SimpleEntry<>(key, value);
	}

	@Override
	public String visitNameconstraint(NameconstraintContext ctx) {
		if (ctx.STAR() != null) {
			return STAR;
		} else if (ctx.ID() != null) {
			return ctx.ID().getText();
		} else {
			return "";
		}
	}

	@Override
	public Integer visitNrconstraint(NrconstraintContext ctx) {
		return (ctx.INT() == null) ? 0 : Integer.parseInt(ctx.INT().getText().trim());
	}

	@Override
	public Integer visitMaxconstraint(MaxconstraintContext ctx) {
		return (ctx.INT() == null) ? 0 : Integer.parseInt(ctx.INT().getText().trim());
	}

	@Override
	public Double visitAlpha(AlphaContext ctx) {
		return (ctx.FLOAT() == null) ? 0 : Double.parseDouble(ctx.FLOAT().getText().trim());
	}

	@Override
	public Double visitGamma(GammaContext ctx) {
		return (ctx.FLOAT() == null) ? 0 : Double.parseDouble(ctx.FLOAT().getText().trim());
	}

	@Override
	public Double visitEpsilon(EpsilonContext ctx) {
		return (ctx.FLOAT() == null) ? 0 : Double.parseDouble(ctx.FLOAT().getText().trim());
	}

	@Override
	public Double visitDecay(DecayContext ctx) {
		return (ctx.FLOAT() == null) ? 0 : Double.parseDouble(ctx.FLOAT().getText().trim());
	}

	@Override
	public String visitString(StringContext ctx) {
		String str = "";
		if (ctx.StringLiteral() != null) {
			for (TerminalNode literal : ctx.StringLiteral()) {
				String[] parts = literal.getText().split("(?<!\\\\)\"", 0);
				if (parts.length > 1) {
					str += parts[1].replace("\\\"", "\"");
				}
			}
		}
		if (ctx.SingleQuotedStringLiteral() != null) {
			for (TerminalNode literal : ctx.SingleQuotedStringLiteral()) {
				String[] parts = literal.getText().split("(?<!\\\\)'", 0);
				if (parts.length > 1) {
					str += parts[1].replace("\\'", "'");
				}
			}
		}
		return str;
	}

	/**
	 * Validation of MAS program does not require second pass to resolve any
	 * references.
	 *
	 * TODO: second pass should check for unused declarations.
	 */
	@Override
	protected ValidatorSecondPass createSecondPass() {
		return null;
	}

	public Analysis process() {
		return process(null);
	}

	/**
	 * Call this method after the validator has been run to process the references
	 * in the agent definitions and, recursively, the references to other modules
	 * and KR files.
	 * <p>
	 * This method adds all (syntax) errors and warnings detected while processing
	 * the references to the lists of (syntax) errors and warnings collected while
	 * validating the MAS file. This means, for example, that the
	 * {@link Validator#getErrors()} method will not only return the MAS errors but
	 * also the validation errors reported for references files.
	 * </p>
	 *
	 * @param override {@link Entry} with override settings
	 *
	 * @throws ParserException
	 */
	public Analysis process(Map.Entry<File, String> override) {
		Analysis analysis = new Analysis(this.registry, getProgram(), override);
		analysis.run();
		if (this.registry.hasAnyError()) {
			return analysis;
		}

		// Fully parse all agents that we can find in the MAS,
		// i.e. the modules that are referenced, and all use clauses that
		// are referenced in those modules in turn (if any).

		// Report
		for (Module module : analysis.getUnusedModuleDefinitions()) {
			reportWarning(MASWarning.MODULE_UNUSED, module.getDefinition(), module.getSignature());
		}
		for (UserSpecAction action : analysis.getUnusedActionDefinitions()) {
			reportWarning(MASWarning.ACTION_UNUSED, action.getSourceInfo(), action.getSignature());
		}
		for (DatabaseFormula belief : analysis.getBeliefsUnused()) {
			reportWarning(MASWarning.PREDICATE_UNUSED, belief.getSourceInfo(), belief.getSignature());
		}

		// Final checks
		try {
			Map<String, SourceInfo> knowledgeDefined = getDefinedSignatures(analysis.getAllKnowledge());
			Map<String, SourceInfo> knowledgeDeclared = getDeclaredSignatures(analysis.getAllKnowledge());
			checkNoPredicatesBeliefAndKnowledge(knowledgeDefined, knowledgeDeclared, analysis.getAllBeliefs());
			checkNoBeliefsInsertedAsKnowledge(knowledgeDefined, knowledgeDeclared, analysis.getBeliefUpdates());
		} catch (ParserException e) {
			reportParsingException(e);
		}
		for (DatabaseFormula dbf : analysis.getAllKnowledge()) {
			try {
				CognitiveKR ckr = getCognitiveKR();
				List<Var> vars = ckr.getAllVariables(dbf);
				Set<Var> unique = new LinkedHashSet<>(vars);
				for (Var var : unique) {
					int occurences = Collections.frequency(vars, var);
					if (occurences < 2) {
						reportWarning(MASWarning.VARIABLE_UNUSED, var.getSourceInfo(), var.toString());
					}
				}
			} catch (ParserException e) {
				reportParsingException(e);
			}
		}
		return analysis;
	}

	/**
	 * @param expressions
	 * @return map with defined signatures as key and {@link SourceInfo} as value.
	 * @throws ParserException
	 */
	protected Map<String, SourceInfo> getDefinedSignatures(Collection<DatabaseFormula> formulas)
			throws ParserException {
		CognitiveKR ckr = getCognitiveKR();
		Map<String, SourceInfo> signatures = new LinkedHashMap<>(formulas.size());
		for (DatabaseFormula formula : formulas) {
			for (String signature : ckr.getDefinedSignatures(formula)) {
				signatures.put(signature, formula.getSourceInfo());
			}
		}
		return signatures;
	}

	/**
	 * @param formulas
	 * @return map with all explicitly declared signatures as key and all
	 *         {@link SourceInfo} as value.
	 * @throws ParserException
	 */
	protected Map<String, SourceInfo> getDeclaredSignatures(Collection<DatabaseFormula> formulas)
			throws ParserException {
		CognitiveKR ckr = getCognitiveKR();
		Map<String, SourceInfo> signatures = new LinkedHashMap<>(formulas.size());
		for (DatabaseFormula formula : formulas) {
			for (String signature : ckr.getDeclaredSignatures(formula)) {
				signatures.put(signature, formula.getSourceInfo());
			}
		}
		return signatures;
	}

	/**
	 * Check that no beliefs are inserted/deleted that are already knowledge
	 *
	 * @param knowledgeDefined
	 * @param knowledgeDeclared
	 * @param beliefUpdates     all belief updates, both from inserts, deletes, and
	 *                          actionspecs.
	 * @throws ParserException
	 */
	private void checkNoBeliefsInsertedAsKnowledge(Map<String, SourceInfo> knowledgeDefined,
			Map<String, SourceInfo> knowledgeDeclared, Set<Update> beliefUpdates) {
		for (Update update : beliefUpdates) {
			checkNoPredicatesBeliefAndKnowledge(knowledgeDefined, knowledgeDeclared, update.getAddList());
			checkNoPredicatesBeliefAndKnowledge(knowledgeDefined, knowledgeDeclared, update.getDeleteList());
		}
	}

	/**
	 * Check there is no predicate defined both in knowledge and beliefs.
	 *
	 * @param knowledgeDefined
	 * @param knowledgeDeclared
	 * @param allBeliefs
	 * @throws ParserException
	 */
	protected void checkNoPredicatesBeliefAndKnowledge(Map<String, SourceInfo> knowledgeDefined,
			Map<String, SourceInfo> knowledgeDeclared, Collection<DatabaseFormula> allBeliefs) {
		try {
			Map<String, SourceInfo> signatures = getDefinedSignatures(allBeliefs);
			signatures.putAll(getDeclaredSignatures(allBeliefs));
			signatures.putAll(knowledgeDeclared);
			for (String signature : signatures.keySet()) {
				if (knowledgeDefined.containsKey(signature)) {
					reportError(MASError.PREDICATE_ALREADY_KNOWLEDGE, signatures.get(signature), signature,
							knowledgeDefined.get(signature).toString());
				}
			}
		} catch (ParserException e) {
			reportError(MASError.NO_SIGNATURE, e.getSourceInfo(), e.getMessage());
		}
	}
}
