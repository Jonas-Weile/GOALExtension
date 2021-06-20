package goal.tools.profiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import krTools.parser.SourceInfo;

public class ProfileStatisticTest {
	private final SourceInfo sourceInfo = mock(SourceInfo.class);
	private final String desc = "blabla";
	private final String desc2 = "blabla2";
	private final String rootdesc = "rootblabla";

	@Test
	public void testMerge() {
		ProfileStatistic stat1 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.desc, null, this.desc);
		ProfileStatistic stat2 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.desc, null, this.desc);

		stat1.add(100);
		stat2.add(55);
		stat2.add(33);

		ProfileStatistic merge = stat1.merge(stat2, null);

		assertEquals(188, merge.getTotalTime());
		assertEquals(3, merge.getTotalNumber());
	}

	@Test
	public void testParent() {
		ProfileStatistic root1 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.rootdesc, null,
				this.rootdesc);
		ProfileStatistic stat1 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.desc, root1, this.desc);
		assertEquals(root1, stat1.getParent());
		assertEquals(1, root1.getChildren().size());
		assertEquals(stat1, root1.getChildren().get(0));
	}

	@Test
	public void testMergeParents() {
		ProfileStatistic root1 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.rootdesc, null,
				this.rootdesc);
		ProfileStatistic stat1 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.desc, root1, this.desc);
		ProfileStatistic root2 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.rootdesc, null,
				this.rootdesc);
		ProfileStatistic stat2 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.desc, root2, this.desc);

		stat1.add(100);
		stat2.add(55);
		stat2.add(33);

		ProfileStatistic merge = root1.merge(root2, null);

		// check that there is a new, merged root
		assertNotEquals(root1, merge);
		assertNotEquals(root2, merge);

		assertEquals(1, merge.getChildren().size());
		ProfileStatistic newchild = merge.getChildren().get(0);

		assertEquals(188, newchild.getTotalTime());
		assertEquals(3, newchild.getTotalNumber());

		assertEquals(merge, merge.getChildren().get(0).getParent());
	}

	@Test
	public void testMergeParentsDifferentChilds() {
		ProfileStatistic root1 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.rootdesc, null,
				this.rootdesc);
		ProfileStatistic stat1 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.desc, root1, this.desc);
		ProfileStatistic root2 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.rootdesc, null,
				this.rootdesc);
		ProfileStatistic stat2 = new ProfileStatistic(this.sourceInfo, InfoType.KR_CALL, this.desc2, root2, this.desc2);

		stat1.add(100);
		stat2.add(55);
		stat2.add(33);

		ProfileStatistic merge = root1.merge(root2, null);

		// check that there is a new, merged root
		assertNotEquals(root1, merge);
		assertNotEquals(root2, merge);

		assertEquals(2, merge.getChildren().size());
		merge.getChildren().get(0);
		merge.getChildren().get(0);

		assertEquals(this.desc, merge.getChildren().get(0).getDescription());
		assertEquals(merge, merge.getChildren().get(0).getParent());
		assertEquals(this.desc2, merge.getChildren().get(1).getDescription());
		assertEquals(merge, merge.getChildren().get(1).getParent());
	}
}
