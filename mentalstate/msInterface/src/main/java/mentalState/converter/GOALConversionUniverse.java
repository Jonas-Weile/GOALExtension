/**
 * The GOAL Mental State. Copyright (C) 2014 Koen Hindriks.
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

package mentalState.converter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a conversion universe for GOAL. This creates a list of known
 * {@link GOALConversionElement}s. {@link GOALConversionElement}s are converted
 * toString and inserted into the list. After insertion, the
 * {@link GOALConversionElement}s can be referred to using their index number.
 * 
 * <p>
 * Every {@link GOALConversionElement} is assumed to map to a unique index
 * number (even though there are separate lists for each type of element)
 * 
 * <p>
 * Different instances of {@link GOALConversionElement} are treated differently
 * DOC. It seems that there are two modes of operation: one where
 * preassignedIndexToElement is not used, and another where it is used. There
 * are no safety checks for doing operations in the wrong order in these modes.
 */
public class GOALConversionUniverse implements Serializable {
	private static final long serialVersionUID = 183750989441408102L;
	// /**
	// * Mapping from beliefs to their indices.
	// */
	// private final Map<GOALCE_Belief, Integer> beliefToIndex = new
	// HashMap<>();
	// /**
	// * Mapping from foci (at a specific depth) to their indices (this means
	// that
	// * the same focus may occur multiple times in this mapping, although at
	// * different depths.
	// */
	// private final List<HashMap<GOALCE_FocusAtDepth, Integer>>
	// focusAtDepthToIndex = new LinkedList<>();
	// /**
	// * Mapping from goals (at a specific depth) to their indices (this means
	// * that the same goal may occur multiple times in this mapping, although
	// at
	// * different depths).
	// */
	// private final List<HashMap<GOALCE_GoalAtDepth, Integer>>
	// goalAtDepthToIndex = new LinkedList<>();
	// /**
	// * "Mapping" from indices to both beliefs, goals or foci (which are hence
	// * all stored in this mapping).
	// */
	// private final List<GOALConversionElement> indexToElement = new
	// LinkedList<>();
	// /**
	// * The number of foci.
	// */
	// private int nFocusses = 0;
	// /**
	// * The lists below are used to store preassigned indices for elements. Any
	// * new element added to the universe is checked against this array and if
	// a
	// * match is found (string match) then the index of that element in this
	// * array will be used. Preassigned indices are used during learning only
	// and
	// * should not interfere with normal operation when preassignedElementIndex
	// * should always be null. Ideally we would want to do this by simply
	// * serializing this object, but since that is not possible, we use this
	// * scheme.
	// */
	// private List<String> preassignedElementIndex = null;
	// private final List<GOALConversionElement> preassignedIndexToElement = new
	// LinkedList<>();

	/**
	 * Lookup from String (GOALConversionElement) to Integer is the most common
	 * function needed here.
	 */
	private Map<String, Integer> elements = new HashMap<>();

	//
	// Public methods
	//

	/**
	 * Adds specified element to this conversion universe if the element is not
	 * already contained. In this latter case, the index of the existing element
	 * is returned. Otherwise, the index of the added element is returned.
	 *
	 * @param element
	 *            - The element to be added.
	 * @return The index at which the element is registered in the universe.
	 */
	public int addIfNotContains(GOALConversionElement element) {
		String elementStr = element.toString();
		Integer index = elements.get(elementStr);
		if (index == null) {
			index = addElement(elementStr);
		}

		// int index = getIndex(element);
		//
		// /* If the element is not yet contained, add it */
		// if (index == -1) {
		//
		// /* Get a new index */
		// index = assignIndex();
		//
		// /*
		// * If the element is a belief, add it to beliefToIndex with the new
		// * index
		// */
		// if (element instanceof GOALCE_Belief) {
		// this.beliefToIndex.put((GOALCE_Belief) element, index);
		// }
		//
		// /*
		// * If the element is a goal, add it to goalAtDepthToIndex with the
		// * new index
		// */
		// if (element instanceof GOALCE_GoalAtDepth) {
		// GOALCE_GoalAtDepth goal = (GOALCE_GoalAtDepth) element;
		//
		// /*
		// * If the depth at which the goal need be inserted is higher
		// * than encountered before, extend the ArrayList first to this
		// * depth
		// */
		// for (int i = this.goalAtDepthToIndex.size(); i <= goal.depth; i++) {
		// this.goalAtDepthToIndex.add(new HashMap<GOALCE_GoalAtDepth,
		// Integer>());
		// }
		//
		// /* Add the goal */
		// this.goalAtDepthToIndex.get(goal.depth).put(goal, index);
		// }
		//
		// /*
		// * If the element is a focus, add it to focusAtDepthToIndex with the
		// * new index
		// */
		// if (element instanceof GOALCE_FocusAtDepth) {
		// GOALCE_FocusAtDepth focus = (GOALCE_FocusAtDepth) element;
		//
		// /*
		// * If the depth at which the focus need be inserted is higher
		// * than encountered before, extend the ArrayList first to this
		// * depth
		// */
		// for (int i = this.focusAtDepthToIndex.size(); i <= focus.depth; i++)
		// {
		// this.focusAtDepthToIndex.add(new HashMap<GOALCE_FocusAtDepth,
		// Integer>());
		// }
		//
		// /* Add the focus */
		// this.focusAtDepthToIndex.get(focus.depth).put(focus, index);
		// this.nFocusses++;
		// }
		// this.indexToElement.add(element);
		// }
		return index;
	}

	/**
	 * Checks if the specified element is contained in this conversion universe.
	 *
	 * @param element
	 *            - The element to be (or not to be) contained.
	 * @return <tt>true</tt> if the element is contained; <tt>false</tt>
	 *         otherwise.
	 */
	public boolean contains(GOALConversionElement element) {
		return getIndex(element) != -1;
	}

	// /**
	// * Get the conversion element at the specified index.
	// *
	// * If preassigned element indices are in use, and this index is valid
	// there,
	// * but the element at this index has not occurred yet (so does not exist
	// in
	// * the elements list), then the returned value is null.
	// *
	// * @param index
	// * - The index of the requested element.
	// * @return The requested element.
	// */
	// public GOALConversionElement getAtIndex(int index) {
	// return (this.preassignedElementIndex == null) ?
	// this.indexToElement.get(index)
	// : this.preassignedIndexToElement.get(index);
	// }

	/**
	 * Determine the index of specified element.
	 *
	 * @param elem
	 *            - The element to determine the index of.
	 * @return The index corresponding to the element if has already been
	 *         indexed; Null otherwise.
	 */
	public Integer getIndex(GOALConversionElement elem) {
		return elements.get(elem.toString());

		// if (this.preassignedElementIndex != null) {
		// for (int i = 0; i < this.preassignedElementIndex.size(); i++) {
		// if (this.preassignedElementIndex.get(i).equals(elem.toString())) {
		// return i;
		// }
		// }
		// }
		// if (elem instanceof GOALCE_Belief) {
		// return this.beliefToIndex.containsKey(elem) ?
		// this.beliefToIndex.get(elem) : -1;
		// }
		// if (elem instanceof GOALCE_GoalAtDepth) {
		// GOALCE_GoalAtDepth goal = (GOALCE_GoalAtDepth) elem;
		// if (goal.depth < this.goalAtDepthToIndex.size()) {
		// return this.goalAtDepthToIndex.get(goal.depth).containsKey(elem)
		// ? this.goalAtDepthToIndex.get(goal.depth).get(goal) : -1;
		// }
		// return -1;
		// }
		// if (elem instanceof GOALCE_FocusAtDepth) {
		// GOALCE_FocusAtDepth focus = (GOALCE_FocusAtDepth) elem;
		// if (focus.depth < this.focusAtDepthToIndex.size()) {
		// return this.focusAtDepthToIndex.get(focus.depth).containsKey(elem)
		// ? this.focusAtDepthToIndex.get(focus.depth).get(focus) : -1;
		// }
		// return -1;
		// }
		// return -1;
	}

	// /**
	// * Get the maximum number of focus names, namely by counting all focuses
	// at
	// * all different depths. This is an upper bound to the actual number of
	// * different focuses, as the same focus can occur at different depths.
	// *
	// * @return The number of focus names.
	// */
	// public int nFocusses() {
	// return this.nFocusses;
	// }

	/**
	 * Determine the size of this universe.
	 *
	 * @return The size of this universe.
	 */
	public int size() {
		return elements.size();
		// return (this.preassignedElementIndex == null) ?
		// this.indexToElement.size()
		// : this.preassignedIndexToElement.size();
	}

	@Override
	public String toString() {
		String string = "";
		for (String element : elements.keySet()) {
			string += "\n" + elements.get(element) + ": " + element;
		}
		// if (this.preassignedElementIndex == null) {
		// for (int i = 0; i < this.indexToElement.size(); i++) {
		// string += "\n" + i + ": " + this.indexToElement.get(i).toString();
		// }
		// } else {
		// /**
		// * The preassignedElementIndex array holds any preassigned indices
		// * starting with index=0.
		// */
		// for (int i = 0; i < this.preassignedElementIndex.size(); i++) {
		// string += "\n" + i + ": " + this.preassignedElementIndex.get(i);
		// }
		// /**
		// * The preassignedElementIndex list contains any new elements added
		// * that were not in the preassignedElementIndex array
		// */
		// for (int i = this.preassignedElementIndex.size(); i <
		// this.preassignedIndexToElement.size(); i++) {
		// string += "\n" + i + ": " + this.preassignedIndexToElement.get(i);
		// }
		// }
		return string;
	}

	/**
	 * Add new element with new unique index to the list.
	 * 
	 * @param newElement
	 * @return the index for the new element. 0-based.
	 */
	private int addElement(String newElement) {
		int index = elements.size();
		elements.put(newElement, index);
		return index;
	}

	/**
	 * Appends preassigned indices for elements to the existing list, respecting
	 * the given order of newElements.
	 *
	 * To ensure the list of elements is inserted starting with index 0, this
	 * must be called prior to adding any {@link GOALConversionElement} elements
	 * to this universe.
	 *
	 * @param newElements
	 *            the elements to be inserted into this universe. These must
	 *            have been created with
	 *            {@link GOALConversionUniverse#toString()}
	 */
	public void setPreassignedIndices(List<String> newElements) {
		for (String element : newElements) {
			addElement(element);
		}
		// this.preassignedElementIndex = elements;
		// /**
		// * Also create placeholders for the preassigned elements
		// */
		// if (this.preassignedElementIndex != null) {
		// for (int i = 0; i < this.preassignedElementIndex.size(); i++) {
		// this.preassignedIndexToElement.add(null);
		// }
		// }
	}

	/**
	 * 
	 * @param n
	 *            the element to be fetched.
	 * @return get the element having index n, or null if no such object.
	 * 
	 */
	private String get(int n) {
		for (String element : elements.keySet()) {
			if (elements.get(element) == n) {
				return element;
			}
		}
		return null;
	}

	/**
	 * FIXME let GOALConversionUniverse serialize it's own business...
	 * 
	 * @return A string array with all elements, ordered starting from index 0
	 *         upwards.
	 */
	public List<String> toStringArray() {
		List<String> string = new LinkedList<>();

		for (int n = 0; n < elements.size(); n++) {
			string.add(get(n));
		}

		// if (this.preassignedElementIndex == null) {
		// for (int i = 0; i < this.indexToElement.size(); i++) {
		// string.add(this.indexToElement.get(i).toString());
		// }
		// } else {
		// /**
		// * The preassignedElementIndex array holds any preassigned indices
		// * starting with index=0.
		// */
		// for (int i = 0; i < this.preassignedElementIndex.size(); i++) {
		// string.add(this.preassignedElementIndex.get(i));
		// }
		// /**
		// * The preassignedElementIndex list contains any new elements added
		// * that were not in the preassignedElementIndex array
		// */
		// for (int i = this.preassignedElementIndex.size(); i <
		// this.preassignedIndexToElement.size(); i++) {
		// string.add(this.preassignedElementIndex.get(i).toString());
		// }
		// }
		return string;

	}

	// private int assignIndex() {
	// return (this.preassignedElementIndex == null) ?
	// this.indexToElement.size()
	// : this.preassignedIndexToElement.size();
	// }
}