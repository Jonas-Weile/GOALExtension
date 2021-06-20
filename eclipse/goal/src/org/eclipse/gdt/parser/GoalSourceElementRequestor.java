package org.eclipse.gdt.parser;

import java.util.List;

import org.eclipse.dltk.ast.declarations.Argument;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.compiler.ISourceElementRequestor;
import org.eclipse.dltk.compiler.SourceElementRequestVisitor;

@SuppressWarnings("rawtypes")
public class GoalSourceElementRequestor extends SourceElementRequestVisitor {

	public GoalSourceElementRequestor(final ISourceElementRequestor requestor) {
		super(requestor);
	}

	@Override
	public boolean visit(final MethodDeclaration method) throws Exception {
		this.fNodes.push(method);
		final List args = method.getArguments();
		final String[] parameter = new String[args.size()];
		for (int a = 0; a < args.size(); a++) {
			final Argument arg = (Argument) args.get(a);
			parameter[a] = arg.getName();
		}

		final ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
		mi.parameterNames = parameter;
		mi.name = method.getName();
		mi.modifiers = method.getModifiers();
		mi.nameSourceStart = method.getNameStart();
		mi.nameSourceEnd = method.getNameEnd() - 1;
		mi.declarationStart = method.sourceStart();

		((ISourceElementRequestor) this.fRequestor).enterMethod(mi);

		this.fInMethod = true;
		this.fCurrentMethod = method;
		return true;
	}
}