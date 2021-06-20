/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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

package krTools.dependency;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.Expression;

/**
 * A node is used to store expressions of a KR language (more precisely, the
 * signature of expressions represented) that the agent uses in a
 * {@link DependencyGraph}.
 *
 * @param <T>
 *            The node type
 */
public class Node<T extends Expression> {
	/**
	 * Signature of all the expressions stored in this node.
	 */
	private final String signature;
	/**
	 * The KR language definitions stored in this {@link Node}. All these
	 * expressions should have the same signature.
	 */
	private final List<T> definitions = new LinkedList<>();
	/**
	 * The KR language queries stored in this {@link Node}. All these
	 * expressions should have the same signature.
	 */
	private final List<T> queries = new LinkedList<>();

	/**
	 * Used to check for cycles when computing all basic dependencies, see
	 * {@link #getBasicDependencies()}.
	 */
	private boolean visited = false;
	/**
	 * The list of expression (nodes) that this expression('s evaluation)
	 * depends on.
	 */
	private final List<Node<T>> dependencies = new LinkedList<>();

	/**
	 * Constructor of a Node with a given signature.
	 *
	 * @param signature
	 *            The node signature string.
	 */
	public Node(String signature) {
		this.signature = signature;
	}

	/**
	 * Adds an expression to the list of {@link #definitions} stored in this
	 * node. All occurrences of expressions with the same signature should be
	 * stored in one and the same node.
	 *
	 * @param expression
	 *            The expression that is added to the list of definitions.
	 */
	public void addDefinition(T expression) {
		this.definitions.add(expression);
	}

	/**
	 * Adds an expression to the list of {@link #queries} stored in this node.
	 * All occurrences of expressions with the same signature should be stored
	 * in one and the same node.
	 *
	 * @param expression
	 *            The expression that is added to the list of queries.
	 */
	public void addQuery(T expression) {
		this.queries.add(expression);
	}

	/**
	 * Getter for the signature of this Node.
	 *
	 * @return the signature string
	 */
	public String getSignature() {
		return this.signature;
	}

	/**
	 * Getter for the definitions of this Node.
	 *
	 * @return the list of definitions.
	 */
	public List<T> getDefinitions() {
		return this.definitions;
	}

	/**
	 * Getter for the queries of this Node.
	 *
	 * @return the list of queries
	 */
	public List<T> getQueries() {
		return this.queries;
	}

	/**
	 * Adds a node to this {@link Node}'s dependency list. To avoid cyclic
	 * dependencies, this node itself cannot be added to it's own dependency
	 * list.
	 *
	 * @param node
	 *            The node that is added to this node's dependency list.
	 */
	public void addDependency(Node<T> node) {
		// Do not add the same node twice.
		for (Node<T> dependency : this.dependencies) {
			if (dependency.signature.equals(node.getSignature())) {
				return;
			}
		}
		this.dependencies.add(node);
	}

	/**
	 * Getter for the dependencies of this Node.
	 *
	 * @return the dependencies as a list of nodes
	 */
	public List<Node<T>> getDependencies() {
		return this.dependencies;
	}

	/**
	 * Checks if the node is defined
	 *
	 * @return boolean true if the node's definitions is not empty
	 */
	public boolean isDefined() {
		return !this.definitions.isEmpty();
	}

	/**
	 * Checks if the node is queried.
	 *
	 * @return boolean true if the node's queries is not empty
	 */
	public boolean isQueried() {
		return !this.queries.isEmpty();
	}

	/**
	 * Checks if the node is basic (has no dependencies)
	 *
	 * @return boolean true if the node is basic
	 */
	public boolean isBasic() {
		return this.dependencies.isEmpty();
	}

	/**
	 * Returns the signatures for the basic dependencies of this node's
	 * expression. That is, those expressions on which the evaluation of the
	 * expression stored in this node depends that not depend themselves on
	 * other expressions, or the expression stored itself.
	 *
	 * @return The signatures.
	 */
	public Set<String> getBasicDependencies() {
		Set<String> signatures = new HashSet<>();
		// Check whether we have been here already; if so, immediately return,
		// in order to avoid cycles.
		if (this.visited) {
			return signatures;
		} else {
			this.visited = true;
		}
		// Add the signature of this node if it is basic, i.e. the expression's
		// evaluation does not depend on other expressions.
		if (this.isBasic()) {
			signatures.add(this.getSignature());
		} else {
			for (Node<T> node : this.dependencies) {
				signatures.addAll(node.getBasicDependencies());
			}
		}
		this.visited = false;
		return signatures;
	}

	/**
	 * Returns a string representation of this {@link Node}, with on the first
	 * line the expressions stored in this node and on the second line the list
	 * of associated queries.
	 *
	 * @return A string representation of this node.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Definitions:");
		builder.append(this.definitions.toString());
		builder.append("\n Queries:");
		builder.append(this.queries.toString());
		builder.append("\n Dependencies:");
		builder.append(this.dependencies.toString());
		return builder.toString();
	}
}
