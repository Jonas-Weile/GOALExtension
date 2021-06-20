package org.eclipse.gdt.prefs;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;

import goal.preferences.CorePreferences;
import goal.preferences.LoggingPreferences;
import goal.preferences.ProfilerPreferences;
import goal.tools.profiler.InfoType;

public class GoalRuntimePreferencePage extends GoalPreferencePage {

	@Override
	protected void createFieldEditors() {
		final GroupFieldEditor general = new GroupFieldEditor("General", getFieldEditorParent());
		final List<FieldEditor> generalFields = new LinkedList<>();
		generalFields.add(new BooleanFieldEditor(CorePreferences.Pref.agentCopyEnvState.name(),
				"New agents copy the environment's state", general.getFieldEditorParent()));
		generalFields.add(new BooleanFieldEditor(CorePreferences.Pref.printEntities.name(),
				"Print a message when an entity (dis)appears", general.getFieldEditorParent()));
		// generalFields.add(new
		// BooleanFieldEditor(PMPreferences.Pref.middlewareLocal.name(),
		// "Always run the middleware locally",
		// general.getFieldEditorParent()));
		general.setFieldEditors(generalFields);
		addField(general);

		final GroupFieldEditor performance = new GroupFieldEditor("Performance", getFieldEditorParent());
		final List<FieldEditor> performanceFields = new LinkedList<>();
		performanceFields.add(new BooleanFieldEditor(CorePreferences.Pref.sleepRepetitiveAgent.name(),
				"Sleep agents when they get no new messages or percepts", performance.getFieldEditorParent()));
		performanceFields.add(new BooleanFieldEditor(CorePreferences.Pref.removeKilledAgent.name(),
				"Remove agents completely when they are killed", performance.getFieldEditorParent()));
		performanceFields.add(new BooleanFieldEditor(CorePreferences.Pref.sequentialExecution.name(),
				"Execute multiple agents in sequence (instead of in parallel)", performance.getFieldEditorParent()));
		performance.setFieldEditors(performanceFields);
		addField(performance);

		final GroupFieldEditor debugging = new GroupFieldEditor("Debugging and testing", getFieldEditorParent());
		final List<FieldEditor> debuggingFields = new LinkedList<>();
		debuggingFields.add(new BooleanFieldEditor(CorePreferences.Pref.breakOnGoalAchieved.name(),
				"Break (step) on goal achievement", debugging.getFieldEditorParent()));
		debuggingFields.add(new BooleanFieldEditor(CorePreferences.Pref.globalBreakpoints.name(),
				"Pause all agents upon hitting a user breakpoint", debugging.getFieldEditorParent()));
		debuggingFields.add(new BooleanFieldEditor(CorePreferences.Pref.abortOnTestFailure.name(),
				"Abort tests directly upon first failure", debugging.getFieldEditorParent()));
		debuggingFields.add(new BooleanFieldEditor(LoggingPreferences.Pref.enableHistory.name(),
				"Enable trace recording (history)", debugging.getFieldEditorParent()));
		debugging.setFieldEditors(debuggingFields);
		addField(debugging);

		final GroupFieldEditor profiling = new GroupFieldEditor("Profiling", getFieldEditorParent());
		final List<FieldEditor> profilingFields = new LinkedList<>();
		profilingFields.add(new BooleanFieldEditor(ProfilerPreferences.Pref.profiling.name(), "Enable profiling",
				profiling.getFieldEditorParent()));
		profilingFields.add(new BooleanFieldEditor(ProfilerPreferences.Pref.profilingToFile.name(),
				"Save profiling data to file(s)", profiling.getFieldEditorParent()));
		profilingFields.add(new BooleanFieldEditor(ProfilerPreferences.Pref.logNodeID.name(),
				"Include profile node IDs", profiling.getFieldEditorParent()));
		for (InfoType type : InfoType.values()) {
			profilingFields.add(new BooleanFieldEditor(type.name(), "Profile " + type.getDescription(),
					profiling.getFieldEditorParent()));
		}
		profiling.setFieldEditors(profilingFields);
		addField(profiling);

		// final GroupFieldEditor data = new GroupFieldEditor("Data collection",
		// getFieldEditorParent());
		// final List<FieldEditor> dataFields = new LinkedList<>();
		// final StringButtonFieldEditor dataCollection = new
		// StringButtonFieldEditor(Activator.PERMISSION,
		// "UUID for data collection for research purposes (N means no consent)",
		// data.getFieldEditorParent()) {
		// @Override
		// protected String changePressed() {
		// String newValue;
		// if (getPreferenceStore().getString(Activator.PERMISSION).length() > 1) {
		// newValue = "N";
		// } else {
		// newValue = UUID.randomUUID().toString();
		// }
		// getPreferenceStore().setValue(Activator.PERMISSION, newValue);
		// setStringValue(newValue);
		// return null;
		// }
		// };
		// dataCollection.setChangeButtonText("Toggle on/off");
		// dataCollection.getTextControl(data.getFieldEditorParent()).setEditable(false);
		// dataFields.add(dataCollection);
		// data.setFieldEditors(dataFields);
		// addField(data);

		// final GroupFieldEditor learning = new GroupFieldEditor("Learning",
		// getFieldEditorParent());
		// final List<FieldEditor> learningFields = new
		// LinkedList<FieldEditor>();
		// learningFields.add(new
		// BooleanFieldEditor(CorePreferences.Pref.learning.name(), "Enable
		// learning",
		// learning.getFieldEditorParent()));
		// learningFields.add(new
		// FileFieldEditor(CorePreferences.Pref.learnedBehaviourFile.name(),
		// "Learned-behaviour file:", learning.getFieldEditorParent()));
		// learning.setFieldEditors(learningFields);
		// addField(learning);
	}
}