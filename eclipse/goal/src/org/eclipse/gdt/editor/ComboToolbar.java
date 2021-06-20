package org.eclipse.gdt.editor;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.Messages;
import org.eclipse.gdt.launch.GoalRunnableProcess;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class ComboToolbar extends WorkbenchWindowControlContribution {
	private final static Map<String, String> saved = new ConcurrentHashMap<>();
	private final int WIDTH = 220;
	private Combo mReader;
	private Map<String, IPath> mas;
	private IProject project;
	private IPath active;

	@Override
	protected Control createControl(final Composite parent) {
		Activator.getDefault().setActiveToolbar(this);
		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout glContainer = new GridLayout(1, false);
		glContainer.marginTop = -1;
		glContainer.marginHeight = 0;
		glContainer.marginWidth = 0;
		container.setLayout(glContainer);
		final GridData glReader = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		glReader.widthHint = this.WIDTH - 20;
		this.mReader = new Combo(container, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
		this.mReader.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				selectedIndex();
			}
		});
		this.mReader.setLayoutData(glReader);
		return container;
	}

	private void selectedIndex() {
		boolean active = false;
		final int index = this.mReader.getSelectionIndex();
		if (index >= 0) {
			final String item = this.mReader.getItem(index);
			if (this.mas != null) {
				this.active = this.mas.get(this.mReader.getItem(index));
				active = true;
			}
			saved.put(this.project.getName(), item);
		}
		if (!active) {
			this.active = null;
		}
	}

	public void resolveMAS(final IEditorInput editor) {
		try {
			this.mas = new ConcurrentHashMap<>();
			final IResource resource = editor.getAdapter(IResource.class);
			final Map<String, IPath> runnables = GoalRunnableProcess.getRunnables(resource);
			for (final String key : runnables.keySet()) {
				final IPath path = runnables.get(key);
				if (Messages.MASFileExtension.equalsIgnoreCase(path.getFileExtension())) {
					this.mas.put(key, path);
				}
			}
			if (this.mas.size() > 0) {
				this.project = resource.getProject();
				if (this.mReader.isDisposed()) {
					this.active = this.mas.get(this.project.getName());
				} else {
					final String[] items = this.mas.keySet().toArray(new String[this.mas.size()]);
					Arrays.sort(items);
					this.mReader.setItems(items);
					if (saved.containsKey(this.project.getName())) {
						final int index = Arrays.binarySearch(items, saved.get(this.project.getName()));
						this.mReader.select(index);
					} else {
						this.mReader.select(0);
					}
					selectedIndex();
				}
			} else if (this.mReader.isDisposed()) {
				this.active = null;
			} else {
				this.mReader.setItems(new String[0]);
				this.active = null;
			}
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
	}

	public IPath getActiveItem() {
		return this.active;
	}

	@Override
	protected int computeWidth(final Control control) {
		return this.WIDTH;
	}
}