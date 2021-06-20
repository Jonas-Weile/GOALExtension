package goal.tools.profiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Database that collects all {@link AgentProfile}s that have been killed so
 * far. This is used to accumulate profiles of different agents after a run.
 */
public class Profiles {

	private List<AgentProfile> profiles = new LinkedList<>();

	/**
	 * Adds a new 'finished' profiler to the set.
	 * 
	 * @param profiler
	 *            finished {@link Profiler}
	 */
	public void add(AgentProfile profile) {
		profiles.add(profile);
	}

	/**
	 * @param agentTypeName
	 *            the name of the profiles to be merged.
	 * @return a {@link AgentProfile} that contains merged info from all
	 *         profiles contained here. Can return empty profile if no profiles
	 *         of given name exist.
	 */
	public AgentProfile getMergedProfile(String agentTypeName) {
		AgentProfile merged = new AgentProfile(agentTypeName);
		for (AgentProfile profile : getProfiles(agentTypeName)) {
			merged = merged.merge(profile);
		}
		return merged;
	}

	/**
	 * @return all names occuring in the profiles.
	 */
	public Set<String> getNames() {
		return profiles.stream().map(p -> p.getName()).collect(Collectors.toSet());
	}

	/**
	 * 
	 * @param name
	 *            the name looked for
	 * @return all profiles that have {@link AgentProfile#getName()} equal to
	 *         given name.
	 */
	public List<AgentProfile> getProfiles(String name) {
		return profiles.stream().filter(profile -> name.equals(profile.getName())).collect(Collectors.toList());
	}

}
