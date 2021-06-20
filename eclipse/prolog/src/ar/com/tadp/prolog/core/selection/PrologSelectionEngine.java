/*****************************************************************************
 * This file is part of the Prolog Development Tools (ProDT)
 *
 * Author: Claudio Cancinos
 * WWW: https://sourceforge.net/projects/prodevtools
 * Copyright (C): 2008, Claudio Cancinos
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; If not, see <http://www.gnu.org/licenses/>
 ****************************************************************************/
package ar.com.tadp.prolog.core.selection;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.Expression;
import org.eclipse.dltk.ast.references.VariableReference;
import org.eclipse.dltk.codeassist.ISelectionEngine;
import org.eclipse.dltk.codeassist.ISelectionRequestor;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceParserUtil;

/**
 * @author ccancino
 *
 */
public class PrologSelectionEngine implements ISelectionEngine {
	private ISourceModule sourceModule;

	@Override
	public IModelElement[] select(final IModuleSource module, final int offset, final int i) {
		this.sourceModule = (ISourceModule) module.getModelElement();
		// TODO see why this is returning null, maybe because the lack of
		// PrologSourceParser implementation
		final ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(this.sourceModule, null);
		final List<IModelElement> results = new LinkedList<IModelElement>();

		try {
			moduleDeclaration.traverse(new ASTVisitor() {
				@Override
				public boolean visit(final Expression s) throws Exception {
					if (s.sourceStart() <= offset && offset <= s.sourceEnd()) {
						// if (s instanceof ExtendedVariableReference) {
						// ExtendedVariableReference ref =
						// (ExtendedVariableReference) s;
						// int count = ref.getExpressionCount();
						// for (int j = 0; j < count; j++) {
						// Expression e = ref.getExpression(j);
						// if (e.sourceStart() <= offset
						// && offset <= e.sourceEnd()) {
						// if (e instanceof VariableReference) {
						// findDeclaration(((VariableReference) e).getName(),
						// results);
						// }
						// }
						// }
						// } else {
						if (s instanceof VariableReference) {
							findDeclaration(((VariableReference) s).getName(), results);
						}
						// }
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
		return results.toArray(new IModelElement[results.size()]);
	}

	private void findDeclaration(final String name, final List<IModelElement> results) {
		try {
			this.sourceModule.accept(new IModelElementVisitor() {
				@Override
				public boolean visit(final IModelElement element) {
					if (element.getElementName().equals(name)) {
						results.add(element);
					}
					return true;
				}
			});
		} catch (final ModelException e) {
			DLTKCore.error(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setOptions(final Map options) {
	}

	@Override
	public void setRequestor(final ISelectionRequestor arg0) {
	}

}
