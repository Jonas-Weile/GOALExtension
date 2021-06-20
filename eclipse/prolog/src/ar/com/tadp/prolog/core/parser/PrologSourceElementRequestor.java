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
package ar.com.tadp.prolog.core.parser;

import org.eclipse.dltk.ast.declarations.FieldDeclaration;
import org.eclipse.dltk.ast.statements.Statement;
import org.eclipse.dltk.compiler.IElementRequestor.FieldInfo;
import org.eclipse.dltk.compiler.ISourceElementRequestor;
import org.eclipse.dltk.compiler.SourceElementRequestVisitor;

/**
 * @author ccancino
 *
 */
public class PrologSourceElementRequestor extends SourceElementRequestVisitor {

	public PrologSourceElementRequestor(final ISourceElementRequestor requesor) {
		super(requesor);
	}

	@Override
	public boolean visit(final Statement statement) throws Exception {
		if (statement instanceof FieldDeclaration) {
			return visit((FieldDeclaration) statement);
		}
		return super.visit(statement);
	}

	@Override
	public boolean endvisit(final Statement statement) throws Exception {
		if (statement instanceof FieldDeclaration) {
			return endvisit((FieldDeclaration) statement);
		}
		return super.endvisit(statement);
	}

	public boolean visit(final FieldDeclaration field) throws Exception {
		this.fNodes.push(field);

		final FieldInfo fieldInfo = new ISourceElementRequestor.FieldInfo();
		fieldInfo.declarationStart = field.sourceStart();
		fieldInfo.name = field.getName();
		fieldInfo.nameSourceStart = field.getNameStart();
		fieldInfo.nameSourceEnd = field.getNameEnd();
		fieldInfo.modifiers = field.getModifiers();

		this.fRequestor.enterField(fieldInfo);
		return true;
	}

	public boolean endvisit(final FieldDeclaration field) throws Exception {
		this.fRequestor.exitField(field.sourceEnd());
		this.fNodes.pop();
		return true;
	}
}
