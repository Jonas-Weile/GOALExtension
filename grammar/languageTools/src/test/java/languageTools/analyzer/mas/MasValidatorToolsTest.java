package languageTools.analyzer.mas;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import languageTools.errors.ValidatorWarning;
import languageTools.program.mas.Entity;
import languageTools.program.mas.LaunchRule;

/**
 * Test if rules are reachable (part of MasValidator)
 *
 */
public class MasValidatorToolsTest {
	private String TYPE1 = "type1"; // arbitrary type
	private String TYPE2 = "type2"; // arbitrary type

	private LaunchRule simpleRule1 = mock(LaunchRule.class);
	private LaunchRule simpleRule2 = mock(LaunchRule.class);

	// conditional rule, but type=null (just triggers on a entity name)
	private LaunchRule conditionalRule1 = mock(LaunchRule.class);
	private Entity entity = mock(Entity.class);

	private LaunchRule catchSomeType1 = mock(LaunchRule.class);

	private LaunchRule catchAllRule1 = mock(LaunchRule.class);
	private Entity entity1 = mock(Entity.class);

	private LaunchRule catchAllRule2 = mock(LaunchRule.class);
	private Entity entity2 = mock(Entity.class);

	private LaunchRule catchAllStarRule = mock(LaunchRule.class);
	private Entity entityStar = mock(Entity.class);

	@Before
	public void before() {

		when(catchSomeType1.isConditional()).thenReturn(true);
		when(catchSomeType1.getEntity()).thenReturn(entity1);
		when(conditionalRule1.isCatchAll()).thenReturn(false);

		when(conditionalRule1.isConditional()).thenReturn(true);
		when(conditionalRule1.getEntity()).thenReturn(entity);
		when(conditionalRule1.isCatchAll()).thenReturn(false);

		when(catchAllRule1.isConditional()).thenReturn(true);
		when(catchAllRule1.getEntity()).thenReturn(entity1);
		when(catchAllRule1.isCatchAll()).thenReturn(true);
		when(entity1.getType()).thenReturn(TYPE1);

		when(catchAllRule2.isConditional()).thenReturn(true);
		when(catchAllRule2.getEntity()).thenReturn(entity2);
		when(catchAllRule2.isCatchAll()).thenReturn(true);
		when(entity2.getType()).thenReturn(TYPE2);

		when(catchAllStarRule.isConditional()).thenReturn(true);
		when(catchAllStarRule.getEntity()).thenReturn(entityStar);
		when(catchAllStarRule.isCatchAll()).thenReturn(true);
		when(entityStar.getType()).thenReturn(null); // means ALL types.

	}

	@Test
	public void testNoRulesReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void testSimpleRulesReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		launchRules.add(simpleRule1);
		launchRules.add(simpleRule2);
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void testOneConditionalRuleReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		launchRules.add(conditionalRule1);
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void testOneCatchAllReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		launchRules.add(catchAllRule1);
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertTrue(warnings.isEmpty());
	}

	/**
	 * 2 catch-all rules catching the same type
	 */
	@Test
	public void testTwoCatchAllsReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		launchRules.add(catchAllRule1);
		launchRules.add(catchAllRule1);
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertFalse(warnings.isEmpty());
		assertTrue(warnings.get(0).toString().contains("unreachable because a previous rule catches all entities"));
	}

	/**
	 * 2 catch-all rules but catching differnet types
	 */
	@Test
	public void testTwoCatchAllsDifferentReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		launchRules.add(catchAllRule1);
		launchRules.add(catchAllRule2);
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertTrue(warnings.isEmpty());
	}

	/**
	 * 2 catch-all rules. rule 1 and 3 catch same type
	 */
	@Test
	public void testThreeCatchAllsReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		launchRules.add(catchAllRule1);
		launchRules.add(catchAllRule2);
		launchRules.add(catchAllRule1);
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertFalse(warnings.isEmpty());
		assertTrue(warnings.get(0).toString().contains("unreachable because a previous rule catches all entities"));
	}

	/**
	 * catch-all rule with * type, followed by a normal rule (which can not be
	 * reached as * catches everything)
	 */
	@Test
	public void testCatchAllAndNormalReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		launchRules.add(catchAllStarRule);
		launchRules.add(simpleRule1);
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertFalse(warnings.isEmpty());
		assertTrue(warnings.get(0).toString().contains("unreachable because a previous rule catches all entities"));
	}

	/**
	 * normal rule followed by catch-all rule with * type. Should be all ok
	 */
	@Test
	public void testNormalAndCatchAllReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		launchRules.add(simpleRule1);
		launchRules.add(catchAllStarRule);
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertTrue(warnings.isEmpty());
	}

	/**
	 * catch some and then all.
	 */
	@Test
	public void testCatchSomeAndAllReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		launchRules.add(catchSomeType1);
		launchRules.add(catchAllRule1);
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertTrue(warnings.isEmpty());
	}

	/**
	 * catch all and then some of type (not ok).
	 */
	@Test
	public void testCatchAllAndSomeReachable() {
		List<LaunchRule> launchRules = new ArrayList<>();
		launchRules.add(catchAllRule1);
		launchRules.add(catchSomeType1);
		List<ValidatorWarning> warnings = MASValidatorTools.checkLaunchRulesReachable(launchRules, null);
		assertFalse(warnings.isEmpty());
		assertTrue(warnings.get(0).toString().contains("unreachable because a previous rule catches all entities"));
	}

}
