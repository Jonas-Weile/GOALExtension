package org.eclipse.gdt.launch;

import org.eclipse.dltk.core.DLTKIdContributionSelector;
import org.eclipse.dltk.core.PreferencesLookupDelegate;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.debug.GoalDebugConstants;

public class GoalDebuggingEngineSelector extends DLTKIdContributionSelector {

	@Override
	protected String getSavedContributionId(final PreferencesLookupDelegate delegate) {
		return delegate.getString(Activator.PLUGIN_ID, GoalDebugConstants.DEBUGGING_ENGINE_ID_KEY);
	}
}