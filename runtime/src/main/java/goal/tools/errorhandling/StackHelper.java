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
package goal.tools.errorhandling;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

/**
 * Class with static helper method relating to printing the stack trace of an
 * error. Extracted from {@link Warning}, to be more intuitively used by other
 * classes.
 */
public class StackHelper {
	// disable instantiation
	private StackHelper() {
	}

	/**
	 * Returns the full stack trace associated with the exception and its causes.
	 *
	 * @param error the error for which the stack trace information is requested.
	 * @return string with stack trace information related to exception and its
	 *         causes.
	 */
	public static String getFullStackTraceInfo(Throwable error) {
		StringBuilder stackTrace = new StringBuilder();
		Throwable cause;

		List<StackTraceElement> stackElements = getTopStack(error);
		for (StackTraceElement element : stackElements) {
			stackTrace.append(element);
			stackTrace.append("\n"); //$NON-NLS-1$
		}

		// add info about the cause if a cause exists
		cause = error.getCause();
		if (cause != null) {
			stackTrace.append(Resources.get(WarningStrings.BY_DEEPER_EXCEPTION));
			stackTrace.append(cause);
			stackTrace.append("\n"); //$NON-NLS-1$
			stackTrace.append(getFullStackTraceInfo(cause));
		}

		return stackTrace.toString();
	}

	/**
	 * Returns the stack trace elements associated with the exception, but only
	 * includes one cause (the 'top' cause) and only those messages that include a
	 * line number.
	 *
	 * @param error the error for which the stack trace elements are requested.
	 * @return the elements in the stack trace up to the top cause and only those
	 *         elements that have an associated Java line number.
	 */
	private static List<StackTraceElement> getTopStack(Throwable error) {
		List<StackTraceElement> elements = Arrays.asList(error.getStackTrace());

		int startIdx = 0;

		// remove all lines up to the first line that shows a real source file
		// location such as MentalState.java:22
		while (startIdx < elements.size() && elements.get(startIdx).getLineNumber() < 0) {
			startIdx++;
		}
		if (startIdx == elements.size()) {
			startIdx = 0;
		}
		return elements.subList(startIdx, elements.size());
	}

	/**
	 * get full string with all (sub)causes, starting with the topmost cause
	 *
	 * @return full string with all (sub)causes, starting with the topmost cause.
	 *         Returns empty string if no cause available.
	 * @param error the error for which the stack trace elements are requested.
	 */
	public static String getAllCauses(Throwable error) {
		String fullcause = ""; //$NON-NLS-1$
		while (error != null) {
			if (error.getMessage() != null) {
				if (!fullcause.isEmpty()) {
					fullcause += ": "; // separate submessages //$NON-NLS-1$
				}
				fullcause += getMessage(error);
			}
			error = error.getCause();
		} // while (error != null);
		return fullcause;
	}

	/**
	 * Returns error.getMessage() but with workaround for issue with
	 * RemoteException.getMessage() (bugged because it uses toString and gets very
	 * verbose)
	 * 
	 * @param error
	 * @return error message
	 */
	private static String getMessage(Throwable error) {
		if (error instanceof RemoteException) {
			return "error on server: ";
		}
		return error.getMessage();
	}
}
