package org.eclipse.gdt.prefs;

import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.dltk.debug.core.DLTKDebugPreferenceConstants;
import org.eclipse.dltk.ui.CodeFormatterConstants;
import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.debug.GoalDebugConstants;
import org.eclipse.gdt.editor.IGoalColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.EditorsUI;

import events.Channel;
import events.Channel.ChannelState;
import goal.preferences.CorePreferences;
import goal.preferences.DBExportPreferences;
import goal.preferences.DebugPreferences;
import goal.preferences.LoggingPreferences;
import goal.preferences.Preferences;
import goal.preferences.ProfilerPreferences;
import goal.tools.profiler.InfoType;

public class GoalPreferenceInitializer extends AbstractPreferenceInitializer {
	public static String LOG = "LOG_";
	public static String STEP = "STEP_";

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		EditorsUI.useAnnotationsPreferencePage(store);
		EditorsUI.useQuickDiffPreferencePage(store);

		PreferenceConstants.initializeDefaultValues(store);

		PreferenceConverter.setDefault(store, IGoalColorConstants.GOAL_COMMENT, new RGB(63, 127, 95));
		PreferenceConverter.setDefault(store, IGoalColorConstants.GOAL_STRING, new RGB(42, 0, 255));
		PreferenceConverter.setDefault(store, IGoalColorConstants.GOAL_KEYWORD, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, IGoalColorConstants.GOAL_OPERATOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, IGoalColorConstants.GOAL_DECLARATION, new RGB(0, 0, 0));

		store.setDefault(IGoalColorConstants.GOAL_COMMENT + PreferenceConstants.EDITOR_BOLD_SUFFIX, false);
		store.setDefault(IGoalColorConstants.GOAL_COMMENT + PreferenceConstants.EDITOR_ITALIC_SUFFIX, false);

		store.setDefault(IGoalColorConstants.GOAL_STRING + PreferenceConstants.EDITOR_BOLD_SUFFIX, false);
		store.setDefault(IGoalColorConstants.GOAL_STRING + PreferenceConstants.EDITOR_ITALIC_SUFFIX, false);

		store.setDefault(IGoalColorConstants.GOAL_KEYWORD + PreferenceConstants.EDITOR_BOLD_SUFFIX, true);
		store.setDefault(IGoalColorConstants.GOAL_KEYWORD + PreferenceConstants.EDITOR_ITALIC_SUFFIX, true);

		store.setDefault(IGoalColorConstants.GOAL_OPERATOR + PreferenceConstants.EDITOR_BOLD_SUFFIX, false);
		store.setDefault(IGoalColorConstants.GOAL_OPERATOR + PreferenceConstants.EDITOR_ITALIC_SUFFIX, false);

		store.setDefault(IGoalColorConstants.GOAL_DECLARATION + PreferenceConstants.EDITOR_BOLD_SUFFIX, true);
		store.setDefault(IGoalColorConstants.GOAL_DECLARATION + PreferenceConstants.EDITOR_ITALIC_SUFFIX, false);

		store.setDefault(PreferenceConstants.EDITOR_TAB_WIDTH, 4);
		store.setDefault(PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE, true);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);

		store.setDefault(CodeFormatterConstants.FORMATTER_TAB_CHAR, CodeFormatterConstants.TAB);
		store.setDefault(CodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		store.setDefault(CodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "4");

		store.setDefault(IGoalColorConstants.EDITOR_MATCHING_BRACKETS, true);
		PreferenceConverter.setDefault(store, IGoalColorConstants.EDITOR_MATCHING_BRACKETS_COLOR,
				new RGB(192, 192, 192));

		store.setDefault(GoalDebugConstants.DEBUGGING_ENGINE_ID_KEY, Activator.PLUGIN_ID);
		store.setDefault(DLTKDebugPreferenceConstants.PREF_DBGP_BREAK_ON_FIRST_LINE, false);
		store.setDefault(DLTKDebugPreferenceConstants.PREF_DBGP_ENABLE_LOGGING, false);
		store.setDefault(DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_GLOBAL, true);
		store.setDefault(DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_CLASS, true);
		store.setDefault(DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_LOCAL, true);

		store.setDefault(Activator.PERMISSION, "");

		loadGoalPrefs(store);
	}

	public static void loadGoalPrefs(final IPreferenceStore store) {
		Preferences.initializeAllPrefs();
		loadGoalPrefs(store, CorePreferences.getPrefs());
		loadGoalPrefs(store, DBExportPreferences.getPrefs());
		for (final Map.Entry<String, Object> entry : DebugPreferences.getPrefs().entrySet()) {
			final ChannelState value = ChannelState.valueOf((String) entry.getValue());
			store.setDefault(STEP + entry.getKey(), value.shouldPause());
			store.setDefault(LOG + entry.getKey(), value.canView());
		}
		loadGoalPrefs(store, LoggingPreferences.getPrefs());
		loadGoalPrefs(store, ProfilerPreferences.getPrefs());
	}

	public static void resetGoalPrefs(final IPreferenceStore store) {
		Preferences.resetToDefaultPreferences();
		loadGoalPrefs(store);
	}

	public static void saveGoalPrefs(final IPreferenceStore store) {
		CorePreferences.initPrefs(saveGoalPrefs(store, EnumSet.allOf(CorePreferences.Pref.class)));
		DBExportPreferences.initPrefs(saveGoalPrefs(store, EnumSet.allOf(DBExportPreferences.Pref.class)));
		DebugPreferences.initPrefs(saveGoalPrefs(store, EnumSet.allOf(Channel.class)));
		LoggingPreferences.initPrefs(saveGoalPrefs(store, EnumSet.allOf(LoggingPreferences.Pref.class)));
		Map<String, Object> profiling = saveGoalPrefs(store, EnumSet.allOf(ProfilerPreferences.Pref.class));
		profiling.putAll(saveGoalPrefs(store, EnumSet.allOf(InfoType.class)));
		ProfilerPreferences.initPrefs(profiling);
		Preferences.persistAllPrefs();
	}

	private static void loadGoalPrefs(final IPreferenceStore store, final Map<String, Object> prefs) {
		for (final Map.Entry<String, Object> entry : prefs.entrySet()) {
			if (entry.getValue() instanceof Boolean) {
				store.setDefault(entry.getKey(), (Boolean) entry.getValue());
			} else if (entry.getValue() instanceof Integer) {
				store.setDefault(entry.getKey(), (Integer) entry.getValue());
			} else if (entry.getValue() instanceof String) {
				store.setDefault(entry.getKey(), (String) entry.getValue());
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static Map<String, Object> saveGoalPrefs(final IPreferenceStore store, final EnumSet prefs) {
		final Map<String, Object> returned = new TreeMap<>();
		for (final Object rawKey : prefs) {
			final String key = rawKey.toString();
			if (rawKey instanceof Channel) {
				final boolean step = store.getBoolean(STEP + key);
				final boolean log = store.getBoolean(LOG + key);
				if (step && log) {
					returned.put(key, ChannelState.VIEWPAUSE.name());
				} else if (step) {
					returned.put(key, ChannelState.PAUSE.name());
				} else if (log) {
					returned.put(key, ChannelState.VIEW.name());
				} else {
					returned.put(key, ChannelState.NONE.name());
				}
			} else {
				final String base = store.getString(key);
				if (key == null || base == null) {
					continue;
				} else if (base.equals("true")) {
					returned.put(key, true);
				} else if (base.equals("false")) {
					returned.put(key, false);
				} else {
					try {
						returned.put(key, Integer.valueOf(base));
					} catch (final Exception e) {
						returned.put(key, base);
					}
				}
			}
		}
		return returned;
	}
}