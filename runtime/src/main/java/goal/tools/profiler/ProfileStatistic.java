package goal.tools.profiler;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import goal.util.datatable.ColumnType;
import goal.util.datatable.DataRow;
import krTools.parser.SourceInfo;

/**
 * {@link Statistic} with additional information for a profile and to make it
 * into a hierarchy of profiles. Parent nodes contain statistics about a process
 * that has this as a sub-process.
 */
public class ProfileStatistic extends Statistic {

	/**
	 * {@link ColumnType}s to support table conversion.
	 *
	 */
	public static enum Column implements ColumnType {
		SOURCE("source"), CALLS("number of calls"), TIME("total time (s)"), INFO("source info"), TYPE("type"), THIS(
				"this"), PARENT("parent");

		private String description;

		Column(String descr) {
			this.description = descr;
		}

		@Override
		public String getDescription() {
			return this.description;
		}
	}

	private final String description;
	private final SourceInfo sourceInfo;
	private final InfoType type;
	private final ProfileStatistic parent;
	// this list is mutable, when new children are added to a parent.
	private final List<ProfileStatistic> children;
	private final Object associatedObject;

	ProfileStatistic(SourceInfo info, InfoType type, String desc, ProfileStatistic parent, Object associatedObject) {
		this(info, type, desc, parent, associatedObject, 0, 0);
	}

	/**
	 * create new statistic. args will be evaluated into the desription only
	 * when toString is called.
	 *
	 * @param info
	 *            the {@link SourceInfo} of the object. Can be null. Just to
	 *            help humans find back source code. There may be multiple
	 *            statistics attached to the same source location.
	 * @param type
	 *            the {@link InfoType}, used for sorting the rows.
	 * 
	 * @param desc
	 *            string description of this statistic.
	 * @param parent
	 *            the parent node, an object related to a goal action that
	 *            caused this statistic to occur. parent.addChild() is called to
	 *            report the new child there as well.
	 * @param associatedObject
	 *            the (unique) associatedObject with this statistic. Two
	 *            statistics originate from the same source in GOAL if they have
	 *            the same associatedObject.
	 * @param totalTime
	 *            see {@link SourceInfo}
	 * @param number
	 *            see {@link SourceInfo}
	 */
	ProfileStatistic(SourceInfo info, InfoType type, String desc, ProfileStatistic parent, Object associatedObject,
			long totalTime, long number) {
		super(totalTime, number);
		this.sourceInfo = info;
		this.type = type;
		this.description = desc;
		this.parent = parent;
		this.associatedObject = associatedObject;
		this.children = new LinkedList<>();
		if (parent != null) {
			parent.addChild(this);
		}
	}

	/**
	 * Merges 2 into a new one. recursively merges the child nodes. Should be
	 * applied only to root nodes but we Will recursively descend through the
	 * nodes of both statistics.
	 * 
	 * @param stat1
	 *            the first statistic to merge. Must be a root node.
	 * @param stat2
	 *            the second statistic to merge. Must be a root node.
	 * @param newParent
	 *            the new parent for the newly merged object.
	 * @throws IllegalArgumentException
	 *             if the statistics are not root nodes (root node means
	 *             parent=null). Or if the associatedObjects are not equal.
	 */
	public ProfileStatistic merge(ProfileStatistic stat2, ProfileStatistic newParent) {
		if (!(associatedObject.equals(stat2.associatedObject))) {
			throw new IllegalArgumentException("objects do not have same associated object");
		}
		ProfileStatistic merge = new ProfileStatistic(sourceInfo, type, description, newParent, associatedObject,
				getTotalTime() + stat2.getTotalTime(), getTotalNumber() + stat2.getTotalNumber());

		for (ProfileStatistic child1 : getChildren()) {
			ProfileStatistic child2 = stat2.getChild(child1.associatedObject);
			if (child2 == null) {
				new ProfileStatistic(child1.sourceInfo, child1.type, child1.description, merge,
						child1.associatedObject);
			} else {
				// recurse merge
				child1.merge(child2, merge);
			}
		}

		for (ProfileStatistic child2 : stat2.getChildren()) {
			ProfileStatistic child1 = getChild(child2.associatedObject);
			if (child1 == null) { // child appears only in stat2
				new ProfileStatistic(child2.sourceInfo, child2.type, child2.description, merge,
						child2.associatedObject);
			}
			// else we already merged in previous loop
		}
		return merge;

	}

	/**
	 * @param associatedObject
	 *            the key to look for in the children
	 * @return child that has as key the associateObject
	 */
	private ProfileStatistic getChild(Object associatedObject) {
		for (ProfileStatistic child : children) {
			if (associatedObject.equals(child.associatedObject)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * add child to parent.
	 * 
	 * @param profileStatistic
	 */
	private void addChild(ProfileStatistic stat) {
		if (stat == null) {
			throw new NullPointerException("child should not be null");
		}
		children.add(stat);
	}

	/**
	 * 
	 * @return parent of this node.
	 */
	public ProfileStatistic getParent() {
		return parent;
	}

	public SourceInfo getSourceInfo() {
		return this.sourceInfo;
	}

	/**
	 * @return the current statistics as DataRow.
	 */
	public DataRow getData() {
		Map<ColumnType, Object> data = new LinkedHashMap<>();
		data.put(Column.TIME, getTotalTime() / 1000000000.);
		data.put(Column.CALLS, getTotalNumber());
		data.put(Column.SOURCE, this.description);
		data.put(Column.INFO, this.sourceInfo);
		data.put(Column.TYPE, this.type);
		data.put(Column.THIS, this);
		data.put(Column.PARENT, this.parent);

		return new DataRow(data);
	}

	/**
	 * Give short version of hashcode, for neater representation in log files we
	 * don't need the whole "goal.tools.profiler.ProfileStatistic@...." but just
	 * the number
	 */
	@Override
	public String toString() {
		return "@" + Integer.toHexString(hashCode());
	}

	/**
	 * @return the unique key object for this.
	 */
	public Object getAssociatedObject() {
		return associatedObject;
	}

	/**
	 * 
	 * @return all children of this statistic
	 */
	public List<ProfileStatistic> getChildren() {
		return children;
	}

	public String getDescription() {
		return description;
	}

}
