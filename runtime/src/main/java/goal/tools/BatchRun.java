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
import java.util.Arrays;
import java.util.List;

import goal.tools.adapt.FileLearner;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import languageTools.program.mas.MASProgram;

/**
 * A Batch will run a batch of {@link MASProgram}s a repeated number of times.
 * Between runs the state of the agents {@link FileLearner} can be persisted by
 * setting a {@link PersistanceHelper}. To inspect the results of each run the
 * {@link ResultInspector} can be used.
 */
public class BatchRun extends SingleRun {
	private long repeats = 1;
	private final List<File> masFiles;

	/**
	 * Creates an instance of {@link BatchRun} that can be used to run the
	 * <code>masFile</code>>.
	 *
	 * @param masFile
	 *            to use in this {@link BatchRun}
	 * @throws GOALRunFailedException
	 */
	public BatchRun(File... masFile) throws GOALRunFailedException {
		this(Arrays.asList(masFile));
	}

	/**
	 * Creates an instance of {@link BatchRun} using the the {@link MASProgram}
	 * (s).
	 *
	 * @param masFiles
	 *            to use in this {@link BatchRun}
	 * @throws GOALRunFailedException
	 */
	public BatchRun(List<File> masFiles) throws GOALRunFailedException {
		super((File) null);
		this.masFiles = masFiles;
	}

	/**
	 * Sets the number of times the {@link BatchRun} is repeated.
	 *
	 * @param times
	 *            the {@link BatchRun} is repeated
	 */
	public void setRepeats(long times) {
		this.repeats = times;
	}

	/**
	 * Starts the batch run. This will repeat running all {@link MASProgram}s
	 * for a given number of times.
	 *
	 * @throws GOALRunFailedException
	 *             thrown when a run fails; if multiple runs fail, only the last
	 *             exception is thrown, e.g. all runs are executed at all times.
	 */
	@Override
	public void run(boolean startEnvironments) throws GOALRunFailedException {
		GOALRunFailedException last = null;
		for (long i = 0; i < this.repeats; i++) {
			for (File masFile : this.masFiles) {
				try {
					SingleRun singleRun = new SingleRun(masFile);
					singleRun.setTimeOut(this.timeout);
					singleRun.setDebuggerOutput(this.debuggerOutput);
					singleRun.run(startEnvironments);
				} catch (GOALRunFailedException e) { // top level reporting
					new Warning(e.getMessage(), e.getCause()).emit();
					last = e;
				}
			}
		}

		if (last != null) {
			throw last;
		}
	}

}