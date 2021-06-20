package org.eclipse.gdt.parser;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.ast.parser.AbstractSourceParser;
import org.eclipse.dltk.ast.parser.IModuleDeclaration;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.compiler.problem.ProblemSeverity;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.gdt.Messages;

import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.actionspec.ActionSpecValidator;
import languageTools.analyzer.planner.PlannerValidator;
import languageTools.analyzer.mas.MASValidator;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.analyzer.test.TestValidator;
import languageTools.errors.Message;
import languageTools.program.Program;
import languageTools.program.ProgramMap;
import languageTools.program.test.TestProgram;

public class GoalSourceParser extends AbstractSourceParser {
	private final static Map<IPath, ProgramMap> maps = new HashMap<>();
	private static Map.Entry<IPath, String> content = new AbstractMap.SimpleEntry<>(null, null);
	private static IModuleDeclaration last = null;

	public static ProgramMap getMap(final IPath path) {
		ProgramMap returned = maps.get(path);
		if (Messages.MASFileExtension.equals(path.getFileExtension())) {
			if (returned == null) {
				final FileRegistry registry = new FileRegistry();
				final MASValidator visitor = new MASValidator(path.toOSString(), registry);
				visitor.validate();
				visitor.process();
				returned = new ProgramMap();
				returned.register(visitor.getProgram());
				for (final File source : registry.getSourceFiles()) {
					Program sub = registry.getProgram(source);
					returned.merge(sub.getMap());
				}
				maps.put(path, returned);
			}
		} else if (Messages.TestFileExtension.equals(path.getFileExtension())) {
			if (returned == null) {
				final TestValidator visitor = new TestValidator(path.toOSString(), new FileRegistry());
				visitor.validate();
				final TestProgram test = visitor.getProgram();
				if (test != null) {
					try {
						final IPath mas = Path.fromOSString(test.getMAS().getSourceFile().getCanonicalPath());
						returned = getMap(mas);
					} catch (final Exception e) {
						DLTKCore.error(e);
					}
				}
			}
		}
		return returned;
	}

	@Override
	public IModuleDeclaration parse(final IModuleSource source, final IProblemReporter reporter) {
		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(source.getFileName()));
		if (file != null && file.exists()) {
			try {
				if (file.getLocation().equals(content.getKey())
						&& source.getSourceContents().equals(content.getValue())) {
					return last;
				} else {
					content = new AbstractMap.SimpleEntry<>(file.getLocation(), source.getSourceContents());
					file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
					if (Messages.MASFileExtension.equalsIgnoreCase(file.getFileExtension())) {
						last = parseMAS2G(file);
					} else if (Messages.ModuleFileExtension.equalsIgnoreCase(file.getFileExtension())) {
						last = parseMOD2G(file);
					} else if (Messages.TestFileExtension.equalsIgnoreCase(file.getFileExtension())) {
						last = parseTEST2G(file);
					} else if (Messages.PlannerFileExtension.equalsIgnoreCase(file.getFileExtension())) {
						last = parsePLAN2G(file);
					} else if (Messages.ActionFileExtension.equalsIgnoreCase(file.getFileExtension())) {
						last = parseACT2G(file);
					} else {
						last = null;
					}
					return last;
				}
			} catch (final Exception e) {
				DLTKCore.error(e);
			}
		}
		return null;
	}

	private IModuleDeclaration parseMAS2G(final IFile ifile) throws Exception {
		final FileRegistry registry = new FileRegistry();
		final MASValidator visitor = new MASValidator(ifile.getLocation().toOSString(), registry);
		if (ifile.getLocation().equals(content.getKey())) {
			visitor.override(content.getValue());
		}
		visitor.validate();
		markProblems(ifile, registry);
		return null; // TODO: DLTK tree
	}

	private IModuleDeclaration parseMOD2G(final IFile ifile) throws Exception {
		final FileRegistry registry1 = new FileRegistry();
		final ModuleValidator visitor = new ModuleValidator(ifile.getLocation().toOSString(), registry1);
		if (ifile.getLocation().equals(content.getKey())) {
			visitor.override(content.getValue());
		}
		visitor.validate();
		markProblems(ifile, registry1);

		final FileRegistry registry2 = new FileRegistry();
		final ModuleWalker walker = new ModuleWalker(ifile.getLocation().toOSString(), registry2);
		if (ifile.getLocation().equals(content.getKey())) {
			walker.override(content.getValue());
		}
		walker.validate();
		markProblems(ifile, registry2);
		return walker.getDeclaration();
	}

	private IModuleDeclaration parseACT2G(final IFile ifile) throws Exception {
		final FileRegistry registry = new FileRegistry();
		final ActionSpecValidator visitor = new ActionSpecValidator(ifile.getLocation().toOSString(), registry);
		if (ifile.getLocation().equals(content.getKey())) {
			visitor.override(content.getValue());
		}
		visitor.validate();
		markProblems(ifile, registry);
		return null; // TODO: DLTK tree
	}
	
	private IModuleDeclaration parsePLAN2G(final IFile ifile) throws Exception {
		final FileRegistry registry = new FileRegistry();
		final PlannerValidator visitor = new PlannerValidator(ifile.getLocation().toOSString(), registry);
		if (ifile.getLocation().equals(content.getKey())) {
			visitor.override(content.getValue());
		}
		visitor.validate();
		markProblems(ifile, registry);
		return null; // TODO: DLTK tree
	}

	private IModuleDeclaration parseTEST2G(final IFile ifile) throws Exception {
		final FileRegistry registry = new FileRegistry();
		final TestValidator visitor = new TestValidator(ifile.getLocation().toOSString(), registry);
		if (ifile.getLocation().equals(content.getKey())) {
			visitor.override(content.getValue());
		}
		visitor.validate();
		markProblems(ifile, registry);
		return null; // TODO: DLTK tree
	}

	public static void markProblems(final IFile parent, final FileRegistry registry) {
		final IWorkspaceRunnable markers = new IWorkspaceRunnable() {
			private final Set<Message> reported = new HashSet<>();

			@Override
			public void run(final IProgressMonitor monitor) throws CoreException {
				for (final Message error : registry.getSyntaxErrors()) {
					if (this.reported.add(error)) {
						markProblem(parent, error.getSource(), error.toShortString(), ProblemSeverity.ERROR);
					}
				}
				for (final Message error : registry.getErrors()) {
					if (this.reported.add(error)) {
						markProblem(parent, error.getSource(), error.toShortString(), ProblemSeverity.ERROR);
					}
				}
				for (final Message warning : registry.getWarnings()) {
					if (this.reported.add(warning)) {
						markProblem(parent, warning.getSource(), warning.toShortString(), ProblemSeverity.WARNING);
					}
				}
			}
		};
		try {
			parent.getWorkspace().run(markers, null, IWorkspace.AVOID_UPDATE, null);
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
	}

	private static void markProblem(final IFile parent, final SourceInfo pos, final String msg,
			final ProblemSeverity severity) {
		if (pos == null || msg == null) {
			return;
		}
		IFile file = null;
		if (pos.getSource() != null) {
			file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(Path.fromOSString(pos.getSource()));
		}
		if (file == null || !file.exists()) {
			file = parent;
		}
		try {
			final IMarker marker = file.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.MESSAGE, msg);
			if (severity == null || severity.equals(ProblemSeverity.ERROR)) {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			} else if (severity.equals(ProblemSeverity.WARNING)) {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			} else if (severity.equals(ProblemSeverity.INFO)) {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			}
			marker.setAttribute(IMarker.LOCATION, file.getName() + " line " + pos.getLineNumber());
			marker.setAttribute(IMarker.LINE_NUMBER, pos.getLineNumber());
			marker.setAttribute(IMarker.CHAR_START, pos.getStartIndex());
			marker.setAttribute(IMarker.CHAR_END, pos.getStopIndex() + 1);
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
	}
}