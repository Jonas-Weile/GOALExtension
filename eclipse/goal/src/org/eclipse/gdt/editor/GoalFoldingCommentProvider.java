package org.eclipse.gdt.editor;

import org.eclipse.dltk.ui.text.IPartitioningProvider;
import org.eclipse.dltk.ui.text.folding.IFoldingBlockKind;
import org.eclipse.dltk.ui.text.folding.IFoldingContent;
import org.eclipse.dltk.ui.text.folding.PartitioningFoldingBlockProvider;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class GoalFoldingCommentProvider extends PartitioningFoldingBlockProvider {

	public GoalFoldingCommentProvider() {
		super(GoalPartitioningProvider.getInstance());
	}

	@Override
	public void computeFoldableBlocks(final IFoldingContent content) {
		computeBlocksForPartitionType(content, IGoalPartitions.GOAL_COMMENT, new GoalCommentBlockKind(),
				isCollapseComments());
	}

	private static class GoalCommentBlockKind implements IFoldingBlockKind {
		@Override
		public boolean isComment() {
			return true;
		}
	}

	private static class GoalPartitioningProvider implements IPartitioningProvider {
		private static final IPartitioningProvider INSTANCE = new GoalPartitioningProvider();

		public static IPartitioningProvider getInstance() {
			return INSTANCE;
		}

		private GoalPartitioningProvider() {
		}

		@Override
		public IPartitionTokenScanner createPartitionScanner() {
			return new GoalPartitionScanner();
		}

		@Override
		public String[] getPartitionContentTypes() {
			return IGoalPartitions.GOAL_PARITION_TYPES;
		}

		@Override
		public String getPartitioning() {
			return IGoalPartitions.GOAL_PARTITIONING;
		}
	}
}
