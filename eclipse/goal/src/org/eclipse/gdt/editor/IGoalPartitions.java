package org.eclipse.gdt.editor;

import org.eclipse.jface.text.IDocument;

public interface IGoalPartitions {
	public final static String GOAL_PARTITIONING = "__goal_partitioning";
	public final static String GOAL_COMMENT = "__goal_comment";
	public final static String[] GOAL_PARITION_TYPES = new String[] { GOAL_COMMENT, IDocument.DEFAULT_CONTENT_TYPE };
}