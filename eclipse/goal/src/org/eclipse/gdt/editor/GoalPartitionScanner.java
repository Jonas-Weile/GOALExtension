package org.eclipse.gdt.editor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class GoalPartitionScanner extends RuleBasedPartitionScanner {
	public GoalPartitionScanner() {
		final IToken comment = new Token(IGoalPartitions.GOAL_COMMENT);

		final List<IPredicateRule> rules = new LinkedList<IPredicateRule>();

		// Rules for comments
		rules.add(new EndOfLineRule("%", comment));
		rules.add(new MultiLineRule("/*", "*/", comment));

		final IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}