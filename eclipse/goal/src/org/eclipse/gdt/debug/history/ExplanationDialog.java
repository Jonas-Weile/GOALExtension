package org.eclipse.gdt.debug.history;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.gdt.debug.dbgp.LocalDebugger;
import org.eclipse.gdt.parser.GoalSourceParser;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import goal.tools.history.EventStorage;
import goal.tools.history.explanation.DebuggingIsExplaining;
import languageTools.program.ProgramMap;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.actions.Action;

public class ExplanationDialog extends TrayDialog {
	public enum Explanation {
		WHY_ACTION("Why was the action executed?"), WHY_NOT_ACTION("Why was the action NOT executed?");

		private final String description;

		private Explanation(final String description) {
			this.description = description;
		}

		public String getDescription() {
			return this.description;
		}
	}

	private final LocalDebugger debugger;
	private final AgentId agent;
	private final DebuggingIsExplaining explanation;
	private ComboViewer combo;
	private Text text;
	private Text result;

	public ExplanationDialog(final LocalDebugger debugger, final AgentId agent) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		setHelpAvailable(false);
		this.debugger = debugger;
		this.agent = agent;
		final EventStorage history = debugger.getAgentState(agent).getHistory(debugger);
		final ProgramMap map = GoalSourceParser.getMap(debugger.getPath());
		this.explanation = new DebuggingIsExplaining(history, map);
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("History Explanation");
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		createComboField(container);
		createAutocompleteField(container);

		this.result = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		final GridData multiline = new GridData(SWT.FILL, SWT.FILL, true, true);
		multiline.heightHint = 10 * this.text.getLineHeight();
		this.result.setLayoutData(multiline);
		this.result.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				e.doit = false;
			}
		});

		final Button button = new Button(container, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		button.setText("Answer");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent se) {
				final StructuredSelection sel = (StructuredSelection) ExplanationDialog.this.combo.getSelection();
				try {
					final Explanation question = (Explanation) sel.getFirstElement();
					final String subject = ExplanationDialog.this.text.getText();
					final String explanation = ExplanationDialog.this.debugger.explain(ExplanationDialog.this.agent,
							question, subject);
					ExplanationDialog.this.result.setText(explanation);
				} catch (final Exception e) {
					DLTKCore.error(e);
				}
			}
		});

		return area;
	}

	private void createComboField(final Composite container) {
		final Label question = new Label(container, SWT.NONE);
		question.setText("Question");

		this.combo = new ComboViewer(container, SWT.READ_ONLY);
		this.combo.setContentProvider(ArrayContentProvider.getInstance());
		this.combo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof Explanation) {
					return ((Explanation) element).getDescription();
				} else {
					return super.getText(element);
				}
			}
		});
		this.combo.setInput(Explanation.values());
	}

	private void createAutocompleteField(final Composite container) {
		final Label subject = new Label(container, SWT.NONE);
		subject.setText("Subject");

		final GridData grid = new GridData();
		grid.grabExcessHorizontalSpace = true;
		grid.horizontalAlignment = GridData.FILL;
		this.text = new Text(container, SWT.BORDER);
		this.text.setLayoutData(grid);

		final ContentProposalAdapter autocomplete = new ContentProposalAdapter(this.text, new TextContentAdapter(),
				new SimpleContentProposalProvider(getActions()), null, null);
		autocomplete.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
	}

	private String[] getActions() {
		this.explanation.process();
		final List<String> actions = new LinkedList<>();
		for (final Action<?> action : this.explanation.getAllActions()) {
			actions.add(action.toString());
		}
		return actions.toArray(new String[actions.size()]);
	}
}
