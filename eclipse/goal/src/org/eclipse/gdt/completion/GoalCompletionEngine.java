package org.eclipse.gdt.completion;

import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.Vocabulary;
import org.eclipse.dltk.codeassist.RelevanceConstants;
import org.eclipse.dltk.codeassist.ScriptCompletionEngine;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.IType;
import org.eclipse.gdt.Messages;

import languageTools.parser.GOALLexer;

public class GoalCompletionEngine extends ScriptCompletionEngine {
	private final static Set<String> goalKeywords = new HashSet<String>();
	private final static Set<String> masKeywords = new HashSet<String>();

	static {
		final String onlyLowerCase = "\\p{javaLowerCase}*";
		final Vocabulary goal = new GOALLexer(null).getVocabulary();
		for (int i = 0; i < goal.getMaxTokenType(); ++i) {
			final String keyword = goal.getDisplayName(i).replace("'", "");
			if (keyword.matches(onlyLowerCase)) {
				goalKeywords.add(keyword);
			}
		}
		final Vocabulary mas = new GOALLexer(null).getVocabulary();
		for (int i = 0; i < mas.getMaxTokenType(); ++i) {
			final String keyword = mas.getDisplayName(i).replace("'", "");
			if (keyword.matches(onlyLowerCase)) {
				masKeywords.add(keyword);
			}
		}
	}

	private String prefix;

	@Override
	public void complete(final IModuleSource module, final int position, final int offset) {
		this.actualCompletionPosition = position;
		this.offset = offset;

		final StringBuffer source = new StringBuffer(module.getSourceContents());
		final StringBuffer prefix = new StringBuffer();
		for (int i = position; i > 0; i--) {
			final char curChar = source.charAt(i - 1);
			if (!Character.isLetterOrDigit(curChar) && curChar != '_') {
				break;
			}
			prefix.insert(0, curChar);
		}
		this.prefix = prefix.toString();

		try {
			final Set<String> proposals = new HashSet<String>();
			module.getModelElement().accept(new IModelElementVisitor() {
				@Override
				public boolean visit(final IModelElement element) {
					final String toAdd = element.getElementName().trim();
					if (element.getElementType() > IModelElement.SOURCE_MODULE
							&& toAdd.startsWith(GoalCompletionEngine.this.prefix) && !toAdd.isEmpty()
							&& proposals.add(toAdd)) {
						createProposal(toAdd, element);
					}
					return true;
				}
			});
			final String name = module.getFileName().toLowerCase();
			if (name.endsWith(Messages.MASFileExtension)) {
				for (final String keyword : masKeywords) {
					if (keyword.startsWith(this.prefix) && proposals.add(keyword)) {
						createProposal(keyword, module.getModelElement());
					}
				}
			} else if (name.endsWith(Messages.ActionFileExtension) || name.endsWith(Messages.ModuleFileExtension) ||
					   name.endsWith(Messages.PlannerFileExtension) || name.endsWith(Messages.TestFileExtension)) {
				// TODO: check if we are in KR section or not and adapt?!
				for (final String keyword : goalKeywords) {
					if (keyword.startsWith(this.prefix) && proposals.add(keyword)) {
						createProposal(keyword, module.getModelElement());
					}
				}
			}
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
	}

	private void createProposal(final String name, final IModelElement element) {
		CompletionProposal proposal = null;
		try {
			if (element == null) {
				proposal = this.createProposal(CompletionProposal.KEYWORD, this.actualCompletionPosition);
			} else {
				switch (element.getElementType()) {
				case IModelElement.METHOD:
					proposal = this.createProposal(CompletionProposal.METHOD_REF, this.actualCompletionPosition);
					proposal.setFlags(((IMethod) element).getFlags());
					break;
				case IModelElement.FIELD:
					proposal = this.createProposal(CompletionProposal.FIELD_REF, this.actualCompletionPosition);
					proposal.setFlags(((IField) element).getFlags());
					break;
				case IModelElement.TYPE:
					proposal = this.createProposal(CompletionProposal.TYPE_REF, this.actualCompletionPosition);
					proposal.setFlags(((IType) element).getFlags());
					break;
				case IModelElement.LOCAL_VARIABLE:
					proposal = this.createProposal(CompletionProposal.LOCAL_VARIABLE_REF,
							this.actualCompletionPosition);
					break;
				default:
					proposal = this.createProposal(CompletionProposal.KEYWORD, this.actualCompletionPosition);
					break;
				}
			}
			proposal.setName(name);
			proposal.setCompletion(name);
			proposal.setRelevance(RelevanceConstants.R_INTERESTING);
			proposal.setReplaceRange(this.actualCompletionPosition - this.offset - this.prefix.length(),
					this.actualCompletionPosition - this.offset);
			proposal.setModelElement(element);
			this.requestor.accept(proposal);
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
	}
}