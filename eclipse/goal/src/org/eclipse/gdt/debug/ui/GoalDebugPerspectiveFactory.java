package org.eclipse.gdt.debug.ui;

import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.gdt.debug.history.GoalHistoryView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

@SuppressWarnings("restriction")
public class GoalDebugPerspectiveFactory implements IPerspectiveFactory {
	private final static String SCRIPTDISPLAY = "org.eclipse.dltk.debug.ui.ScriptDisplayView";
	private final static String SCRIPTACTIONS = "org.eclipse.dltk.debug.ui.ScriptDebugActionSet";

	@Override
	public void createInitialLayout(final IPageLayout layout) {
		final IFolderLayout navFolder1 = layout.createFolder(IInternalDebugUIConstants.ID_NAVIGATOR_FOLDER_VIEW,
				IPageLayout.TOP, 0.25f, layout.getEditorArea());
		navFolder1.addView(IDebugUIConstants.ID_DEBUG_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_DEBUG_VIEW);
		navFolder1.addView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		navFolder1.addView(IDebugUIConstants.ID_EXPRESSION_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_EXPRESSION_VIEW);

		final IFolderLayout consolesFolder = layout.createFolder(IInternalDebugUIConstants.ID_CONSOLE_FOLDER_VIEW,
				IPageLayout.BOTTOM, 0.4f, layout.getEditorArea());
		consolesFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		consolesFolder.addPlaceholder(GoalAgentConsoleView.VIEW_ID + ":*");

		final IFolderLayout toolsFolder1 = layout.createFolder(IInternalDebugUIConstants.ID_TOOLS_FOLDER_VIEW,
				IPageLayout.RIGHT, 0.75f, IInternalDebugUIConstants.ID_NAVIGATOR_FOLDER_VIEW);
		toolsFolder1.addView(GoalVariablesViewBeliefs.VIEW_ID);
		layout.addShowViewShortcut(GoalVariablesViewBeliefs.VIEW_ID);
		toolsFolder1.addView(GoalVariablesViewGoals.VIEW_ID);
		layout.addShowViewShortcut(GoalVariablesViewGoals.VIEW_ID);
		toolsFolder1.addView(GoalVariablesViewPercepts.VIEW_ID);
		layout.addShowViewShortcut(GoalVariablesViewPercepts.VIEW_ID);
		toolsFolder1.addView(GoalVariablesViewMails.VIEW_ID);
		layout.addShowViewShortcut(GoalVariablesViewMails.VIEW_ID);

		final IFolderLayout toolsFolder2 = layout.createFolder(IInternalDebugUIConstants.ID_TOOLS_FOLDER_VIEW + "2",
				IPageLayout.RIGHT, 0.75f, layout.getEditorArea());
		toolsFolder2.addView(GoalVariablesViewEvaluation.VIEW_ID);
		layout.addShowViewShortcut(GoalVariablesViewEvaluation.VIEW_ID);

		final IFolderLayout toolsFolder3 = layout.createFolder(IInternalDebugUIConstants.ID_TOOLS_FOLDER_VIEW + "3",
				IPageLayout.RIGHT, 0.75f, IInternalDebugUIConstants.ID_CONSOLE_FOLDER_VIEW);
		toolsFolder3.addView(SCRIPTDISPLAY);
		layout.addShowViewShortcut(SCRIPTDISPLAY);

		layout.addView(GoalHistoryView.VIEW_ID, IPageLayout.TOP, 0.6f,
				IInternalDebugUIConstants.ID_CONSOLE_FOLDER_VIEW);

		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
		layout.addActionSet(SCRIPTACTIONS);
		layout.addActionSet("org.eclipse.gdt.debug.history.actionSet");
	}
}