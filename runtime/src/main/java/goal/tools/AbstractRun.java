/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package goal.tools;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedSet;

import goal.core.agent.AgentFactory;
import goal.core.agent.GOALInterpreter;
import goal.core.runtime.RuntimeManager;
import goal.core.runtime.service.agent.AgentService;
import goal.core.runtime.service.environment.EnvironmentService;
import goal.preferences.LoggingPreferences;
import goal.tools.debugger.Debugger;
import goal.tools.eclipse.RunTool;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import goal.tools.history.StorageEventObserver;
import goal.tools.logging.InfoLog;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.mas.MASValidator;
import languageTools.errors.Message;
import languageTools.program.mas.MASProgram;

/**
 * Abstract run can be used to run a {@link MASProgram}. The run will setup the
 * environment and launch the agents of the MAS. When all agents have terminated
 * the environment will be terminated as well. It is also possible to terminate
 * a MAS based on a timeout.
 * <p>
 * The result of the run can be inspected by means of a {@link ResultInspector}.
 * </p>
 * <p>
 * The choice of agent types that are used to create and run the agents in the
 * MAS is delegated to the implementation of this abstract class. These
 * implementations need to provide an {@link AgentFactory} that defines the
 * types of agents by implementing the {@link #buildAgentFactory} method.
 * </p>
 *
 * @param <DEBUGGER>   Type of debugger used by the agent controller
 *                     (interpreter).
 * @param <CONTROLLER> Type of the agent controller used (interpreter).
 */
public abstract class AbstractRun<DEBUGGER extends Debugger, CONTROLLER extends GOALInterpreter<DEBUGGER>> {
	/**
	 * {@code true} if the agent should log their debugger output. Default is to
	 * <b>not</b> log any debugger output.
	 */
	protected boolean debuggerOutput = false;
	/**
	 * The timestamp (in millisecond precision) at which the run should be
	 * terminated; 0 means run indefinitely.
	 */
	protected long timeout = 0;

	private ResultInspector<CONTROLLER> resultInspector = null;
	private final FileRegistry registry;
	private final MASProgram masProgram;
	private RuntimeManager<DEBUGGER, CONTROLLER> runtimeManager = null;

	/**
	 * Creates a run for a MAS file.
	 *
	 * @param masFile A MAS file.
	 * @throws GOALRunFailedException
	 */
	public AbstractRun(File masFile) throws GOALRunFailedException {
		this.registry = new FileRegistry();
		if (masFile == null) {
			this.masProgram = null;
		} else {
			String java = System.getProperty("java.runtime.name") + " " + System.getProperty("java.runtime.version");
			new InfoLog("GOAL " + getVersion() + " on " + java + " " + System.getProperty("os.arch")).emit();

			MASValidator mas2g = null;
			try {
				mas2g = new MASValidator(masFile.getCanonicalPath(), this.registry);
				mas2g.validate();
				mas2g.process();
			} catch (IOException e) {
				mas2g = null; // TODO
			}
			this.masProgram = (mas2g == null) ? null : mas2g.getProgram();
		}
	}

	public AbstractRun(MASProgram mas) {
		this.registry = new FileRegistry();
		this.masProgram = mas;
	}

	public SortedSet<Message> getErrors() {
		return this.registry.getAllErrors();
	}

	public SortedSet<Message> getWarnings() {
		return this.registry.getWarnings();
	}

	public MASProgram getProgram() {
		return this.masProgram;
	}

	/**
	 * Sets a timeout for the run.
	 *
	 * @param timeout The number of seconds to wait before the run is terminated.
	 *                Use a timeout of 0 to wait indefinitely.
	 */
	public void setTimeOut(long timeout) {
		if (timeout > 0) {
			this.timeout = System.currentTimeMillis() + (timeout * 1000L);
		} else {
			this.timeout = 0;
		}
	}

	/**
	 * Set to true to start the {@link MASProgram} with a logging debugger.
	 *
	 * @param debuggerOutput {@code true} if a logging debugger should be used.
	 */
	public void setDebuggerOutput(boolean debuggerOutput) {
		this.debuggerOutput = debuggerOutput;
	}

	/**
	 * Sets the {@link ResultInspector} used to inspect the agent states at the end
	 * of a run.
	 *
	 * @param resultInspector An inspector for inspecting the agent's states at the
	 *                        end of a run.
	 */
	public void setResultInspector(ResultInspector<CONTROLLER> resultInspector) {
		this.resultInspector = resultInspector;
	}

	/**
	 * Starts a run of a MAS program.
	 *
	 * @throws GOALRunFailedException
	 */
	public void run(boolean startEnvironments) throws GOALRunFailedException {
		String file = (this.masProgram == null) ? "unknown" : this.masProgram.getSourceFile().getPath();
		try {
			if (this.masProgram == null || this.registry.hasAnyError()) {
				throw new GOALLaunchFailureException(
						"found errors while parsing: " + this.registry.getAllErrors() + ".");
			}

			// Launch the MAS and start the runtime environment.
			new InfoLog("launching '" + file + "'.").emit();

			if (this.runtimeManager == null) {
				buildRuntime();
			}

			// Start the environment (if any). This will also start the MAS!
			this.runtimeManager.start(startEnvironments);

			// Wait for at least one agent to show up.
			// Not all environments result in agents being launched immediately.
			new InfoLog("waiting for the first agent to be launched...").emit();
			this.runtimeManager.awaitFirstAgent();
			try {
				// Wait for system to end.
				awaitTermination(this.runtimeManager);
				new InfoLog("all agents have stopped running.").emit();
			} catch (InterruptedException e) {
				new InfoLog(e.getMessage()).emit();
			}
		} catch (Exception e) { // top level catch of run of MAS
			throw new GOALRunFailedException("could not perform run of '" + file + "'.", e);
		} finally {
			if (this.resultInspector != null && this.runtimeManager != null) {
				this.resultInspector.handleResult(this.runtimeManager.getAgents());
			}
			cleanup();
			new InfoLog("'" + file + "' has been terminated").emit();
		}
	}

	/**
	 * This is to be called after the run has finished, to shutdown the runtime
	 * manager. FIXME: public for SimpleIDE (hacky)
	 */
	public void cleanup() {
		if (this.runtimeManager != null) {
			this.runtimeManager.shutDown(false);
			this.runtimeManager = null;
		}
	}

	/**
	 * Blocks until the agent system is terminated.
	 * <p>
	 * Implementations of this class can implement their own termination criteria
	 * here.
	 * </p>
	 *
	 * @param runtimeManager A runtime manager.
	 * @throws InterruptedException
	 */
	protected void awaitTermination(RuntimeManager<? extends DEBUGGER, ? extends CONTROLLER> runtimeManager)
			throws InterruptedException {
		runtimeManager.awaitTermination();
	}

	/**
	 *
	 * @return the runtime manager, or null if not running.
	 */
	public RuntimeManager<DEBUGGER, CONTROLLER> getManager() {
		return this.runtimeManager;
	}

	/**
	 * Builds the {@link RuntimeManager} that will be used to run the MAS Program.
	 *
	 * @return a new run time service manager.
	 * @throws GOALLaunchFailureException when the program could not be validated
	 */
	public RuntimeManager<DEBUGGER, CONTROLLER> buildRuntime() throws GOALLaunchFailureException {
		// Initialize a messaging and environment service.
		EnvironmentService environmentService = new EnvironmentService(this.masProgram);
		AgentFactory<DEBUGGER, CONTROLLER> agentFactory = buildAgentFactory();
		AgentService<DEBUGGER, CONTROLLER> runtimeService = new AgentService<>(this.masProgram, agentFactory);

		this.runtimeManager = new RuntimeManager<>(runtimeService, environmentService);
		if (LoggingPreferences.getEnableHistory()) {
			this.runtimeManager.addObserver(new StorageEventObserver());
		}

		return this.runtimeManager;
	}

	/**
	 * Provides an agent factory used for creating agents at run time.
	 *
	 * @return An agent factory.
	 * @throws GOALLaunchFailureException
	 */
	protected abstract AgentFactory<DEBUGGER, CONTROLLER> buildAgentFactory() throws GOALLaunchFailureException;

	/**
	 * @return a unique number for the current source code, that changes when the
	 *         GOAL version changes. the maven version number of this GOAL runtime,
	 *         or the modification date of this class if no maven info is
	 *         available..
	 */
	private static String getVersion() {
		String version = RunTool.class.getPackage().getImplementationVersion();
		if (version == null) {
			version = "";
		} else {
			version += " ";
		}
		try {
			String srcpath = RunTool.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			File srcfile = new File(URLDecoder.decode(srcpath, "UTF-8"));
			version += "build " + new SimpleDateFormat("yyyyMMddHHmm").format(new Date(srcfile.lastModified()));
		} catch (Exception ignore) {
		}

		return version;
	}
}