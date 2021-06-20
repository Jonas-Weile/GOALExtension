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

package languageTools.analyzer.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.FilenameUtils;

import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.Validator;
import languageTools.analyzer.ValidatorSecondPass;
import languageTools.analyzer.mas.MASValidator;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.module.ModuleError;
import languageTools.errors.module.ModuleErrorStrategy;
import languageTools.errors.test.TestError;
import languageTools.errors.test.TestErrorStrategy;
import languageTools.errors.test.TestWarning;
import languageTools.parser.GOALLexer;
import languageTools.parser.InputStreamPosition;
import languageTools.parser.MOD2GParser;
import languageTools.parser.TEST2GParser;
import languageTools.parser.TEST2GParser.ActionContext;
import languageTools.parser.TEST2GParser.ActioncomboContext;
import languageTools.parser.TEST2GParser.AgenttestContext;
import languageTools.parser.TEST2GParser.DoneTestContext;
import languageTools.parser.TEST2GParser.ExitoptionContext;
import languageTools.parser.TEST2GParser.FocusoptionContext;
import languageTools.parser.TEST2GParser.GeneralactionContext;
import languageTools.parser.TEST2GParser.InContext;
import languageTools.parser.TEST2GParser.LearnclauseContext;
import languageTools.parser.TEST2GParser.MacroContext;
import languageTools.parser.TEST2GParser.MentalatomContext;
import languageTools.parser.TEST2GParser.MentalliteralContext;
import languageTools.parser.TEST2GParser.MentalopContext;
import languageTools.parser.TEST2GParser.ModuleContext;
import languageTools.parser.TEST2GParser.ModulerefContext;
import languageTools.parser.TEST2GParser.ModuletestContext;
import languageTools.parser.TEST2GParser.MscContext;
import languageTools.parser.TEST2GParser.OptionContext;
import languageTools.parser.TEST2GParser.OrderoptionContext;
import languageTools.parser.TEST2GParser.PostContext;
import languageTools.parser.TEST2GParser.PreContext;
import languageTools.parser.TEST2GParser.ReacttestContext;
import languageTools.parser.TEST2GParser.RefContext;
import languageTools.parser.TEST2GParser.RulesContext;
import languageTools.parser.TEST2GParser.RunconditionContext;
import languageTools.parser.TEST2GParser.SelectorContext;
import languageTools.parser.TEST2GParser.SelectoractionContext;
import languageTools.parser.TEST2GParser.SelectoropContext;
import languageTools.parser.TEST2GParser.StringContext;
import languageTools.parser.TEST2GParser.TemporaltestContext;
import languageTools.parser.TEST2GParser.TestContext;
import languageTools.parser.TEST2GParser.TestactionContext;
import languageTools.parser.TEST2GParser.TestconditionContext;
import languageTools.parser.TEST2GParser.TestmscContext;
import languageTools.parser.TEST2GParser.TimeoutoptionContext;
import languageTools.parser.TEST2GParser.UsecaseContext;
import languageTools.parser.TEST2GParser.UseclauseContext;
import languageTools.parser.TEST2GParserVisitor;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.mas.MASProgram;
import languageTools.program.mas.UseClause;
import languageTools.program.mas.UseClause.UseCase;
import languageTools.program.test.AgentTest;
import languageTools.program.test.ModuleTest;
import languageTools.program.test.TestAction;
import languageTools.program.test.TestMentalStateCondition;
import languageTools.program.test.TestProgram;
import languageTools.program.test.testcondition.Always;
import languageTools.program.test.testcondition.DoneCondition;
import languageTools.program.test.testcondition.Eventually;
import languageTools.program.test.testcondition.Never;
import languageTools.program.test.testcondition.TestCondition;
import languageTools.program.test.testcondition.Until;

/**
 * Validates a test file and constructs a test program.
 */
public class TestValidator extends Validator<GOALLexer, TEST2GParser, TestErrorStrategy, TestProgram>
		implements TEST2GParserVisitor<Object> {
	private TEST2GParser parser;
	private MASProgram override;
	private static TestErrorStrategy strategy = null;

	/**
	 * Creates the test validator.
	 *
	 * @param filename Name of file which contains test.
	 */
	public TestValidator(String filename, FileRegistry registry) {
		super(filename, registry);
	}

	@Override
	protected GOALLexer getNewLexer(CharStream stream) {
		return new GOALLexer(stream);
	}

	@Override
	protected TEST2GParser getNewParser(TokenStream stream) {
		this.parser = new TEST2GParser(stream);
		return this.parser;
	}

	@Override
	protected ParseTree startParser() {
		return this.parser.test();
	}

	@Override
	protected TestErrorStrategy getTheErrorStrategy() {
		if (strategy == null) {
			strategy = new TestErrorStrategy();
		}
		return strategy;
	}

	@Override
	protected TestProgram getNewProgram(File file) throws IOException {
		return new TestProgram(this.registry, new InputStreamPosition(0, 0, 0, 0, file.getCanonicalPath()));
	}

	@Override
	public TestProgram getProgram() {
		return (TestProgram) super.getProgram();
	}

	@Override
	protected ValidatorSecondPass createSecondPass() {
		return new TestValidatorSecondPass(this);
	}

	public void overrideMAS(MASProgram mas) {
		this.override = mas;
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
	public Void visitTest(TestContext ctx) {
		if (ctx == null) {
			return null;
		}
		try {
			// Process use clauses (mas2g).
			for (UseclauseContext useclausectx : ctx.useclause()) {
				visitUseclause(useclausectx);
			}
			File masPath = null;
			for (UseClause clause : getProgram().getUseClauses()) {
				List<File> resolved = clause.getResolvedReference();
				if (clause.getUseCase() == UseCase.MAS && resolved.size() == 1) {
					masPath = resolved.get(0);
					break;
				}
			}
			if (masPath == null) {
				reportError(TestError.MAS_MISSING, ctx);
				return null;
			}
			// Process option (timeout).
			if (ctx.option() != null) {
				visitOption(ctx.option());
			}

			if (this.override == null) {
				MASValidator mas2g = new MASValidator(masPath.getCanonicalPath(), this.registry);
				mas2g.validate();
				mas2g.process();
				if (mas2g.getProgram() == null || mas2g.getProgram().getKRInterface() == null) {
					return null; // best effort...
				}
				getProgram().setMAS(mas2g.getProgram());
			} else {
				getProgram().setMAS(this.override);
				for (File source : this.override.getRegistry().getSourceFiles()) {
					this.registry.register(source, this.override.getRegistry().getProgram(source));
				}
			}

			for (ModuletestContext testCtx : ctx.moduletest()) {
				List<ModuleTest> moduleTests = visitModuletest(testCtx);
				for (ModuleTest moduleTest : moduleTests) {
					if (!getProgram().addModuleTest(moduleTest)) {
						reportWarning(TestWarning.DUPLICATE_MODULE_TEST, testCtx, moduleTest.getModuleSignature());
					}
				}
			}

			for (AgenttestContext testCtx : ctx.agenttest()) {
				List<AgentTest> agentTests = visitAgenttest(testCtx);
				for (AgentTest agentTest : agentTests) {
					if (!getProgram().getMAS().getAgentNames().contains(agentTest.getAgentName())) {
						reportError(TestError.TEST_INVALID_AGENT, testCtx, agentTest.getAgentName());
					} else if (!getProgram().addAgentTest(agentTest)) {
						reportWarning(TestWarning.DUPLICATE_AGENT_TEST, testCtx, agentTest.getAgentName());
					}
				}
			}

			return null;
		} catch (Exception any) {
			// Convert stack trace to string
			StringWriter sw = new StringWriter();
			any.printStackTrace(new PrintWriter(sw));
			reportError(SyntaxError.FATAL, null, any.getMessage() + "\n" + sw.toString());
			return null;
		}
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
	public Void visitUseclause(UseclauseContext ctx) {
		// Get use case.
		UseCase useCase = (ctx.usecase() == null) ? null : UseCase.getUseCase(visitUsecase(ctx.usecase()));

		for (RefContext ref : ctx.ref()) {
			// Get reference.
			String reference = visitRef(ref);

			// Create use clause and resolve reference.
			UseClause useClause = new UseClause(reference, useCase, getPathRelativeToSourceFile(""),
					getSourceInfo(ref));
			List<URI> files = useClause.resolveReference();
			if (files.isEmpty()) {
				reportError(ModuleError.REFERENCE_COULDNOT_FIND, ref, reference);
			}

			// Add use clause to module.
			if (!getProgram().addUseClause(useClause)) {
				reportError(ModuleError.REFERENCE_DUPLICATE, ref, reference);
			}
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public String visitUsecase(UsecaseContext ctx) {
		if (ctx != null) {
			return ctx.getText();
		} else {
			return "";
		}
	}

	@Override
	public Void visitOption(OptionContext ctx) {
		if (ctx.timeoutoption() != null) {
			visitTimeoutoption(ctx.timeoutoption());
		}
		return null;
	}

	@Override
	public Void visitTimeoutoption(TimeoutoptionContext ctx) {
		String number = (ctx.value == null) ? "" : ctx.value.getText();
		try {
			getProgram().setTimeout(Long.parseLong(number));
		} catch (NumberFormatException e) {
			reportError(TestError.TIMEOUT_INVALID, ctx, number);
		}
		return null;
	}

	@Override
	public List<ModuleTest> visitModuletest(ModuletestContext ctx) {
		List<ModuleTest> tests = new LinkedList<>();
		if (ctx.moduleref() == null || ctx.moduleref().isEmpty()) {
			reportError(TestError.TEST_MISSING_MODULE, ctx);
			return tests;
		}

		for (final ModulerefContext module : ctx.moduleref()) {
			ModuleTest test = visitModuleref(module);
			if (ctx.pre() != null) {
				test.setPre(visitPre(ctx.pre()));
			}
			if (ctx.in() != null) {
				test.setIn(visitIn(ctx.in()));
			}
			if (ctx.post() != null) {
				test.setPost(visitPost(ctx.post()));
			}
			tests.add(test);
		}

		return tests;
	}

	@Override
	public ModuleTest visitModuleref(ModulerefContext ctx) {
		List<Term> params = new ArrayList<>(0);
		if (ctx.PARLIST() != null) {
			params = visitPARLIST(ctx.PARLIST(), getSourceInfo(ctx.PARLIST()));
		}
		return new ModuleTest(ctx.ID().getText(), params, getSourceInfo(ctx.ID()));
	}

	@Override
	public TestMentalStateCondition visitPre(PreContext ctx) {
		String pre = (ctx.KR_BLOCK() == null) ? "" : removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		if (pre.trim().isEmpty()) {
			return null;
		} else {
			SourceInfo info = getSourceInfo(ctx.KR_BLOCK());
			info = (info == null) ? null
					: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
							info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
			TEST2GParser parser = prepareTestParser(pre, info);
			TestmscContext mscContext = parser.testmsc();

			TestValidator sub = getTestSub("precondition", info);
			return sub.visitTestmsc(mscContext);
		}
	}

	@Override
	public TestMentalStateCondition visitPost(PostContext ctx) {
		String post = (ctx.KR_BLOCK() == null) ? "" : removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		if (post.trim().isEmpty()) {
			return null;
		} else {
			SourceInfo info = getSourceInfo(ctx.KR_BLOCK());
			info = (info == null) ? null
					: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
							info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
			TEST2GParser parser = prepareTestParser(post, info);
			TestmscContext mscContext = parser.testmsc();

			TestValidator sub = getTestSub("postcondition", info);
			return sub.visitTestmsc(mscContext);
		}
	}

	@Override
	public List<TestCondition> visitIn(InContext ctx) {
		List<TestCondition> conditions = new ArrayList<>(ctx.testcondition().size());
		for (TestconditionContext conditionCtx : ctx.testcondition()) {
			TestCondition condition = visitTestcondition(conditionCtx);
			if (condition != null) {
				conditions.add(condition);
			}
		}
		return conditions;
	}

	@Override
	public List<AgentTest> visitAgenttest(AgenttestContext ctx) {
		List<AgentTest> tests = new LinkedList<>();
		if (ctx.ID() == null || ctx.ID().isEmpty()) {
			reportError(TestError.TEST_MISSING_AGENT, ctx);
			return tests;
		}

		for (final TerminalNode ID : ctx.ID()) {
			String agentName = ID.getText();
			if (ctx.testaction() == null || ctx.testaction().isEmpty()) {
				tests.add(new AgentTest(agentName, getSourceInfo(ctx)));
			} else {
				List<TestAction> actions = new ArrayList<>(ctx.testaction().size());
				for (TestactionContext actionCtx : ctx.testaction()) {
					TestAction action = visitTestaction(actionCtx);
					if (action != null) {
						actions.add(action);
					}
				}
				tests.add(new AgentTest(agentName, actions, getSourceInfo(ctx)));
			}
		}

		return tests;
	}

	@Override
	public TestAction visitTestaction(TestactionContext ctx) {
		if (ctx.actioncombo() == null) {
			reportError(TestError.TEST_MISSING_ACTION, ctx);
			return null;
		} else {
			ActionCombo combo = visitActioncombo(ctx.actioncombo());
			if (combo == null) {
				return null;
			} else if (ctx.runcondition() == null) {
				return new TestAction(combo);
			} else {
				return new TestAction(combo, visitRuncondition(ctx.runcondition()));
			}
		}
	}

	@Override
	public ActionCombo visitActioncombo(ActioncomboContext ctx) {
		SourceInfo ctxSource = getSourceInfo(ctx);
		MOD2GParser parser = prepareModuleParser(ctx.getText(), ctxSource);
		languageTools.parser.MOD2GParser.ActioncomboContext comboContext = parser.actioncombo();

		ModuleValidator sub = getModuleSub("inline-actions", ctxSource);
		return sub.visitActioncombo(comboContext);
	}

	@Override
	public MentalStateCondition visitMsc(MscContext ctx) {
		final SourceInfo ctxSource = getSourceInfo(ctx);
		MOD2GParser parser = prepareModuleParser(ctx.getText(), ctxSource);
		languageTools.parser.MOD2GParser.MscContext conditionContext = parser.msc();

		ModuleValidator sub = getModuleSub("inline-condition", ctxSource);
		return sub.visitMsc(conditionContext);
	}

	@Override
	public TestCondition visitTestcondition(TestconditionContext ctx) {
		if (ctx.temporaltest() != null) {
			TestCondition query = visitTemporaltest(ctx.temporaltest());
			if (query != null) {
				return query;
			}
		} else if (ctx.reacttest() != null) {
			TestCondition query = visitReacttest(ctx.reacttest());
			if (query != null) {
				return query;
			}
		}

		return null;
	}

	@Override
	public TestCondition visitReacttest(ReacttestContext ctx) {
		if (ctx.testmsc() != null && ctx.testmsc().size() == 2) {
			TestMentalStateCondition first = visitTestmsc(ctx.testmsc(0));
			TestMentalStateCondition second = visitTestmsc(ctx.testmsc(1));
			if (first == null || second == null) {
				return null;
			} else {
				TestCondition returned = new Always(first, getText(ctx));
				TestCondition nested = new Eventually(second, getText(ctx));
				returned.setNestedCondition(nested);
				return returned;
			}
		} else {
			reportError(TestError.TEST_MISSING_QUERY, ctx);
			return null;
		}
	}

	@Override
	public TestCondition visitTemporaltest(TemporaltestContext ctx) {
		TestMentalStateCondition msc = null;
		if (ctx.testmsc() != null) {
			msc = visitTestmsc(ctx.testmsc());
		}
		if (msc != null) {
			if (ctx.ALWAYS() != null) {
				return new Always(msc, getText(ctx));
			} else if (ctx.NEVER() != null) {
				return new Never(msc, getText(ctx));
			} else if (ctx.EVENTUALLY() != null) {
				return new Eventually(msc, getText(ctx));
			} else {
				reportError(TestError.TEST_INVALID_OPERATOR, ctx);
				return null;
			}
		} else {
			reportError(TestError.TEST_MISSING_QUERY, ctx);
			return null;
		}
	}

	@Override
	public Until visitRuncondition(RunconditionContext ctx) {
		TestMentalStateCondition msc = null;
		if (ctx.testmsc() != null) {
			msc = visitTestmsc(ctx.testmsc());
		}
		if (msc != null) {
			return new Until(msc, getText(ctx));
		} else {
			reportError(TestError.TEST_MISSING_QUERY, ctx);
			return null;
		}
	}

	@Override
	public TestMentalStateCondition visitTestmsc(TestmscContext ctx) {
		InputStreamPosition first = null;
		MentalStateCondition condition = null;
		if (ctx.msc() != null) {
			condition = visitMsc(ctx.msc());
			if (condition != null) {
				first = (InputStreamPosition) getSourceInfo(ctx.msc());
			}
		}

		InputStreamPosition second = null;
		DoneCondition testaction = null;
		if (ctx.doneTest() != null) {
			testaction = visitDoneTest(ctx.doneTest());
			if (testaction != null) {
				second = (InputStreamPosition) getSourceInfo(ctx.doneTest());
			}
		}

		if (first == null) {
			return new TestMentalStateCondition(testaction, condition);
		} else if (second == null) {
			return new TestMentalStateCondition(condition, testaction);
		} else {
			if (first.compareTo(second) > 0) {
				return new TestMentalStateCondition(testaction, condition);
			} else {
				return new TestMentalStateCondition(condition, testaction);
			}
		}
	}

	@Override
	public DoneCondition visitDoneTest(DoneTestContext ctx) {
		if (ctx.action() == null) {
			reportError(TestError.TEST_MISSING_ACTION, ctx);
			return null;
		} else {
			Action<?> action = visitAction(ctx.action());
			if (action == null) {
				return null;
			} else {
				ActionCombo combo = new ActionCombo(action.getSourceInfo());
				combo.addAction(action);
				return new DoneCondition(combo, (ctx.NOT() == null));
			}
		}
	}

	@Override
	public Action<?> visitAction(ActionContext ctx) {
		SourceInfo ctxSource = getSourceInfo(ctx);
		MOD2GParser parser = prepareModuleParser(ctx.getText(), ctxSource);
		languageTools.parser.MOD2GParser.ActionContext actionContext = parser.action();

		ModuleValidator sub = getModuleSub("inline-action", ctxSource);
		return sub.visitAction(actionContext);
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
	 * Creates an embedded module parser that can parse the given string.
	 *
	 * @param pString is the string to be parsed.
	 * @return a MOD2GParser.
	 */
	private MOD2GParser prepareModuleParser(String pString, SourceInfo sourceInfo) {
		String name = (sourceInfo.getSource() == null) ? "" : sourceInfo.getSource();
		CharStream stream = CharStreams.fromString(pString, name);

		GOALLexer lexer = new GOALLexer(stream);
		lexer.removeErrorListeners();
		lexer.addErrorListener(this);
		lexer.setLine(sourceInfo.getLineNumber());
		lexer.setCharPositionInLine(sourceInfo.getCharacterPosition() + 1);

		MOD2GParser parser = new MOD2GParser(new CommonTokenStream(lexer));
		parser.setErrorHandler(new ModuleErrorStrategy());
		parser.removeErrorListeners();
		parser.addErrorListener(this);

		return parser;
	}

	/**
	 * Creates an embedded test parser that can parse the given string.
	 *
	 * @param pString is the string to be parsed.
	 * @return a TEST2GParser.
	 */
	private TEST2GParser prepareTestParser(String pString, SourceInfo sourceInfo) {
		String name = (sourceInfo.getSource() == null) ? "" : sourceInfo.getSource();
		CharStream stream = CharStreams.fromString(pString, name);

		GOALLexer lexer = new GOALLexer(stream);
		lexer.removeErrorListeners();
		lexer.addErrorListener(this);
		lexer.setLine(sourceInfo.getLineNumber());
		lexer.setCharPositionInLine(sourceInfo.getCharacterPosition() + 1);

		TEST2GParser parser = new TEST2GParser(new CommonTokenStream(lexer));
		parser.setErrorHandler(new TestErrorStrategy());
		parser.removeErrorListeners();
		parser.addErrorListener(this);

		return parser;
	}

	private ModuleValidator getModuleSub(final String name, final SourceInfo ctxSource) {
		ModuleValidator sub = new ModuleValidator(name, getRegistry()) {
			@Override
			public boolean reportError(SyntaxError type, SourceInfo info, String... args) {
				SourceInfo isp = null;
				if (info != null) {
					isp = new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition(),
							ctxSource.getStartIndex() + info.getStartIndex(),
							ctxSource.getStartIndex() + info.getStopIndex() + 1, info.getSource());
				}
				return super.reportError(type, isp, args);
			}

			@Override
			public SourceInfo getSourceInfo(ParserRuleContext ctx) {
				Token start = (ctx == null) ? null : ctx.getStart();
				Token stop = (ctx == null) ? null : ctx.getStop();
				if (stop == null) {
					stop = start;
				}
				return (start == null) ? null
						: new InputStreamPosition(start.getLine(), start.getCharPositionInLine(),
								ctxSource.getStartIndex() + start.getStartIndex(),
								ctxSource.getStartIndex() + stop.getStopIndex() + 1, ctxSource.getSource());
			}

			@Override
			public SourceInfo getSourceInfo(TerminalNode leaf) {
				Token symbol = (leaf == null) ? null : leaf.getSymbol();
				return (symbol == null) ? null
						: new InputStreamPosition(symbol.getLine(), symbol.getCharPositionInLine(),
								ctxSource.getStartIndex() + symbol.getStartIndex(),
								ctxSource.getStartIndex() + symbol.getStopIndex() + 1, ctxSource.getSource());
			}
		};
		Module temp = new Module(getRegistry(), ctxSource);
		temp.setKRInterface(getProgram().getKRInterface());
		sub.overrideProgram(temp);
		return sub;
	}

	private TestValidator getTestSub(final String name, final SourceInfo ctxSource) {
		TestValidator sub = new TestValidator(name, getRegistry()) {
			@Override
			public boolean reportError(SyntaxError type, SourceInfo info, String... args) {
				SourceInfo isp = null;
				if (info != null) {
					isp = new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition(),
							ctxSource.getStartIndex() + info.getStartIndex(),
							ctxSource.getStartIndex() + info.getStopIndex() + 1, info.getSource());
				}
				return super.reportError(type, isp, args);
			}

			@Override
			public SourceInfo getSourceInfo(ParserRuleContext ctx) {
				Token start = (ctx == null) ? null : ctx.getStart();
				Token stop = (ctx == null) ? null : ctx.getStop();
				if (stop == null) {
					stop = start;
				}
				return (start == null) ? null
						: new InputStreamPosition(start.getLine(), start.getCharPositionInLine(),
								ctxSource.getStartIndex() + start.getStartIndex(),
								ctxSource.getStartIndex() + stop.getStopIndex() + 1, ctxSource.getSource());
			}

			@Override
			public SourceInfo getSourceInfo(TerminalNode leaf) {
				Token symbol = (leaf == null) ? null : leaf.getSymbol();
				return (symbol == null) ? null
						: new InputStreamPosition(symbol.getLine(), symbol.getCharPositionInLine(),
								ctxSource.getStartIndex() + symbol.getStartIndex(),
								ctxSource.getStartIndex() + symbol.getStopIndex() + 1, ctxSource.getSource());
			}
		};
		TestProgram temp = new TestProgram(getRegistry(), ctxSource);
		temp.setKRInterface(getProgram().getKRInterface());
		sub.overrideProgram(temp);
		return sub;
	}

	// THE FUNCTIONS BELOW ARE UNUSED...

	@Override
	public Void visitModule(ModuleContext ctx) {
		return null;
	}

	@Override
	public Void visitLearnclause(LearnclauseContext ctx) {
		return null;
	}

	@Override
	public Void visitExitoption(ExitoptionContext ctx) {
		return null;
	}

	@Override
	public Void visitFocusoption(FocusoptionContext ctx) {
		return null;
	}

	@Override
	public Void visitOrderoption(OrderoptionContext ctx) {
		return null;
	}

	@Override
	public Void visitMacro(MacroContext ctx) {
		return null;
	}

	@Override
	public Void visitRules(RulesContext ctx) {
		return null;
	}

	@Override
	public Void visitMentalliteral(MentalliteralContext ctx) {
		return null;
	}

	@Override
	public Void visitMentalatom(MentalatomContext ctx) {
		return null;
	}

	@Override
	public Void visitMentalop(MentalopContext ctx) {
		return null;
	}

	@Override
	public Void visitSelectoraction(SelectoractionContext ctx) {
		return null;
	}

	@Override
	public Void visitGeneralaction(GeneralactionContext ctx) {
		return null;
	}

	@Override
	public Void visitSelector(SelectorContext ctx) {
		return null;
	}

	@Override
	public Void visitSelectorop(SelectoropContext ctx) {
		return null;
	}

	private String getText(ParserRuleContext ctx) {
		int start = ctx.start.getStartIndex();
		int stop = (ctx.stop == null) ? start : ctx.stop.getStopIndex();
		return ctx.getStart().getInputStream().getText(new Interval(start, stop));
	}
}
