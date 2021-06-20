package org.eclipse.gdt.search;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.Expression;
import org.eclipse.dltk.ast.references.VariableReference;
import org.eclipse.dltk.codeassist.ScriptSelectionEngine;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.SourceParserUtil;

public class GoalSelectionEngine extends ScriptSelectionEngine {
	private ISourceModule sourceModule;

	@Override
	public IModelElement[] select(final IModuleSource module, final int offset, final int i) {
		this.sourceModule = (org.eclipse.dltk.core.ISourceModule) module.getModelElement();
		final List<IModelElement> results = new LinkedList<IModelElement>();
		final ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(this.sourceModule, null);
		if (moduleDeclaration != null) {
			try {
				moduleDeclaration.traverse(new ASTVisitor() {
					@Override
					public boolean visit(final Expression s) throws Exception {
						if (s.sourceStart() <= offset && offset <= s.sourceEnd()) {
							if (s instanceof VariableReference) {
								findDeclaration(((VariableReference) s).getName(), results);
							}
						}
						return super.visit(s);
					}

					@Override
					public boolean visit(final MethodDeclaration s) throws Exception {
						if (s.getNameStart() <= offset && offset <= s.getNameEnd()) {
							findDeclaration(s.getName(), results);
						}
						return super.visit(s);
					}

					@Override
					public boolean visit(final TypeDeclaration s) throws Exception {
						if (s.getNameStart() <= offset && offset <= s.getNameEnd()) {
							findDeclaration(s.getName(), results);
						}
						return super.visit(s);
					}

				});
			} catch (final Exception e) {
				DLTKCore.error(e);
			}
		}
		return results.toArray(new IModelElement[results.size()]);
	}

	private void findDeclaration(final String name, final List<IModelElement> results) {
		try {
			this.sourceModule.accept(new IModelElementVisitor() {
				@Override
				public boolean visit(final IModelElement element) {
					if (element != null && name.equals(element.getElementName())) {
						results.add(element);
					}
					return true;
				}
			});
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
	}
}