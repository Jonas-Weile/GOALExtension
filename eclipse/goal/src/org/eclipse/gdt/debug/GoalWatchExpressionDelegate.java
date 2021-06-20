package org.eclipse.gdt.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.gdt.debug.dbgp.DebuggerCollection;

import languageTools.program.agent.AgentId;

public class GoalWatchExpressionDelegate implements IWatchExpressionDelegate {

	@Override
	public void evaluateExpression(final String expression, final IDebugElement context,
			final IWatchExpressionListener listener) {
		final List<String> errors = new ArrayList<String>(1);
		String tempvalue = "";
		try {
			final IProject project = context.getLaunch().getLaunchConfiguration().getMappedResources()[0].getProject();
			final DebuggerCollection debugger = DebuggerCollection.getCollection(project.getName());
			for (AgentId agent : debugger.getAgents()) {
				tempvalue += debugger.getMainDebugger().evaluate(agent, expression) + " ";
			}
		} catch (final Exception e) {
			errors.add(e.getMessage());
		}
		final String value = tempvalue;
		listener.watchEvaluationFinished(new IWatchExpressionResult() {
			@Override
			public boolean hasErrors() {
				return !errors.isEmpty();
			}

			@Override
			public IValue getValue() {
				return new IValue() {
					@SuppressWarnings({ "rawtypes", "unchecked" })
					@Override
					public Object getAdapter(final Class adapter) {
						return null;
					}

					@Override
					public String getModelIdentifier() {
						return GoalDebugConstants.DEBUG_MODEL_ID;
					}

					@Override
					public ILaunch getLaunch() {
						return context.getLaunch();
					}

					@Override
					public IDebugTarget getDebugTarget() {
						return context.getDebugTarget();
					}

					@Override
					public boolean isAllocated() throws DebugException {
						return false;
					}

					@Override
					public boolean hasVariables() throws DebugException {
						return false;
					}

					@Override
					public IVariable[] getVariables() throws DebugException {
						return new IVariable[0];
					}

					@Override
					public String getValueString() throws DebugException {
						return value;
					}

					@Override
					public String getReferenceTypeName() throws DebugException {
						return "";
					}

					@Override
					public String toString() {
						try {
							return getValueString();
						} catch (final Exception e) {
							return e.getMessage();
						}
					}
				};
			}

			@Override
			public String getExpressionText() {
				return expression;
			}

			@Override
			public DebugException getException() {
				return null;
			}

			@Override
			public String[] getErrorMessages() {
				return errors.toArray(new String[errors.size()]);
			}
		});
	}
}
