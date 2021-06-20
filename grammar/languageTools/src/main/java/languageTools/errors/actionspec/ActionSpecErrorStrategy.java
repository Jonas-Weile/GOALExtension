package languageTools.errors.actionspec;

import org.antlr.v4.runtime.Token;

import languageTools.errors.MyErrorStrategy;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.parser.ACT2GParser;

public class ActionSpecErrorStrategy extends MyErrorStrategy {
	@Override
	public String prettyPrintToken(Token t) {
		String txt = prettyPrintToken(getSymbolType(t));
		switch (t.getType()) {
		case ACT2GParser.ID:
			return txt + " '" + t.getText() + "'";
		case ACT2GParser.VAR:
			return txt + " '" + t.getText() + "'";
		case ACT2GParser.FLOAT:
			return txt + " " + t.getText();
		case ACT2GParser.ERROR:
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
		case ACT2GParser.ID:
			return "an identifier";
		case ACT2GParser.FLOAT:
			return "a floating point number";
		case ACT2GParser.PARLIST:
			return "a list of KR terms";
		case ACT2GParser.StringLiteral:
			return "a double-quoted string";
		case ACT2GParser.SingleQuotedStringLiteral:
			return "a single-quoted string";
		case ACT2GParser.VAR:
			return "a parameter";
		case ACT2GParser.ERROR:
			return "";
		case ACT2GParser.KR_BLOCK:
			return "a KR expression";
		default:
			// Do not improve, simply return token symbol as is
			return ACT2GParser.VOCABULARY.getDisplayName(type);
		}
	}

	@Override
	public String prettyPrintRuleContext(int ruleIndex) {
		switch (ruleIndex) {
		case ACT2GParser.RULE_specifications:
			return "action specifications";
		case ACT2GParser.RULE_useclause:
			return "use clause";
		case ACT2GParser.RULE_ref:
			return "reference to a KR file";
		case ACT2GParser.RULE_actionspec:
			return "action specification";
		case ACT2GParser.RULE_asclause:
			return "as clause of an action specification";
		default:
			return ACT2GParser.ruleNames[ruleIndex];
		}
	}

	@Override
	public SyntaxError getLexerErrorType(Token token) {
		switch (token.getType()) {
		case ACT2GParser.StringLiteral:
			return SyntaxError.UNTERMINATEDSTRINGLITERAL;
		case ACT2GParser.SingleQuotedStringLiteral:
			return SyntaxError.UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL;
		default:
			return null;
		}
	}
}
