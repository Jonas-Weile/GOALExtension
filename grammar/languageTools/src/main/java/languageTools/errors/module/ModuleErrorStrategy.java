package languageTools.errors.module;

import org.antlr.v4.runtime.Token;

import languageTools.errors.MyErrorStrategy;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.parser.MOD2GParser;

public class ModuleErrorStrategy extends MyErrorStrategy {
	@Override
	public String prettyPrintToken(Token t) {
		String txt = prettyPrintToken(getSymbolType(t));
		switch (t.getType()) {
		case MOD2GParser.ID:
			return txt + " '" + t.getText() + "'";
		case MOD2GParser.VAR:
			return txt + " '" + t.getText() + "'";
		case MOD2GParser.FLOAT:
			return txt + " " + t.getText();
		case MOD2GParser.ERROR:
			return txt + " '" + t.getText() + "'";
		default:
			return txt;
		}
	}

	@Override
	public String prettyPrintToken(int type) {
		switch (type) {
		case Token.EOF:
			return "end of file";
		case MOD2GParser.ID:
			return "an identifier";
		case MOD2GParser.FLOAT:
			return "a floating point number";
		case MOD2GParser.PARLIST:
			return "a list of parameters";
		case MOD2GParser.StringLiteral:
			return "a double-quoted string";
		case MOD2GParser.SingleQuotedStringLiteral:
			return "a single-quoted string";
		case MOD2GParser.VAR:
			return "a parameter";
		case MOD2GParser.ERROR:
			return "";
		case MOD2GParser.KR_BLOCK:
			return "a KR expression";
		default:
			// Do not improve, simply return token symbol as is
			return MOD2GParser.VOCABULARY.getDisplayName(type);
		}
	}

	@Override
	public String prettyPrintRuleContext(int ruleIndex) {
		switch (ruleIndex) {
		case MOD2GParser.RULE_module:
			return "module";
		case MOD2GParser.RULE_useclause:
			return "a use clause";
		case MOD2GParser.RULE_usecase:
			return "a use case for a KR file";
		case MOD2GParser.RULE_option:
			return "a module option";
		case MOD2GParser.RULE_exitoption:
			return "an exit option";
		case MOD2GParser.RULE_focusoption:
			return "a focus option";
		case MOD2GParser.RULE_orderoption:
			return "an order option";
		case MOD2GParser.RULE_macro:
			return "macro definition";
		case MOD2GParser.RULE_rules:
			return "the rules of the module";
		case MOD2GParser.RULE_msc:
			return "a mental state condition";
		case MOD2GParser.RULE_mentalliteral:
			return "a mental state literal";
		case MOD2GParser.RULE_mentalatom:
			return "a mental state atom";
		case MOD2GParser.RULE_mentalop:
			return "a mental state operator";
		case MOD2GParser.RULE_actioncombo:
			return "a combination of multiple actions";
		case MOD2GParser.RULE_action:
			return "an action";
		case MOD2GParser.RULE_selectoraction:
			return "a built-in agent action";
		case MOD2GParser.RULE_generalaction:
			return "an general built-in action";
		case MOD2GParser.RULE_selector:
			return "one or more selector(s)";
		default:
			return MOD2GParser.ruleNames[ruleIndex];
		}
	}

	@Override
	public SyntaxError getLexerErrorType(Token token) {
		switch (token.getType()) {
		case MOD2GParser.StringLiteral:
			return SyntaxError.UNTERMINATEDSTRINGLITERAL;
		case MOD2GParser.SingleQuotedStringLiteral:
			return SyntaxError.UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL;
		default:
			return null;
		}
	}
}
