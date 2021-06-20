package org.eclipse.gdt.ui;

import java.util.List;

import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelProvider;

public class MASModelProvider implements IModelProvider {
	/*
	 * private final Map<String, ISourceModule> removed = new
	 * HashMap<String,ISourceModule>();
	 */

	@Override
	public void provideModelChanges(final IModelElement parentElement, final List<IModelElement> children) {
		/*
		 * if (parentElement instanceof IProject || parentElement instanceof
		 * IScriptFolder) { final List<IModelElement> newChildren = new
		 * LinkedList<IModelElement>(); for (final IModelElement child :
		 * children) { if (child instanceof ISourceModule) { final ISourceModule
		 * module = (ISourceModule) child; if (module.getElementName().endsWith(
		 * Messages.MASFileExtension)) { newChildren.add(child); } else {
		 * this.removed.put(module.getPath().toOSString(), module); } } }
		 * children.clear(); children.addAll(newChildren); } else if
		 * (parentElement instanceof ISourceModule) { final ISourceModule module
		 * = (ISourceModule) parentElement; final ParsedObject parsed =
		 * GoalSourceParser.fromCache(module); final String[] paths =
		 * this.removed.keySet().toArray( new String[this.removed.size()]); if
		 * (parsed instanceof MASProgram) { final MASProgram mas = (MASProgram)
		 * parsed; for (final String path : paths) { final ISourceModule temp =
		 * this.removed.get(path); for (final File agent : mas.getAgentPaths())
		 * { if (agent != null && agent.getPath().endsWith(path)) {
		 * children.add(temp); break; } } } } else if (parsed instanceof
		 * GOALProgram) { final GOALProgram goal = (GOALProgram) parsed; for
		 * (final String path : paths) { final ISourceModule temp =
		 * this.removed.get(path); for (final File imported : goal.getImports())
		 * { if (imported.getPath().endsWith(path)) { children.add(temp); break;
		 * } } } } } // TODO: handle files not belonging to another file?
		 */
	}

	@Override
	public boolean isModelChangesProvidedFor(final IModelElement modelElement, final String name) {
		return false;
		/*
		 * return modelElement instanceof IProject || modelElement instanceof
		 * IScriptFolder || modelElement instanceof ISourceModule;
		 */
	}
}