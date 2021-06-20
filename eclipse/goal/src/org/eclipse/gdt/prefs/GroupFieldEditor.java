package org.eclipse.gdt.prefs;

import java.util.Collection;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Class is intended to create a Group Widgets, inside of the
 * {@link FieldEditorPreferencePage} objects. This class should be used as
 * following: use the {@link #getFieldEditorParent()} to as a parent, while
 * creating new Field Editors. use {@link #setFieldEditors(Collection)} to add
 * the collection of FieldEditors to the {@link GroupFieldEditor}.
 */
public class GroupFieldEditor extends FieldEditor {
	private final String name;
	private Collection<FieldEditor> members;
	private int numcolumns;
	private final Group group;
	private final Composite parent;

	/**
	 * The gap outside, between the group-frame and the widgets around the group
	 * (px)
	 */
	private static final int GROUP_PADDING = 5;
	/**
	 * The gap inside, between the group-frame and the content (px)
	 */
	private static final int GROUP_VERTICAL_MARGIN = 5;

	/**
	 * Creates a Group of {@link FieldEditor} objects
	 *
	 * @param name
	 *            - name
	 * @param fieldEditorParent
	 *            - parent
	 */
	public GroupFieldEditor(final String name, final Composite fieldEditorParent) {
		this.name = name;

		// the parent is a Composite, which is contained inside of the
		// preference page. Initially it does not have any layout.
		this.parent = fieldEditorParent;
		final FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = GROUP_VERTICAL_MARGIN;
		this.parent.setLayout(fillLayout);

		this.group = new Group(this.parent, SWT.SHADOW_OUT);
		this.group.setText(this.name);
	}

	/**
	 * The parent for all the FieldEditors inside of this Group.
	 *
	 * @return - the parent
	 */
	public Composite getFieldEditorParent() {
		return this.group;
	}

	/**
	 * Sets the FieldeditorChildren for this {@link GroupFieldEditor}
	 *
	 * @param membersParam
	 */
	public void setFieldEditors(final Collection<FieldEditor> membersParam) {
		this.members = membersParam;
		doFillIntoGrid(getFieldEditorParent(), this.numcolumns);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void adjustForNumColumns(final int numColumns) {
		this.numcolumns = numColumns;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doFillIntoGrid(final Composite parentParam, final int numColumns) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginLeft = GROUP_PADDING;
		gridLayout.marginRight = GROUP_PADDING;
		gridLayout.marginTop = GROUP_PADDING;
		gridLayout.marginBottom = GROUP_PADDING;
		this.group.setLayout(gridLayout);

		this.parent.layout();
		this.parent.redraw();

		if (this.members != null) {
			for (final FieldEditor editor : this.members) {
				editor.fillIntoGrid(getFieldEditorParent(), editor.getNumberOfControls());
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor. Loads the value from the
	 * preference store and sets it to the check box.
	 */
	@Override
	protected void doLoad() {
		if (this.members != null) {
			for (final FieldEditor editor : this.members) {
				editor.load();
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor. Loads the default value
	 * from the preference store and sets it to the check box.
	 */
	@Override
	protected void doLoadDefault() {
		if (this.members != null) {
			for (final FieldEditor editor : this.members) {
				editor.loadDefault();
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doStore() {
		if (this.members != null) {
			for (final FieldEditor editor : this.members) {
				editor.store();
			}
		}
	}

	@Override
	public void store() {
		doStore();
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	public int getNumberOfControls() {
		return 1;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	public void setFocus() {
		if (this.members != null && !this.members.isEmpty()) {
			this.members.iterator().next().setFocus();
		}
	}

	/*
	 * @see FieldEditor.setEnabled
	 */
	@Override
	public void setEnabled(final boolean enabled, final Composite parentParam) {
		if (this.members != null) {
			for (final FieldEditor editor : this.members) {
				editor.setEnabled(enabled, parentParam);
			}
		}
	}

	@Override
	public void setPreferenceStore(final IPreferenceStore store) {
		super.setPreferenceStore(store);
		if (this.members != null) {
			for (final FieldEditor editor : this.members) {
				editor.setPreferenceStore(store);
			}
		}
	}
}