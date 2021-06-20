package org.eclipse.gdt;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.gdt.debug.GoalLineBreakpoint;
import org.eclipse.gdt.debug.dbgp.DebuggerCollection;
import org.eclipse.gdt.debug.history.BackCommand;
import org.eclipse.gdt.debug.history.BackCommandHandler.IBackHandler;
import org.eclipse.gdt.debug.history.ExplanationCommand;
import org.eclipse.gdt.debug.history.ExplanationCommandHandler.IExplanationHandler;
import org.eclipse.gdt.debug.history.ForwardCommand;
import org.eclipse.gdt.debug.history.ForwardCommandHandler.IForwardHandler;
import org.eclipse.gdt.debug.history.LookupCommand;
import org.eclipse.gdt.debug.history.LookupCommandHandler.ILookupHandler;
import org.eclipse.gdt.editor.ComboToolbar;
import org.eclipse.gdt.editor.GoalTextTools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import goal.preferences.DebugPreferences;
import goal.preferences.Preferences;
import goal.tools.Run;
import goal.tools.eclipse.DebugCommand;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IBreakpointListener, IPartListener2 {
	public static final String PLUGIN_ID = "org.eclipse.gdt";
	public static String PERMISSION = "II_TUDELFT_GOAL";
	private static final String agentVersion = "2.2.0";
	private static final BackCommand fgBackCommand = new BackCommand();
	private static final ForwardCommand fgForwardCommand = new ForwardCommand();
	private static final LookupCommand fgLookupCommand = new LookupCommand();
	private static final ExplanationCommand fgExplanationCommand = new ExplanationCommand();
	private static Activator plugin;
	private String JARpath;
	private String Agentpath;
	private GoalTextTools fGoalTextTools;
	private ComboToolbar toolbar;
	private Job build;

	public Activator() {
	}

	public String getJARpath() {
		return this.JARpath;
	}

	public String getAgentPath() {
		return this.Agentpath;
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		String JARpathT = "";
		try {
			final URL found1 = DebugCommand.class.getProtectionDomain().getCodeSource().getLocation();
			final String found2 = FileLocator.resolve(found1).toString().replaceAll("\\s", "%20");
			final URI found3 = new URI(found2);
			JARpathT = new File(found3).getCanonicalPath();
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
		this.JARpath = JARpathT;
		String AgentpathT = "";
		try { // Locate required libraries
			final URL found1 = FileLocator.find(getDefault().getBundle(), new Path("lib"), null);
			final String found2 = FileLocator.resolve(found1).toString().replaceAll("\\s", "%20");
			final URI found3 = new URI(found2);
			AgentpathT = new File(found3).getCanonicalPath();
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
		this.Agentpath = AgentpathT + File.separator + "agents-" + agentVersion + "-bin.zip";

		final IPath prefs = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(".goalprefs");
		DebugPreferences.setDefault(Run.getDefaultPrefs());
		Preferences.changeSettingsFile(prefs.toFile());

		Display.getDefault().asyncExec(() -> {
			DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(Activator.this);
			final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			window.getActivePage().addPartListener(Activator.this);
			if (window.getPartService().getActivePartReference() != null) {
				partActivated(window.getPartService().getActivePartReference());
			}
			// if (getPreferenceStore().getString(PERMISSION).isEmpty()) {
			// final boolean yesno = MessageDialog.openQuestion(window.getShell(),
			// "Consent for Participation in Research",
			// "I hereby volunteer to participate in a research project conducted by the
			// Interactive Intelligence Group at the Delft University of Technology that
			// aims to gather information about the use of the GOAL plug-in for Eclipse. If
			// I decline to participate, no one will be informed. Participation involves
			// automatic anomyous data gathering and transmission to the researchers. For
			// more information contact v.j.koeman@tudelft.nl");
			// getPreferenceStore().setValue(PERMISSION, yesno ?
			// UUID.randomUUID().toString() : "N");
			// }
			// new Metrics.MetricPersistJob().schedule(60000);
		});

		final IAdapterFactory factory = new IAdapterFactory() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> T getAdapter(final Object adaptableObject, final Class<T> adapterType) {
				if (IForwardHandler.class.equals(adapterType)) {
					return (T) fgForwardCommand;
				} else if (IBackHandler.class.equals(adapterType)) {
					return (T) fgBackCommand;
				} else if (ILookupHandler.class.equals(adapterType)) {
					return (T) fgLookupCommand;
				} else if (IExplanationHandler.class.equals(adapterType)) {
					return (T) fgExplanationCommand;
				} else {
					return null;
				}
			}

			@Override
			public Class<?>[] getAdapterList() {
				return new Class[] { IBackHandler.class, IForwardHandler.class, ILookupHandler.class,
						IExplanationHandler.class };
			}
		};
		Platform.getAdapterManager().registerAdapters(factory, IStep.class);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public GoalTextTools getTextTools() {
		if (this.fGoalTextTools == null) {
			this.fGoalTextTools = new GoalTextTools(true);
		}
		return this.fGoalTextTools;
	}

	public static Image getImage(final String imagePath) {
		final ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
				imagePath);
		return imageDescriptor.createImage();
	}

	public void setActiveToolbar(final ComboToolbar toolbar) {
		this.toolbar = toolbar;
	}

	public ComboToolbar getActiveToolbar() {
		return this.toolbar;
	}

	@Override
	public void breakpointAdded(final IBreakpoint ibreakpoint) {
		if (ibreakpoint instanceof GoalLineBreakpoint) {
			final GoalLineBreakpoint breakpoint = (GoalLineBreakpoint) ibreakpoint;
			final DebuggerCollection collection = DebuggerCollection
					.getCollection(breakpoint.getMarker().getResource().getProject().getName());
			if (collection != null && collection.getMainDebugger() != null) {
				collection.getMainDebugger().updateBreakpoints();
			}
		}
	}

	@Override
	public void breakpointRemoved(final IBreakpoint ibreakpoint, final IMarkerDelta odelta) {
		breakpointAdded(ibreakpoint);
	}

	@Override
	public void breakpointChanged(final IBreakpoint obreakpoint, final IMarkerDelta odelta) {
	}

	@Override
	public void partActivated(final IWorkbenchPartReference partRef) {
		if (partRef.getPage().getActiveEditor() != null) {
			try {
				final IEditorInput editor = partRef.getPage().getActiveEditor().getEditorInput();
				final IProject project = (editor == null) ? null : editor.getAdapter(IResource.class).getProject();
				if (project != null && project.isAccessible() && project.hasNature(GoalNature.GOAL_NATURE)) {
					if (Activator.this.toolbar != null) {
						Activator.this.toolbar.resolveMAS(editor);
					}
					if (Activator.this.build != null) {
						Activator.this.build.cancel();
					}
					Activator.this.build = Job.create("Building " + project.getName() + "...",
							(ICoreRunnable) monitor -> project.build(IncrementalProjectBuilder.FULL_BUILD, null));
					Activator.this.build.setPriority(Job.BUILD);
					Activator.this.build.schedule();
				}
			} catch (final Exception e) {
				DLTKCore.error(e);
			}
		}
	}

	@Override
	public void partBroughtToTop(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partDeactivated(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partOpened(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partHidden(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partVisible(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(final IWorkbenchPartReference partRef) {
	}
}