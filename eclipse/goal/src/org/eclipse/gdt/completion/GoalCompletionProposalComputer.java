package org.eclipse.gdt.completion;

import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalCollector;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalComputer;
import org.eclipse.dltk.ui.text.completion.ScriptContentAssistInvocationContext;
import org.eclipse.dltk.ui.text.completion.ScriptTypeCompletionProposal;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.GoalNature;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.swt.graphics.Image;

public class GoalCompletionProposalComputer extends ScriptCompletionProposalComputer {

	public GoalCompletionProposalComputer() {
	}

	@Override
	protected ScriptCompletionProposalCollector createCollector(final ScriptContentAssistInvocationContext context) {
		return new GoalCompletionProposalCollector(context.getSourceModule());
	}

	@Override
	protected TemplateCompletionProcessor createTemplateProposalComputer(
			final ScriptContentAssistInvocationContext context) {
		return null;
	}

	public static class GoalCompletionProposalCollector extends ScriptCompletionProposalCollector {
		private static final char[] VAR_TRIGGER = { '\t', ' ', '=', ';', '.' };

		@Override
		protected char[] getVarTrigger() {
			return GoalCompletionProposalCollector.VAR_TRIGGER;
		}

		public GoalCompletionProposalCollector(final ISourceModule module) {
			super(module);
		}

		@Override
		protected ScriptCompletionProposal createScriptCompletionProposal(final String completion,
				final int replaceStart, final int length, final Image image, final String displayString, final int i) {
			return new GoalCompletionProposal(completion, replaceStart, length, image, displayString, i);
		}

		@Override
		protected ScriptCompletionProposal createScriptCompletionProposal(final String completion,
				final int replaceStart, final int length, final Image image, final String displayString, final int i,
				final boolean isInDoc) {
			return new GoalCompletionProposal(completion, replaceStart, length, image, displayString, i, isInDoc);
		}

		@Override
		protected ScriptCompletionProposal createOverrideCompletionProposal(final IScriptProject scriptProject,
				final ISourceModule compilationUnit, final String name, final String[] paramTypes, final int start,
				final int length, final String displayName, final String completionProposal) {
			return new GoalOverrideCompletionProposal(scriptProject, compilationUnit, name, paramTypes, start, length,
					displayName, completionProposal);
		}

		@Override
		protected String getNatureId() {
			return GoalNature.GOAL_NATURE;
		}
	}

	public static class GoalCompletionProposal extends ScriptCompletionProposal {
		public GoalCompletionProposal(final String replacementString, final int replacementOffset,
				final int replacementLength, final Image image, final String displayString, final int relevance) {
			super(replacementString, replacementOffset, replacementLength, image, displayString, relevance);
		}

		public GoalCompletionProposal(final String replacementString, final int replacementOffset,
				final int replacementLength, final Image image, final String displayString, final int relevance,
				final boolean isInDoc) {
			super(replacementString, replacementOffset, replacementLength, image, displayString, relevance, isInDoc);
		}

		@Override
		protected boolean isSmartTrigger(final char trigger) {
			return trigger == '.';
		}

		@Override
		protected boolean insertCompletion() {
			final IPreferenceStore preference = Activator.getDefault().getPreferenceStore();
			return preference.getBoolean(PreferenceConstants.CODEASSIST_INSERT_COMPLETION);
		}
	}

	public static class GoalOverrideCompletionProposal extends ScriptTypeCompletionProposal {

		public GoalOverrideCompletionProposal(final IScriptProject scriptProject, final ISourceModule compilationUnit,
				final String name, final String[] paramTypes, final int start, final int length,
				final String displayName, final String completionProposal) {
			super(completionProposal, compilationUnit, start, length, null, displayName, 0);

			final StringBuffer buffer = new StringBuffer();
			buffer.append(completionProposal);

			setReplacementString(buffer.toString());
		}

		@Override
		protected boolean isSmartTrigger(final char trigger) {
			return trigger == '.';
		}

		@Override
		protected boolean insertCompletion() {
			final IPreferenceStore preference = Activator.getDefault().getPreferenceStore();
			return preference.getBoolean(PreferenceConstants.CODEASSIST_INSERT_COMPLETION);
		}
	}
}