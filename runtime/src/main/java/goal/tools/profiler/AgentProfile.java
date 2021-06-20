package goal.tools.profiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import goal.core.runtime.service.agent.NettoRunTime;
import goal.preferences.LoggingPreferences;
import goal.preferences.ProfilerPreferences;
import goal.tools.errorhandling.Warning;
import goal.tools.logging.InfoLog;
import goal.util.datatable.ColumnType;
import goal.util.datatable.DataRow;
import goal.util.datatable.DataTable;
import languageTools.program.agent.AgentId;

/**
 * a profile contains a set of ProfileStatistics, one for each object that is
 * being profiled. The keys are the associateObjects coming from the core, or a
 * carefully crafted object for a number of global accumulated properties (see
 * the constants below). Proper implementation of these objects equals()
 * function is essential so that events originating from the same Object map
 * into the same ProfileStatistic.
 */

public class AgentProfile {
	/**
	 * Object determines the unique source of the ProfileStatistic. It must have
	 * a properly implemented hashCode and equals function to allow this to
	 * work.
	 */
	private final Map<Object, ProfileStatistic> objectStatistics;
	/**
	 * Name of agent definition as described in the MAS file.
	 */
	private final String name;

	/**
	 * As {@link AgentProfile#AgentProfile(Map, String)} but with empty HashMap
	 * for the statistics.
	 * 
	 * @param agentTypeName
	 */
	public AgentProfile(String agentTypeName) {
		this(new LinkedHashMap<>(), agentTypeName);
	}

	/**
	 * @param stats
	 *            the statistics already gathered.
	 * @param agentTypeName
	 *            Name of agent definition as described in the MAS file.
	 *            Profiles with the same name are eligible for accumulation
	 *            after the run.
	 */
	public AgentProfile(Map<Object, ProfileStatistic> stats, String agentTypeName) {
		this.objectStatistics = new HashMap<>(stats);
		this.name = agentTypeName;
	}

	/**
	 * Get the statistics of some object
	 * 
	 * @param obj
	 *            the object for which statistics are needed
	 * @return
	 */
	public ProfileStatistic get(Object obj) {
		return objectStatistics.get(obj);
	}

	/**
	 * Add new associated object and associated statistics
	 * 
	 * @param associateObject
	 *            This object determines the unique source of the
	 *            ProfileStatistic. It must have a properly implemented hashCode
	 *            and equals function to allow this to work.
	 * @param stat
	 *            the new statistics object
	 */
	public void put(ProfileStatistic stat) {
		objectStatistics.put(stat.getAssociatedObject(), stat);
	}

	public Set<Object> keySet() {
		return objectStatistics.keySet();
	}

	/**
	 * Get the statistic results.
	 * 
	 * @param stopTime
	 *            the stoptime (millis netto runtime, see
	 *            {@link NettoRunTime#get()})
	 *
	 * @return A DataTable, sorted already for convenience.
	 */
	public DataTable getStats() {
		DataTable table = new DataTable();

		for (Object module : this.objectStatistics.keySet()) {
			table.add(this.objectStatistics.get(module).getData());
		}

		table.sort(new CompareStats());

		return table;
	}

	/**
	 * Log the current profile situation to a log file.
	 * 
	 * @param agentID
	 */
	public void log(AgentId agentID) {
		String separator = ProfilerPreferences.getProfilingToFile() ? "," : "\t";
		DataTable stats = getStats();
		boolean logNodeID = ProfilerPreferences.getLogNodeId();

		StringBuilder profile = new StringBuilder();
		profile.append("profile for " + agentID).append("\n");
		profile.append("--------------------").append("\n");

		List<ColumnType> cols = new ArrayList<>(6);
		cols.add(ProfileStatistic.Column.TIME);
		cols.add(ProfileStatistic.Column.CALLS);
		cols.add(ProfileStatistic.Column.SOURCE);
		cols.add(ProfileStatistic.Column.INFO);
		if (logNodeID) {
			cols.add(ProfileStatistic.Column.THIS);
			cols.add(ProfileStatistic.Column.PARENT);
		}
		profile.append(stats.header(cols, separator)).append("\n");

		for (DataRow stat : stats.getData()) {
			if (ProfilerPreferences.isTypeSelected((InfoType) stat.column(ProfileStatistic.Column.TYPE))) {
				profile.append(stat.format(cols, separator)).append("\n");
			}
		}
		profile.append("--------------------").append("\n");

		if (ProfilerPreferences.getProfilingToFile()) {
			try {
				DateFormat format = new SimpleDateFormat("yy-MM-dd_HH.mm.ss");
				String fname = agentID + "_" + format.format(new Date()) + "_profile.csv";
				Files.write(Paths.get(LoggingPreferences.getLogDirectory() + File.separator + fname),
						profile.toString().getBytes());
				new InfoLog("written profile to " + fname).emit();
			} catch (IOException e) {
				new Warning("failed writing profiler results to file for agent '" + agentID + "'.", e).emit();
			}
		} else {
			new InfoLog(profile.toString()).emit();
		}
	}

	/**
	 * 
	 * @param prof
	 *            the profile to merge the current profile with
	 * @return new merged profile.
	 */
	public AgentProfile merge(AgentProfile otherprof) {
		if (!name.equals(otherprof.getName())) {
			throw new IllegalArgumentException("Profiles are for different agents and should not be merged");
		}
		AgentProfile newprof = new AgentProfile(objectStatistics, name);

		// first check keys that we know
		for (Object ourkey : objectStatistics.keySet()) {
			ProfileStatistic ourstatistic = objectStatistics.get(ourkey);
			ProfileStatistic otherstatistic = otherprof.get(ourkey);
			if (ourstatistic.getParent() == null) {
				// handle only root nodes. ProfileStatistic merges the rest
				if (otherprof.get(ourkey) != null) {
					// we need to merge this and recursively descend
					newprof.addAllChildren(ourstatistic.merge(otherstatistic, null));
				}
			} else {
				newprof.addAllChildren(objectStatistics.get(ourkey));
			}
		}

		// and check keys known only in otherprofile
		for (Object otherkey : otherprof.keySet()) {
			if (!objectStatistics.containsKey(otherkey)) {
				// the ones we knew were already handled above.
				ProfileStatistic otherstatistic = otherprof.get(otherkey);
				if (otherstatistic.getParent() == null) {
					newprof.addAllChildren(otherprof.get(otherkey));
				}
			}
		}

		return newprof;
	}

	/**
	 * @return Name of agent definition as described in the MAS file. Profiles
	 *         with the same name are eligible for accumulation after the run.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Add the profileStatistic and all children statistics (so the whole tree
	 * under profileStatistic) to our statistics.
	 * 
	 * @param profileStatistic
	 *            a statistic that may have children. Normally this is a root
	 *            node but we do not enforce this.
	 */
	private void addAllChildren(ProfileStatistic profileStatistic) {
		objectStatistics.put(profileStatistic.getAssociatedObject(), profileStatistic);
		for (ProfileStatistic child : profileStatistic.getChildren()) {
			addAllChildren(child);
		}
	}

}
