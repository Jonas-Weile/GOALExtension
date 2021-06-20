package org.eclipse.gdt.launch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.Messages;
import org.eclipse.gdt.Metrics;
import org.eclipse.gdt.Metrics.Event;
import org.eclipse.gdt.launching.DLTKRunnableProcess;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

import goal.preferences.Preferences;
import goal.tools.eclipse.RunTool;

public class GoalRunnableProcess extends DLTKRunnableProcess {
	private Process process;

	public GoalRunnableProcess(final IInterpreterInstall install, final ILaunch launch,
			final InterpreterConfig config) {
		super(install, launch, config);
	}

	@Override
	public void run() {
		try {
			final IPath ipath = getPath();
			final IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(ipath);
			final IPath path = getRunnable(ifile);
			if (path == null) {
				err("This cannot be run by GOAL; please select a valid GOAL file");
				return;
			}

			final String[] command = new String[] { "java", "-cp", Activator.getDefault().getJARpath(),
					RunTool.class.getName(), Preferences.getSettingsFile().getCanonicalPath(), path.toOSString() };
			if (Messages.TestFileExtension.equalsIgnoreCase(path.getFileExtension())) {
				Metrics.event(Event.RUN_TEST);
			} else {
				Metrics.event(Event.RUN_MAS);
			}
			this.process = getProcess(command, path.toFile().getParentFile(), false);
			out(this.process.getInputStream());
			err(this.process.getErrorStream());
			this.process.waitFor();
		} catch (final Exception e) {
			err(e);
		} finally {
			Metrics.event(Event.END);
			destroy();
		}
	}

	@Override
	public void destroy() {
		if (this.process != null) {
			this.process.destroy();
			this.process = null;
		}
		super.destroy();
	}

	public static Map<String, IPath> getRunnables(final Object element) throws Exception {
		final Map<String, IPath> runnables = new LinkedHashMap<>();
		if (element instanceof ISourceModule) {
			final ISourceModule source = (ISourceModule) element;
			final IPath path = source.getPath();
			if (Messages.MASFileExtension.equalsIgnoreCase(path.getFileExtension())
					|| Messages.TestFileExtension.equalsIgnoreCase(path.getFileExtension())) {
				runnables.put(path.lastSegment(), path);
			} else if (source.getParent() != null) {
				runnables.putAll(getRunnables(source.getParent()));
			}
		} else if (element instanceof IFile) {
			final IFile file = (IFile) element;
			final IPath path = file.getLocation();
			if (Messages.MASFileExtension.equalsIgnoreCase(file.getFileExtension())
					|| Messages.TestFileExtension.equalsIgnoreCase(file.getFileExtension())) {
				runnables.put(path.lastSegment(), path);
			} else if (file.getParent() != null) {
				runnables.putAll(getRunnables(file.getParent()));
			}
		} else if (element instanceof IScriptFolder) {
			final IScriptFolder folder = (IScriptFolder) element;
			for (final ISourceModule source : folder.getSourceModules()) {
				final IPath path = source.getPath();
				if (Messages.MASFileExtension.equalsIgnoreCase(path.getFileExtension())
						|| Messages.TestFileExtension.equalsIgnoreCase(path.getFileExtension())) {
					runnables.put(path.lastSegment(), path);
				}
			}
		} else if (element instanceof IFolder) {
			final IFolder folder = (IFolder) element;
			for (final IResource source : folder.members()) {
				final IPath path = source.getLocation();
				if (Messages.MASFileExtension.equalsIgnoreCase(path.getFileExtension())
						|| Messages.TestFileExtension.equalsIgnoreCase(path.getFileExtension())) {
					runnables.put(path.lastSegment(), path);
				} else if (folder.getParent() != null) {
					runnables.putAll(getRunnables(folder.getParent()));
				}
			}
		} else if (element instanceof IProject || element instanceof IScriptProject) {
			IProject project = null;
			if (element instanceof IScriptProject) {
				project = ((IScriptProject) element).getProject();
			} else {
				project = (IProject) element;
			}
			for (final IResource file : project.members()) {
				if (Messages.MASFileExtension.equalsIgnoreCase(file.getFileExtension())
						|| Messages.TestFileExtension.equalsIgnoreCase(file.getFileExtension())) {
					runnables.put(file.getName(), file.getLocation());
				}
			}
		}
		return runnables;
	}

	public static IPath getRunnable(final Object element) throws Exception {
		final Map<String, IPath> runnables = getRunnables(element);
		if (runnables.size() == 1) {
			return runnables.values().iterator().next();
		} else if (runnables.size() > 1) {
			final List<Object> result = new ArrayList<>(1);
			Display.getDefault().syncExec(() -> {
				final ListDialog ld = new ListDialog(new Shell());
				ld.setAddCancelButton(true);
				ld.setBlockOnOpen(true);
				ld.setContentProvider(new ArrayContentProvider());
				ld.setLabelProvider(new LabelProvider());
				ld.setInput(runnables.keySet());
				ld.setTitle("Select a file to execute");
				ld.open();
				if (ld.getResult() != null && ld.getResult().length > 0) {
					result.add(ld.getResult()[0]);
				}
			});
			if (result.isEmpty()) {
				return null;
			} else {
				return runnables.get(result.get(0));
			}
		} else {
			return null;
		}
	}

	public static Process getProcess(final String[] command, final File runin, final boolean redirectErrors)
			throws IOException {
		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(runin);
		if (redirectErrors) {
			builder.redirectErrorStream(true);
		}
		return builder.start();
	}
}
