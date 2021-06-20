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
package goal.tools.test.exceptions;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import goal.tools.errorhandling.exceptions.GOALRuntimeErrorException;
import goal.tools.test.result.ResultFormatter;
import goal.tools.test.result.TestResult;
import krTools.language.Substitution;
import krTools.parser.SourceInfo;
import languageTools.program.test.testcondition.TestCondition;

public class ConditionFailed extends GOALRuntimeErrorException implements TestResult {
	private static final long serialVersionUID = -1899022697249102491L;
	private final TestCondition condition;
	private final String context;
	private final Map<Substitution, Set<Substitution>> evaluations;
	private final SourceInfo info;
	private final boolean timedOut;

	public ConditionFailed(TestCondition condition, String context, Substitution subst, Set<Substitution> evaluation,
			SourceInfo info, boolean timedOut) {
		super("");
		this.condition = condition;
		this.context = context;
		this.evaluations = new LinkedHashMap<>();
		addEvaluation(new AbstractMap.SimpleEntry<>(subst, evaluation));
		this.info = info;
		this.timedOut = timedOut;
	}

	public Map.Entry<Substitution, Set<Substitution>> getFirstEvaluation() {
		return this.evaluations.entrySet().iterator().next();
	}

	public void addEvaluation(Map.Entry<Substitution, Set<Substitution>> evaluation) {
		if (this.evaluations.containsKey(evaluation.getKey())) {
			this.evaluations.get(evaluation.getKey()).addAll(evaluation.getValue());
		} else {
			this.evaluations.put(evaluation.getKey(), evaluation.getValue());
		}
	}

	private String getCondition() {
		return this.condition.toString().replace("\n", " ");
	}

	@Override
	public String getMessage() {
		StringBuilder msg = new StringBuilder();
		msg.append("The condition '").append(getCondition()).append("'");
		if (this.context != null) {
			msg.append(" in ").append(this.context);
		}
		msg.append(" was violated");
		if (this.timedOut) {
			msg.append(" after the timeout occurred");
		}
		if (this.info != null) {
			msg.append(" at ").append(this.info);
		}
		msg.append(":");
		Iterator<Substitution> substs = this.evaluations.keySet().iterator();
		while (substs.hasNext()) {
			Substitution subst = substs.next();
			Set<Substitution> evaluation = this.evaluations.get(subst);
			if (evaluation.isEmpty()) {
				msg.append(" no evaluation");
			} else {
				msg.append(" the evaluation ").append(evaluation);
			}
			msg.append(" applied");
			if (!subst.getVariables().isEmpty()) {
				msg.append(" for ").append(subst);
			}
			if (substs.hasNext()) {
				msg.append("; ");
			}
		}
		msg.append(".");
		return msg.toString();
	}

	@Override
	public <T> T accept(ResultFormatter<T> formatter) {
		return formatter.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = this.timedOut ? 1 : 0;
		result = prime * result + ((this.condition == null) ? 0 : this.condition.hashCode());
		result = prime * result + ((this.context == null) ? 0 : this.context.hashCode());
		result = prime * result + ((this.info == null) ? 0 : this.info.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof ConditionFailed)) {
			return false;
		}
		ConditionFailed other = (ConditionFailed) obj;
		if (this.condition == null) {
			if (other.condition != null) {
				return false;
			}
		} else if (!this.condition.equals(other.condition)) {
			return false;
		}
		if (this.context == null) {
			if (other.context != null) {
				return false;
			}
		} else if (!this.context.equals(other.context)) {
			return false;
		}
		if (this.info == null) {
			if (other.info != null) {
				return false;
			}
		} else if (!this.info.equals(other.info)) {
			return false;
		}
		return (this.timedOut == other.timedOut);
	}
}
