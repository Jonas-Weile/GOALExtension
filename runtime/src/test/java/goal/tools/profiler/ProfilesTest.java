package goal.tools.profiler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Sets;

public class ProfilesTest {
	@Test
	public void testGetNames() {

		Profiles profiles = new Profiles();
		profiles.add(new AgentProfile("A"));
		profiles.add(new AgentProfile("A"));
		profiles.add(new AgentProfile("B"));
		profiles.add(new AgentProfile("A"));
		profiles.add(new AgentProfile("A"));
		profiles.add(new AgentProfile("C"));
		profiles.add(new AgentProfile("C"));
		profiles.add(new AgentProfile("A"));
		assertEquals(Sets.newHashSet("A", "B", "C"), profiles.getNames());
	}
}
