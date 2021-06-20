package org.eclipse.gdt.debug;

import org.eclipse.dltk.debug.core.model.AtomicScriptType;
import org.eclipse.dltk.debug.core.model.IScriptType;
import org.eclipse.dltk.debug.core.model.IScriptTypeFactory;

public class GoalTypeFactory implements IScriptTypeFactory {

	public GoalTypeFactory() {
	}

	@Override
	public IScriptType buildType(final String type) {
		return new AtomicScriptType(type); // TODO?!
	}
}