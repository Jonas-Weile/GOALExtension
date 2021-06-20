package org.eclipse.gdt.debug.ui;

import org.eclipse.gdt.debug.dbgp.LocalDebugger;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import languageTools.program.agent.AgentId;

public class GoalAgentConsole extends MessageConsole {
	private final static String goalDebug = "org.eclipse.gdt.debug.perspective";
	private final MessageConsoleStream output;
	private String last;

	public GoalAgentConsole(final AgentId agent) {
		super(agent == null ? "Action history" : agent.toString(), null);
		setWaterMarks(80000, 100000); // FIXME: good defaults?!
		this.output = newMessageStream();
		this.last = "";
	}

	public void println(final String s) {
		this.last = s;
		this.output.println(s);
	}

	public String getLast() {
		return this.last;
	}

	public void initialize(final LocalDebugger parent) {
		Display.getDefault().asyncExec(new Runnable() {
			private void addConsole(final IWorkbenchPage page) {
				try {
					final IConsoleManager conMan = ConsolePlugin.getDefault().getConsoleManager();
					page.showView(GoalAgentConsoleView.VIEW_ID, GoalAgentConsole.this.getName(),
							IWorkbenchPage.VIEW_VISIBLE);
					conMan.addConsoles(new IConsole[] { GoalAgentConsole.this });
					Display.getDefault().asyncExec(() -> {
						final String find = parent.getPath().lastSegment();
						final IConsoleView main = ((IConsoleView) page.findView(IConsoleConstants.ID_CONSOLE_VIEW));
						for (final IConsole console : conMan.getConsoles()) {
							final String name = console.getName();
							if (name.startsWith("[Debug Console]") && name.contains(find)) {
								main.display(console);
								main.setPinned(true);
								break;
							}
						}
					});
				} catch (final Exception e) {
					parent.err(e);
				}
			}

			@Override
			public void run() {
				final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				final IPerspectiveDescriptor current = window.getActivePage().getPerspective();
				if (current != null && current.getId().equals(goalDebug)) {
					addConsole(window.getActivePage());
				} else {
					window.addPerspectiveListener(new IPerspectiveListener() {
						@Override
						public void perspectiveChanged(final IWorkbenchPage page,
								final IPerspectiveDescriptor perspective, final String changeId) {
							// perspectiveActivated(page, perspective);
						}

						@Override
						public void perspectiveActivated(final IWorkbenchPage page,
								final IPerspectiveDescriptor perspective) {
							if (perspective != null && perspective.getId().equals(goalDebug)) {
								window.removePerspectiveListener(this);
								addConsole(page);
							}
						}
					});
				}
			}
		});

	}

	@Override
	protected void dispose() {
		getPartitioner().connect(new Document()); // otherwise Eclipse gives nullpointers?
		super.dispose();
		final IConsoleManager conMan = ConsolePlugin.getDefault().getConsoleManager();
		conMan.removeConsoles(new IConsole[] { this });
	}
}
