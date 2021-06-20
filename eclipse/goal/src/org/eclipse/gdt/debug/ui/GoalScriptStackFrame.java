package org.eclipse.gdt.debug.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.dltk.dbgp.IDbgpProperty;
import org.eclipse.dltk.dbgp.IDbgpStackLevel;
import org.eclipse.dltk.internal.debug.core.model.ScriptStackFrame;
import org.eclipse.dltk.internal.debug.core.model.ScriptVariable;
import org.eclipse.gdt.debug.dbgp.AgentState;
import org.eclipse.gdt.debug.dbgp.DebuggerCollection;
import org.eclipse.gdt.debug.dbgp.LocalDebugger;
import org.eclipse.gdt.debug.ui.GoalVariablesView.GoalVariableType;

import languageTools.program.agent.AgentId;

public class GoalScriptStackFrame extends ScriptStackFrame {
	private final GoalVariableType type;

	public GoalScriptStackFrame(final GoalVariableType forType, final ScriptStackFrame parent) {
		super(parent.getStack(), new IDbgpStackLevel() {
			@Override
			public boolean isSameMethod(final IDbgpStackLevel other) {
				return false; // ?!
			}

			@Override
			public String getWhere() {
				return parent.getWhere();
			}

			@Override
			public int getLineNumber() {
				return parent.getLineNumber();
			}

			@Override
			public int getLevel() {
				return parent.getLevel();
			}

			@Override
			public URI getFileURI() {
				return parent.getSourceURI();
			}

			@Override
			public int getEndLine() {
				return parent.getEndLine();
			}

			@Override
			public int getEndColumn() {
				return parent.getEndColumn();
			}

			@Override
			public int getBeginLine() {
				return parent.getBeginLine();
			}

			@Override
			public int getBeginColumn() {
				return parent.getBeginColumn();
			}

			@Override
			public String getMethodName() {
				return parent.getMethodName();
			}

			@Override
			public int getMethodOffset() {
				return parent.getMethodOffset();
			}
		});
		this.type = forType;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		final IPath sourcePath = new Path(getSourceURI().getPath());
		final IFile sourceFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(sourcePath);
		final DebuggerCollection debuggerCollection = (sourceFile == null) ? null
				: DebuggerCollection.getCollection(sourceFile.getProject().getName());
		final AgentId agent = (debuggerCollection == null) ? null
				: debuggerCollection.getAgentForThread(getScriptThread());
		final LocalDebugger debugger = (debuggerCollection == null) ? null : debuggerCollection.getMainDebugger();
		final AgentState state = (debugger == null) ? null : debugger.getAgentState(agent);
		if (state == null) {
			return new IVariable[0];
		}
		Set<String> variables = null;
		switch (this.type) {
		case EVALUATION:
			variables = state.getCondition();
			break;
		case BELIEFS:
			variables = state.getBeliefs();
			break;
		case GOALS:
			variables = state.getGoals();
			break;
		case PERCEPTS:
			variables = state.getPercepts();
			break;
		case MAILS:
			variables = state.getMails();
			break;
		}
		final Iterator<String> iterator = variables.iterator();
		final List<IVariable> returned = new ArrayList<>(variables.size());
		while (iterator.hasNext()) {
			final String variable = iterator.next();
			returned.add(new ScriptVariable(this, variable, new IDbgpProperty() {
				@Override
				public boolean isConstant() {
					return false;
				}

				@Override
				public boolean hasChildren() {
					return false;
				}

				@Override
				public String getValue() {
					return "";
				}

				@Override
				public String getType() {
					return GoalScriptStackFrame.this.type.toString();
				}

				@Override
				public int getPageSize() {
					return 0;
				}

				@Override
				public int getPage() {
					return 0;
				}

				@Override
				public String getName() {
					return variable;
				}

				@Override
				public String getKey() {
					return getName();
				}

				@Override
				public String getEvalName() {
					return getName();
				}

				@Override
				public int getChildrenCount() {
					return 0;
				}

				@Override
				public IDbgpProperty[] getAvailableChildren() {
					return new IDbgpProperty[0];
				}

				@Override
				public String getAddress() {
					return "";
				}
			}));
		}
		return returned.toArray(new IVariable[returned.size()]);
	}
}
