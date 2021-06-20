package org.eclipse.gdt.search;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.Declaration;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.parser.IModuleDeclaration;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.search.ModelElementOccurrencesFinder;

public class GoalOccurrencesFinder extends ModelElementOccurrencesFinder {
	private ModuleDeclaration moduleDeclaration;
	private Declaration declaration;

	@Override
	public String initialize(final ISourceModule module, final IModuleDeclaration root, final int offset,
			final int length) {
		this.declaration = null;
		this.moduleDeclaration = (ModuleDeclaration) root;
		try {
			this.moduleDeclaration.traverse(new ASTVisitor() {
				@Override
				public boolean visit(final MethodDeclaration s) throws Exception {
					if (s.getNameStart() <= offset && offset <= s.getNameEnd()) {
						GoalOccurrencesFinder.this.declaration = s;
					}
					return super.visit(s);
				}

				@Override
				public boolean visit(final TypeDeclaration s) throws Exception {
					if (s.getNameStart() <= offset && offset <= s.getNameEnd()) {
						GoalOccurrencesFinder.this.declaration = s;
					}
					return super.visit(s);
				}

			});
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
		return null;
	}

	@Override
	public OccurrenceLocation[] getOccurrences() {
		final List<OccurrenceLocation> list = new LinkedList<OccurrenceLocation>();
		try {
			this.moduleDeclaration.traverse(new ASTVisitor() {
				@Override
				public boolean visit(final MethodDeclaration s) throws Exception {
					if (GoalOccurrencesFinder.this.declaration.getName().equals(s.getName())) {
						list.add(new OccurrenceLocation(s.sourceStart(), s.matchLength(), s.getName()));
					}
					return super.visit(s);
				}

				@Override
				public boolean visit(final TypeDeclaration s) throws Exception {
					if (GoalOccurrencesFinder.this.declaration.getName().equals(s.getName())) {
						list.add(new OccurrenceLocation(s.sourceStart(), s.matchLength(), s.getName()));
					}
					return super.visit(s);
				}

			});
		} catch (final Exception ignore) {
		}
		return list.toArray(new OccurrenceLocation[list.size()]);
	}
}