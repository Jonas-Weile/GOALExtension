package org.eclipse.gdt.ui.wizard;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.gdt.Activator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings({ "restriction", "deprecation" })
public class ExampleProjectWizardPage extends GoalWizardPage {
	// Section for retrieving project name from user
	private NameGroup projectNameGroup;
	// Section for retrieving environment file from user
	private ComboGroup exampleGroup;
	// Checks whether a valid instance of a project has been created by user
	private Validator projectValidator;

	protected ExampleProjectWizardPage(final String pageName) {
		super(pageName, "Create new GOAL Example Project", "Create a new GOAL Example Project in the workspace");
	}

	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// create UI elements
		this.exampleGroup = new ComboGroup(composite);
		this.projectNameGroup = new NameGroup(composite);

		// create and connect validator
		this.projectValidator = new Validator(this.projectNameGroup.getField(), true);
		this.projectNameGroup.addObserver(this.projectValidator);

		setControl(composite);
		Dialog.applyDialogFont(composite);

		// initialize elements
		this.projectNameGroup.dialogFieldChanged(null);
	}

	/**
	 * Requests a name. Fires an event whenever the text field is changed.
	 */
	private final class NameGroup extends Observable implements IDialogFieldListener {
		// text field for project name
		private final StringDialogField nameField;

		protected NameGroup(final Composite composite) {
			final Composite nameComposite = new Composite(composite, SWT.NONE);
			nameComposite.setFont(composite.getFont());
			nameComposite.setLayout(initGridLayout(new GridLayout(2, false), false));
			nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.nameField = new StringDialogField();
			this.nameField.setLabelText("Project name");
			this.nameField.setDialogFieldListener(this);
			this.nameField.doFillIntoGrid(nameComposite, 2);
			LayoutUtil.setHorizontalGrabbing(this.nameField.getTextControl(null));
		}

		public StringDialogField getField() {
			return this.nameField;
		}

		public String getName() {
			return this.nameField.getText().trim();
		}

		@Override
		public void dialogFieldChanged(final DialogField field) {
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Requests an example project from a list list. Fires an event whenever the
	 * text field is changed.
	 */
	private final class ComboGroup extends Observable implements IDialogFieldListener {
		// text field for the example project
		private final ComboDialogField projectField;

		protected ComboGroup(final Composite composite) {
			final Composite nameComposite = new Composite(composite, SWT.NONE);
			nameComposite.setFont(composite.getFont());
			nameComposite.setLayout(initGridLayout(new GridLayout(2, false), false));
			nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.projectField = new ComboDialogField(SWT.READ_ONLY);
			final List<String> items = new LinkedList<>();
			try {
				final BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream(new File(Activator.getDefault().getAgentPath())));
				final ZipInputStream zis = new ZipInputStream(bis);
				ZipEntry entry = null;
				while ((entry = zis.getNextEntry()) != null) {
					final String item = entry.getName();
					if (entry.isDirectory() && item.indexOf('/') == item.lastIndexOf('/')) {
						items.add(entry.getName().replace("/", ""));
					}
					zis.closeEntry();
				}
				zis.close();
				bis.close();
			} catch (final Exception e) {
				DLTKCore.error(e);
			}
			Collections.sort(items);
			this.projectField.setItems(items.toArray(new String[items.size()]));
			this.projectField.setLabelText("Example project");
			this.projectField.setDialogFieldListener(this);
			this.projectField.doFillIntoGrid(nameComposite, 2);
			LayoutUtil.setHorizontalGrabbing(this.projectField.getComboControl(null));
		}

		public String getName() {
			return this.projectField.getText().trim();
		}

		@Override
		public void dialogFieldChanged(final DialogField field) {
			setChanged();
			notifyObservers();
			ExampleProjectWizardPage.this.projectNameGroup.getField().setText(getName());
		}
	}

	/**
	 * Creates a project resource handle for the current project name field value.
	 * <p>
	 * This method does not create the project resource; this is the responsibility
	 * of <code>IProject::create</code> invoked by the new project resource wizard.
	 * </p>
	 *
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(this.projectNameGroup.getName());
	}

	public String getExampleHandle() {
		return this.exampleGroup.getName();
	}
}