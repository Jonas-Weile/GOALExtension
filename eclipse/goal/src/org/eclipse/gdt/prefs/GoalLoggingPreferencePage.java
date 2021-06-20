package org.eclipse.gdt.prefs;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;

import events.Channel;
import goal.preferences.LoggingPreferences;

public class GoalLoggingPreferencePage extends GoalPreferencePage {

	@Override
	protected void createFieldEditors() {
		final GroupFieldEditor logging = new GroupFieldEditor("Log Handling", getFieldEditorParent());
		final List<FieldEditor> loggingFields = new LinkedList<>();
		loggingFields.add(new BooleanFieldEditor(LoggingPreferences.Pref.showlogtime.name(), "Show log time",
				logging.getFieldEditorParent()));
		loggingFields.add(new BooleanFieldEditor(LoggingPreferences.Pref.logtofile.name(), "Write logs to files",
				logging.getFieldEditorParent()));
		loggingFields.add(new BooleanFieldEditor(LoggingPreferences.Pref.overwritelogfiles.name(),
				"Overwrite old log files", logging.getFieldEditorParent()));
		loggingFields.add(new BooleanFieldEditor(LoggingPreferences.Pref.logconsoles.name(),
				"Log the default consoles to file", logging.getFieldEditorParent()));
		loggingFields.add(new BooleanFieldEditor(LoggingPreferences.Pref.eclipseActionHistory.name(),
				"Show an action history whilst debugging", logging.getFieldEditorParent()));
		loggingFields.add(new BooleanFieldEditor(LoggingPreferences.Pref.eclipseAgentConsoles.name(),
				"Show a console per agent whilst debugging", logging.getFieldEditorParent()));
		loggingFields.add(new FileFieldEditor(LoggingPreferences.Pref.logdirectory.name(), "Logging directory:",
				logging.getFieldEditorParent()));
		logging.setFieldEditors(loggingFields);
		addField(logging);

		final GroupFieldEditor log = new GroupFieldEditor("Logging options", getFieldEditorParent());
		final List<FieldEditor> logFields = new LinkedList<>();
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.REASONING_CYCLE_SEPARATOR.name(),
				Channel.REASONING_CYCLE_SEPARATOR.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(LoggingPreferences.Pref.printStats.name(),
				"Include statistics each cycle separator", log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.SLEEP.name(),
				Channel.SLEEP.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.CALL_ACTION_OR_MODULE.name(),
				Channel.CALL_ACTION_OR_MODULE.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.MODULE_ENTRY.name(),
				Channel.MODULE_ENTRY.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.MODULE_EXIT.name(),
				Channel.MODULE_EXIT.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.RULE_CONDITION_EVALUATION.name(),
				Channel.RULE_CONDITION_EVALUATION.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.ACTION_PRECOND_EVALUATION.name(),
				Channel.ACTION_PRECOND_EVALUATION.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.ACTION_POSTCOND_EVALUATION.name(),
				Channel.ACTION_POSTCOND_EVALUATION.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.ACTION_EXECUTED_BUILTIN.name(),
				Channel.ACTION_EXECUTED_BUILTIN.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.ACTION_EXECUTED_MESSAGING.name(),
				Channel.ACTION_EXECUTED_MESSAGING.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.ACTION_EXECUTED_USERSPEC.name(),
				Channel.ACTION_EXECUTED_USERSPEC.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.BB_UPDATES.name(),
				Channel.BB_UPDATES.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.GB_UPDATES.name(),
				Channel.GB_UPDATES.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.GOAL_ACHIEVED.name(),
				Channel.GOAL_ACHIEVED.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.PERCEPTS_CONDITIONAL_VIEW.name(),
				Channel.PERCEPTS_CONDITIONAL_VIEW.getExplanation(), log.getFieldEditorParent()));
		logFields.add(new BooleanFieldEditor(GoalPreferenceInitializer.LOG + Channel.MAILS_CONDITIONAL_VIEW.name(),
				Channel.MAILS_CONDITIONAL_VIEW.getExplanation(), log.getFieldEditorParent()));
		log.setFieldEditors(logFields);
		addField(log);

		final GroupFieldEditor warnings = new GroupFieldEditor("Warnings", getFieldEditorParent());
		final List<FieldEditor> warningFields = new LinkedList<>();
		warningFields.add(new BooleanFieldEditor(LoggingPreferences.Pref.stackdump.name(),
				"Show stack traces with warnings", warnings.getFieldEditorParent()));
		warningFields.add(new BooleanFieldEditor(LoggingPreferences.Pref.eclipseDebug.name(),
				"Put Eclipse in verbose debug mode", warnings.getFieldEditorParent()));
		warnings.setFieldEditors(warningFields);
		addField(warnings);

		/*
		 * final GroupFieldEditor export = new GroupFieldEditor("Export",
		 * getFieldEditorParent()); final List<FieldEditor> exportFields = new
		 * LinkedList<FieldEditor>(); exportFields.add(new BooleanFieldEditor(
		 * DBExportPreferences.Pref.exportbeliefs.name(), "Export beliefbase",
		 * export.getFieldEditorParent())); exportFields.add(new BooleanFieldEditor(
		 * DBExportPreferences.Pref.exportpercepts.name(), "Include percepts",
		 * export.getFieldEditorParent())); exportFields.add(new BooleanFieldEditor(
		 * DBExportPreferences.Pref.exportmailbox.name(), "Include mails",
		 * export.getFieldEditorParent())); exportFields.add(new BooleanFieldEditor(
		 * DBExportPreferences.Pref.exportgoals.name(), "Export goalbase",
		 * export.getFieldEditorParent())); exportFields.add(new BooleanFieldEditor(
		 * DBExportPreferences.Pref.separatefiles.name(), "Export to separate files",
		 * export.getFieldEditorParent())); exportFields.add(new BooleanFieldEditor(
		 * DBExportPreferences.Pref.openaftersave.name(), "Open export after save",
		 * export.getFieldEditorParent())); exportFields.add(new BooleanFieldEditor(
		 * DBExportPreferences.Pref.rememberLastUsedExportDir.name(),
		 * "Remember last used export directory", export .getFieldEditorParent()));
		 * export.setFieldEditors(exportFields); addField(export);
		 */
	}
}