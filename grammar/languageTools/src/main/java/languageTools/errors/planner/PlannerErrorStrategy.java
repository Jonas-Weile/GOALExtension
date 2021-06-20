package languageTools.errors.planner;

import org.antlr.v4.runtime.Token;

import languageTools.errors.MyErrorStrategy;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.parser.PLAN2GParser;

public class PlannerErrorStrategy extends MyErrorStrategy {

	@Override
	public String prettyPrintToken(Token t) {
		String txt = prettyPrintToken(getSymbolType(t));
		switch (t.getType()) {
		case PLAN2GParser.ID:
			return txt + " '" + t.getText() + "'";
		case PLAN2GParser.VAR:
			return txt + " '" + t.getText() + "'";
		case PLAN2GParser.FLOAT:
			return txt + " " + t.getText();
		case PLAN2GParser.ERROR:
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
		case PLAN2GParser.ID:
			return "an identifier";
		case PLAN2GParser.FLOAT:
			return "a floating point number";
		case PLAN2GParser.PARLIST:
			return "a list of parameters";
		case PLAN2GParser.StringLiteral:
			return "a double-quoted string";
		case PLAN2GParser.SingleQuotedStringLiteral:
			return "a single-quoted string";
		case PLAN2GParser.VAR:
			return "a parameter";
		case PLAN2GParser.ERROR:
			return "";
		case PLAN2GParser.KR_BLOCK:
			return "a KR expression";
		default:
			// Do not improve, simply return token symbol as is
			return PLAN2GParser.VOCABULARY.getDisplayName(type);
		}
	}
	
	
	
	@Override
	public String prettyPrintRuleContext(int ruleIndex) {
		switch (ruleIndex) {
		default:
			return PLAN2GParser.ruleNames[ruleIndex];
		}
	}
	
	
	@Override
	public SyntaxError getLexerErrorType(Token token) {
		switch (token.getType()) {
		case PLAN2GParser.StringLiteral:
			return SyntaxError.UNTERMINATEDSTRINGLITERAL;
		case PLAN2GParser.SingleQuotedStringLiteral:
			return SyntaxError.UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL;
		default:
			return null;
		}
	}
}
