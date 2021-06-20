package languageTools.errors.mas;

import org.antlr.v4.runtime.Token;

import languageTools.errors.MyErrorStrategy;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.parser.MAS2GParser;

public class MASErrorStrategy extends MyErrorStrategy {
	@Override
	public String prettyPrintToken(Token t) {
		String txt = prettyPrintToken(getSymbolType(t));
		switch (t.getType()) {
		case MAS2GParser.ID:
			return txt + " '" + t.getText() + "'";
		case MAS2GParser.FLOAT:
			return txt + " " + t.getText();
		case MAS2GParser.INT:
			return txt + " " + t.getText();
		default:
			return txt;
		}
	}

	@Override
	public String prettyPrintToken(int type) {
		switch (type) {
		case Token.EOF:
			return "end of file";
		case MAS2GParser.ID:
			return "an identifier";
		case MAS2GParser.FLOAT:
			return "a floating point number";
		case MAS2GParser.INT:
			return "an integer";
		case MAS2GParser.StringLiteral:
			return "a double-quoted string";
		case MAS2GParser.SingleQuotedStringLiteral:
			return "a single-quoted string";
		default:
			// Do not improve, simply return token symbol as is
			return MAS2GParser.VOCABULARY.getDisplayName(type);
		}
	}

	@Override
	public String prettyPrintRuleContext(int ruleIndex) {
		switch (ruleIndex) {
		case MAS2GParser.RULE_mas:
			return "a MAS definition";
		case MAS2GParser.RULE_environment:
			return "an environment section";
		case MAS2GParser.RULE_initKeyValue:
			return "a key-value pair";
		case MAS2GParser.RULE_initExpr:
			return "an init value";
		case MAS2GParser.RULE_constant:
			return "an identifier, number, or string";
		case MAS2GParser.RULE_function:
			return "a function";
		case MAS2GParser.RULE_list:
			return "a list";
		case MAS2GParser.RULE_agent:
			return "an agent definition section";
		case MAS2GParser.RULE_useClause:
			return "a use clause";
		case MAS2GParser.RULE_useCase:
			return "a use case";
		case MAS2GParser.RULE_policy:
			return "a launch policy section";
		case MAS2GParser.RULE_launchRule:
			return "a launch rule";
		case MAS2GParser.RULE_entity:
			return "an entity";
		case MAS2GParser.RULE_instruction:
			return "a launch instruction";
		case MAS2GParser.RULE_constraint:
			return "a launch rule constraint";
		case MAS2GParser.RULE_string:
			return "a string";
		default:
			return MAS2GParser.ruleNames[ruleIndex];
		}
	}

	@Override
	public SyntaxError getLexerErrorType(Token token) {
		switch (token.getType()) {
		case MAS2GParser.StringLiteral:
			return SyntaxError.UNTERMINATEDSTRINGLITERAL;
		case MAS2GParser.SingleQuotedStringLiteral:
			return SyntaxError.UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL;
		default:
			return null;
		}
	}
}
