package org.eclipse.gdt.launching;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.debug.core.IDbgpService;
import org.eclipse.dltk.debug.core.model.IScriptThread;
import org.eclipse.dltk.internal.debug.core.model.ScriptDebugTarget;
import org.eclipse.dltk.internal.debug.core.model.ScriptThreadManager;

public class GoalDebugTarget extends ScriptDebugTarget {
	private final ScriptThreadManager newManager;

	public GoalDebugTarget(final String modelId, final IDbgpService dbgpService, final String sessionId,
			final ILaunch launch) {
		super(modelId, dbgpService, sessionId, launch, null);
		this.newManager = new GoalThreadManager(this);
		this.newManager.addListener(this);
		try {
			final Field manager = ScriptDebugTarget.class.getDeclaredField("threadManager");
			manager.setAccessible(true);
			manager.set(this, this.newManager);
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
	}

	public ScriptThreadManager getThreadManager() {
		return this.newManager;
	}

	private static final class GoalThreadManager extends ScriptThreadManager {
		private List<IScriptThread> threads = new ArrayList<>(0);

		@SuppressWarnings("unchecked")
		public GoalThreadManager(final ScriptDebugTarget target) {
			super(target);
			try {
				final Field threadsField = ScriptThreadManager.class.getDeclaredField("threads");
				threadsField.setAccessible(true);
				this.threads = (List<IScriptThread>) threadsField.get(this);
			} catch (final Exception e) {
				DLTKCore.error(e);
			}
		}

		private interface IThreadBoolean {
			boolean get(IScriptThread thread);
		}

		private boolean getThreadBoolean(final IThreadBoolean b) {
			for (final IScriptThread thread : getThreads()) {
				if (b.get(thread)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasThreads() {
			return !this.threads.isEmpty();
		}

		@Override
		public IScriptThread[] getThreads() {
			return this.threads.toArray(new IScriptThread[this.threads.size()]);
		}

		@Override
		public boolean canResume() {
			return getThreadBoolean(new IThreadBoolean() {
				@Override
				public boolean get(final IScriptThread thread) {
					return thread.canResume();
				}
			});
		}

		@Override
		public boolean canSuspend() {
			return getThreadBoolean(new IThreadBoolean() {
				@Override
				public boolean get(final IScriptThread thread) {
					return thread.canSuspend();
				}
			});
		}

		@Override
		public boolean isSuspended() {
			return getThreadBoolean(new IThreadBoolean() {
				@Override
				public boolean get(final IScriptThread thread) {
					return thread.isSuspended();
				}
			});
		}

		@Override
		public void resume() throws DebugException {
			for (final IScriptThread thread : getThreads()) {
				if (thread.canResume()) {
					thread.resume();
				}
			}
		}

		@Override
		public void suspend() throws DebugException {
			for (final IScriptThread thread : getThreads()) {
				if (thread.canSuspend()) {
					thread.suspend();
				}
			}
		}

		@Override
		public void refreshThreads() {
			for (final IScriptThread thread : getThreads()) {
				thread.updateStackFrames();
			}
		}
	}
}
