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
package languageTools.analyzer.actionspec;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.FilenameUtils;

import cognitiveKr.CognitiveKR;
import krTools.exceptions.ParserException;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.Validator;
import languageTools.analyzer.ValidatorSecondPass;
import languageTools.errors.actionspec.ActionSpecError;
import languageTools.errors.actionspec.ActionSpecErrorStrategy;
import languageTools.parser.ACT2GParser;
import languageTools.parser.ACT2GParser.ActionspecContext;
import languageTools.parser.ACT2GParser.AsclauseContext;
import languageTools.parser.ACT2GParser.PostContext;
import languageTools.parser.ACT2GParser.PostaddContext;
import languageTools.parser.ACT2GParser.PostdelContext;
import languageTools.parser.ACT2GParser.PreContext;
import languageTools.parser.ACT2GParser.RefContext;
import languageTools.parser.ACT2GParser.SpecificationsContext;
import languageTools.parser.ACT2GParser.StringContext;
import languageTools.parser.ACT2GParser.UseclauseContext;
import languageTools.parser.ACT2GParserVisitor;
import languageTools.parser.GOALLexer;
import languageTools.parser.InputStreamPosition;
import languageTools.program.actionspec.ActionPostCondition;
import languageTools.program.actionspec.ActionPreCondition;
import languageTools.program.actionspec.ActionSpecProgram;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.mas.UseClause;
import languageTools.program.mas.UseClause.UseCase;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.agent.ActionSymbol;

/**
 * Validates an action specification file and constructs an action specification
 * program. FIXME: Explain what is validated. How to use. What happens if
 * filename does not validate.
 */
public class ActionSpecValidator extends Validator<GOALLexer, ACT2GParser, ActionSpecErrorStrategy, ActionSpecProgram>
		implements ACT2GParserVisitor<Object> {
	private ACT2GParser parser;
	private static ActionSpecErrorStrategy strategy = null;
	/**
	 * Action labels cannot have the same signature because a call cannot be
	 * resolved in that case.
	 */
	private final SymbolTable actionSymbols = new SymbolTable();

	public ActionSpecValidator(String filename, FileRegistry registry) {
		super(filename, registry);
	}

	@Override
	protected ParseTree startParser() {
		return this.parser.specifications();
	}

	@Override
	protected ActionSpecErrorStrategy getTheErrorStrategy() {
		if (strategy == null) {
			strategy = new ActionSpecErrorStrategy();
		}
		return strategy;
	}

	/**
	 * Validation of agent program resolves references to action, macro, and module
	 * symbols, and checks whether all predicates used have been defined.
	 */
	@Override
	protected ValidatorSecondPass createSecondPass() {
		return new ActionSpecValidatorSecondPass(this);
	}

	@Override
	protected GOALLexer getNewLexer(CharStream stream) {
		return new GOALLexer(stream);
	}

	@Override
	protected ACT2GParser getNewParser(TokenStream stream) {
		this.parser = new ACT2GParser(stream);
		return this.parser;
	}

	@Override
	protected ActionSpecProgram getNewProgram(File file) throws IOException {
		return new ActionSpecProgram(this.registry, new InputStreamPosition(0, 0, 0, 0, file.getCanonicalPath()));
	}

	@Override
	public ActionSpecProgram getProgram() {
		return (ActionSpecProgram) super.getProgram();
	}

	/**
	 * Calls {@link ParseTree#accept} on the specified tree.
	 */
	@Override
	public Void visit(ParseTree tree) {
		tree.accept(this);
		return null; // Java says must return something even when Void
	}

	// -------------------------------------------------------------
	// Action Specifications
	// -------------------------------------------------------------

	@Override
	public Object visitSpecifications(SpecificationsContext ctx) {
		// Process use clauses.
		for (UseclauseContext useclausectx : ctx.useclause()) {
			visitUseclause(useclausectx);
		}

		// VALIDATE: Check which (single) KR language is used.
		// Based on KR file references only.
		// TODO: duplicates
		if (getProgram().resolveKRInterface()) {
			// Process action specifications.
			for (ActionspecContext actionspecctx : ctx.actionspec()) {
				visitActionspec(actionspecctx);
			}
		} else {
			if (getProgram().getReferencedKRFiles().isEmpty()) {
				reportError(ActionSpecError.KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, ctx);
			} else {
				reportError(ActionSpecError.KR_COULDNOT_RESOLVE_DIFFERENT_KRS_USED, ctx);
			}
		}

		return null;
	}

	@Override
	public Void visitUseclause(UseclauseContext ctx) {
		for (RefContext ref : ctx.ref()) {
			// Get reference.
			String reference = visitRef(ref);

			// Create use clause and resolve reference.
			UseClause useClause = new UseClause(reference, UseCase.KNOWLEDGE, getPathRelativeToSourceFile(""),
					getSourceInfo(ref));
			List<URI> files = useClause.resolveReference();
			if (files.isEmpty()) {
				reportError(ActionSpecError.REFERENCE_COULDNOT_FIND, ref, reference);
			}

			// Add use clause to module.
			if (!getProgram().addUseClause(useClause)) {
				reportError(ActionSpecError.REFERENCE_DUPLICATE, ref, reference);
			}
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
	public Void visitActionspec(ActionspecContext ctx) {

		// Get action name.
		String name = "";
		if (ctx.ID() != null) {
			name = ctx.ID().getText();
		}

		// Get action parameters.
		List<Term> parameters = new LinkedList<>();
		if (ctx.PARLIST() != null) {
			for (Var var : visitVARLIST(ctx.PARLIST(), ctx)) {
				if (parameters.contains(var)) {
					reportError(ActionSpecError.DUPLICATE_PARAMETER, ctx, var.toString());
				}
				parameters.add(var);
			}
		}

		// Process as clause.
		boolean external = true;
		if (ctx.asclause() != null) {
			external = visitAsclause(ctx.asclause());
		}

		// Get pre- and post-condition
		ActionPreCondition precondition = (ctx.pre() == null) ? null : visitPre(ctx.pre());
		ActionPostCondition positivePostcondition = (ctx.post() == null) ? null : visitPost(ctx.post());
		ActionPostCondition negativePostcondition = null;
		if (positivePostcondition == null) {
			positivePostcondition = (ctx.postadd() == null) ? null : visitPostadd(ctx.postadd());
			negativePostcondition = (ctx.postdel() == null) ? null : visitPostdel(ctx.postdel());
		}

		// Create action specification and store it.
		UserSpecAction action = new UserSpecAction(name, parameters, external, precondition, positivePostcondition,
				negativePostcondition, getSourceInfo(ctx));
		getProgram().addActionSpecification(action);

		// VALIDATE: Check whether action has already been defined.
		if (!this.actionSymbols.define(new ActionSymbol(action.getSignature(), action, action.getSourceInfo()))) {
			reportError(ActionSpecError.ACTION_LABEL_ALREADY_DEFINED, ctx, action.getSignature());
		}

		// VALIDATE: Check whether variables in post-condition are bound.
		Set<Var> postvars = new LinkedHashSet<>();
		if (positivePostcondition != null && positivePostcondition.getPostCondition() != null) {
			postvars.addAll(positivePostcondition.getPostCondition().getFreeVar());
		}
		if (negativePostcondition != null && negativePostcondition.getPostCondition() != null) {
			postvars.addAll(negativePostcondition.getPostCondition().getFreeVar());
		}
		if (precondition != null && precondition.getPreCondition() != null) {
			postvars.removeAll(precondition.getPreCondition().getFreeVar());
		}
		postvars.removeAll(parameters);
		if (!postvars.isEmpty()) {
			reportError(ActionSpecError.POSTCONDITION_UNBOUND_VARIABLE, ctx, prettyPrintSet(postvars));
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public Boolean visitAsclause(AsclauseContext ctx) {
		if (ctx.INTERNAL() != null) {
			return false;
		} else if (ctx.EXTERNAL() != null) {
			return true;
		} else {
			return true;
		}
	}

	@Override
	public ActionPreCondition visitPre(PreContext ctx) {
		String krFragment = (ctx.KR_BLOCK() == null) ? "" : removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		Query pre = null;
		Set<String> signatures = null;
		try {
			CognitiveKR ckr = getCognitiveKR();
			SourceInfo info = getSourceInfo(ctx.KR_BLOCK());
			info = (info == null) ? null
					: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
							info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
			pre = ckr.visitBeliefQuery(krFragment, info);
			if (pre != null) {
				signatures = ckr.getUsedSignatures(pre);
			}
			reportEmbeddedLanguageErrors(ckr);
		} catch (ParserException e) {
			reportParsingException(e);
		}
		return (pre == null) ? null : new ActionPreCondition(pre, signatures, getSourceInfo(ctx));
	}

	@Override
	public ActionPostCondition visitPost(PostContext ctx) {
		String krFragment = (ctx.KR_BLOCK() == null) ? "" : removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		Update post = null;
		try {
			CognitiveKR ckr = getCognitiveKR();
			SourceInfo info = getSourceInfo(ctx.KR_BLOCK());
			info = (info == null) ? null
					: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
							info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
			post = ckr.visitBeliefInsert(krFragment, info);
			reportEmbeddedLanguageErrors(ckr);
		} catch (ParserException e) {
			reportParsingException(e);
		}
		return (post == null) ? null : new ActionPostCondition(post, false, getSourceInfo(ctx));
	}

	@Override
	public ActionPostCondition visitPostadd(PostaddContext ctx) {
		String krFragment = (ctx.KR_BLOCK() == null) ? "" : removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		Update post = null;
		try {
			CognitiveKR ckr = getCognitiveKR();
			SourceInfo info = getSourceInfo(ctx.KR_BLOCK());
			info = (info == null) ? null
					: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
							info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
			post = ckr.visitBeliefInsert(krFragment, info);
			reportEmbeddedLanguageErrors(ckr);
		} catch (ParserException e) {
			reportParsingException(e);
		}
		return (post == null) ? null : new ActionPostCondition(post, false, getSourceInfo(ctx));
	}

	@Override
	public ActionPostCondition visitPostdel(PostdelContext ctx) {
		String krFragment = (ctx.KR_BLOCK() == null) ? "" : removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		Update post = null;
		try {
			CognitiveKR ckr = getCognitiveKR();
			SourceInfo info = getSourceInfo(ctx.KR_BLOCK());
			info = (info == null) ? null
					: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
							info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
			post = ckr.visitBeliefDelete(krFragment, info);
			reportEmbeddedLanguageErrors(ckr);
		} catch (ParserException e) {
			reportParsingException(e);
		}
		return (post == null) ? null : new ActionPostCondition(post, true, getSourceInfo(ctx));
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
}