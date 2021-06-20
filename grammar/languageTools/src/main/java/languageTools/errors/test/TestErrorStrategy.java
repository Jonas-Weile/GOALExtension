package languageTools.errors.test;

import org.antlr.v4.runtime.Token;

import languageTools.errors.MyErrorStrategy;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.parser.TEST2GParser;

public class TestErrorStrategy extends MyErrorStrategy {
	@Override
	public String prettyPrintToken(Token t) {
		String txt = prettyPrintToken(getSymbolType(t));
		switch (t.getType()) {
		case TEST2GParser.ID:
			return txt + " '" + t.getText() + "'";
		case TEST2GParser.VAR:
			return txt + " '" + t.getText() + "'";
		case TEST2GParser.FLOAT:
			return txt + " " + t.getText();
		case TEST2GParser.ERROR:
			return txt + " '" + t.getText() + "'";
		default:
			return txt;
		}
	}

	@Override
	public String prettyPrintToken(int type) {
		switch (type) { // TODO: partial ModuleErrorStrategy duplication
		case Token.EOF:
			return "end of file";
		case TEST2GParser.ID:
			return "an identifier";
		case TEST2GParser.FLOAT:
			return "a floating point number";
		case TEST2GParser.PARLIST:
			return "a list of KR terms";
		case TEST2GParser.StringLiteral:
			return "a double-quoted string";
		case TEST2GParser.SingleQuotedStringLiteral:
			return "a single-quoted string";
		case TEST2GParser.VAR:
			return "a parameter";
		case TEST2GParser.ERROR:
			return "";
		case TEST2GParser.KR_BLOCK:
			return "a KR expression";
		default:
			// Do not improve, simply return token symbol as is
			return TEST2GParser.VOCABULARY.getDisplayName(type);
		}
	}

	@Override
	public String prettyPrintRuleContext(int ruleIndex) {
		switch (ruleIndex) { // TODO: partial ModuleErrorStrategy duplication
		case TEST2GParser.RULE_module:
			return "module";
		case TEST2GParser.RULE_useclause:
			return "a use clause";
		case TEST2GParser.RULE_usecase:
			return "a use case for a KR file";
		case TEST2GParser.RULE_option:
			return "a module option";
		case TEST2GParser.RULE_exitoption:
			return "an exit option";
		case TEST2GParser.RULE_focusoption:
			return "a focus option";
		case TEST2GParser.RULE_orderoption:
			return "an order option";
		case TEST2GParser.RULE_macro:
			return "macro definition";
		case TEST2GParser.RULE_rules:
			return "the rules of the module";
		case TEST2GParser.RULE_msc:
			return "a mental state condition";
		case TEST2GParser.RULE_mentalliteral:
			return "a mental state literal";
		case TEST2GParser.RULE_mentalatom:
			return "a mental state atom";
		case TEST2GParser.RULE_mentalop:
			return "a mental state operator";
		case TEST2GParser.RULE_actioncombo:
			return "a combination of multiple actions";
		case TEST2GParser.RULE_action:
			return "an action";
		case TEST2GParser.RULE_selectoraction:
			return "a built-in agent action";
		case TEST2GParser.RULE_generalaction:
			return "an general built-in action";
		case TEST2GParser.RULE_selector:
			return "one or more selector(s)";
		// actual test2g rules:
		case TEST2GParser.RULE_test:
			return "a test";
		case TEST2GParser.RULE_moduletest:
			return "a module test";
		case TEST2GParser.RULE_testcondition:
			return "a test condition";
		case TEST2GParser.RULE_temporaltest:
			return "a temporal test condition";
		case TEST2GParser.RULE_reacttest:
			return "a reactto test condition";
		case TEST2GParser.RULE_testmsc:
			return "one or more mental state or 'done' conditions";
		case TEST2GParser.RULE_doneTest:
			return "a 'done' condition";
		case TEST2GParser.RULE_agenttest:
			return "an agent test";
		case TEST2GParser.RULE_testaction:
			return "an action to execute";
		case TEST2GParser.RULE_runcondition:
			return "a run condition for an action";
		default:
			return TEST2GParser.ruleNames[ruleIndex];
		}
	}

	@Override
	public SyntaxError getLexerErrorType(Token token) {
		switch (token.getType()) {
		case TEST2GParser.StringLiteral:
			return SyntaxError.UNTERMINATEDSTRINGLITERAL;
		case TEST2GParser.SingleQuotedStringLiteral:
			return SyntaxError.UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL;
		default:
			return null;
		}
	}
}
