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

import java.util.BitSet;

import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

/**
 * Represents a state in program automaton corresponding to a single-agent GOAL
 * system in the absence of an environment as represented by {@link GOALProg}.
 * <p>
 * A {@link GOALState} only has meaning inside the context of the associated
 * {@link GOALMentalStateConverter}.
 * 
 */
public class GOALState extends BitSet {
	/**
	 * Default serial version ID (needed because this class extends
	 * {@link BitSet}.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The mental state converter associated with this state, i.e. responsible
	 * for creation of this object.
	 */
	private final GOALMentalStateConverter converter;

	/**
	 * Constructs an empty state (i.e. all bits are 0), given the specified
	 * mental state converter.
	 *
	 * @param converter
	 *            The mental state converter responsible for the creation of a
	 *            new {@link GOALState}.
	 */
	public GOALState(GOALMentalStateConverter converter) {
		super();
		this.converter = converter;
	}

	// /**
	// * Constructs a state according to the specified {@link BitSet}, given the
	// * specified mental state converter.
	// *
	// * @param conv
	// * - The mental state converter responsible for the creation of a
	// * new object.
	// * @param q
	// * - Characterization of the state to be constructed.
	// */
	// public GOALState(GOALMentalStateConverter conv, BitSet q) {
	// this(conv);
	// for (int i = q.nextSetBit(0); i >= 0; i = q.nextSetBit(i + 1)) {
	// this.set(i);
	// }
	// }

	// /**
	// * Computes a new program state by "subtracting" the specified state from
	// * this state. That is, bits are compared pairwise as follows. Let
	// * <tt>i</tt>, <tt>j</tt>, and <tt>k</tt> be bits in, respectively, this
	// * state, the specified state, and the resulting state. Then:
	// * <ul>
	// * <li><tt>i</tt>=0 and <tt>j</tt>=0 : <tt>k</tt>=0
	// * <li><tt>i</tt>=0 and <tt>j</tt>=1 : <tt>k</tt>=0
	// * <li><tt>i</tt>=1 and <tt>j</tt>=0 : <tt>k</tt>=1
	// * <li><tt>i</tt>=1 and <tt>j</tt>=1 : <tt>k</tt>=0
	// * </ul>
	// *
	// * @param q
	// * - The state to subtract from this state.
	// * @return The state resulting from subtracting <tt>q</tt> from this
	// state.
	// */
	// public GOALState minus(GOALState q) {
	// /* Compute the intersection of the two states */
	// GOALState intersection = new GOALState(this.converter, this);
	// intersection.and(q);
	//
	// /* Compute the subtraction */
	// GOALState minus = new GOALState(this.converter, this);
	// minus.andNot(intersection);
	// return minus;
	// }

	public String toString(MentalStateWithEvents ms, int indent) {
		try {
			return this.converter.toString(ms, indent);
		} catch (MSTDatabaseException | MSTQueryException e) {
			return e.getMessage();
		}
	}

	// /**
	// * Container for the byte array version of this object, which serves as a
	// * key for Jenkins' hash. At the cost of additional memory, we gain an
	// * increase in speed, as this key need only be computed once.
	// */
	// private byte[] jenkinsKey = null;

	// @Override
	// public byte[] jenkinsKey() {
	// /* If a key has not been defined before, define one */
	// if (this.jenkinsKey == null) {
	// /*
	// * Compute the byte representation of this object (which is a
	// * BitSet)
	// */
	// byte[] key = new byte[length() / 8 + 1];
	// for (int i = 0; i < length(); i++) {
	// if (get(i)) {
	// key[key.length - i / 8 - 1] |= 1 << (i % 8);
	// }
	// }
	//
	// /* Store key */
	// this.jenkinsKey = key;
	// }
	//
	// /* Return */
	// return this.jenkinsKey;
	// }
}
