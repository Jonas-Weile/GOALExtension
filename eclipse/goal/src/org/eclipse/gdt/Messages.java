package org.eclipse.gdt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.gdt.messages";
	public static String ActionFileExtension;
	public static String ActionFileTemplateID;
	public static String GoalProjectWizard_Create_new_project;
	public static String NewProjectWizard_Title;
	public static String NewProjectWizard_1;
	public static String ExampleProjectWizard_Title;
	public static String ExampleProjectWizard_1;
	public static String ImportProjectWizard_Title;
	public static String ImportProjectWizard_1;
	public static String MASFileExtension;
	public static String MASFileTemplateID;
	public static String MASFileEnvTemplateID;
	public static String ModuleFileExtension;
	public static String ModuleTemplateFileID;
	public static String TestFileExtension;
	public static String TestTemplateFileID;
	public static String PlannerFileExtension;
	public static String PrologFileExtension;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}