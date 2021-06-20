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

package languageTools.program.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.program.GoalParsedObject;
import languageTools.program.test.testcondition.TestCondition;

public class ModuleTest extends GoalParsedObject {
	/**
	 * Name of the module under test.
	 */
	private final String moduleName;
	/**
	 * Arguments of the module under test.
	 */
	private final List<Term> arguments;
	/**
	 * The pre-condition
	 */
	private TestMentalStateCondition pre;
	/**
	 * The (temporal) in-conditions
	 */
	private List<TestCondition> in;
	/**
	 * The post-condition
	 */
	private TestMentalStateCondition post;

	/**
	 * @param moduleName
	 *            name of the module
	 */
	public ModuleTest(String moduleName, List<Term> arguments, SourceInfo info) {
		super(info);
		this.moduleName = moduleName;
		this.arguments = arguments;
	}

	public void setPre(TestMentalStateCondition pre) {
		this.pre = pre;
	}

	public void setIn(List<TestCondition> in) {
		this.in = in;
	}

	public void setPost(TestMentalStateCondition post) {
		this.post = post;
	}

	/**
	 * @return name of the module to test
	 */
	public String getModuleName() {
		return this.moduleName;
	}

	/**
	 * @return arguments of the module to test
	 */
	public List<Term> getModuleArguments() {
		return Collections.unmodifiableList(this.arguments);
	}

	/**
	 * @return signature of the module to test
	 */
	public String getModuleSignature() {
		return this.moduleName + "/" + this.arguments.size();
	}

	/**
	 * @return the pre-condition (if any; null otherwise).
	 */
	public TestMentalStateCondition getPre() {
		return this.pre;
	}

	/**
	 * @return the in-conditions (possibly empty).
	 */
	public List<TestCondition> getIn() {
		return (this.in == null) ? new ArrayList<>(0) : this.in;
	}

	/**
	 * @return the post-condition (if any; null otherwise).
	 */
	public TestMentalStateCondition getPost() {
		return this.post;
	}

	@Override
	public String toString() {
		return "ModuleTest [moduleName=" + this.moduleName + ", pre=" + this.pre + ", in=" + this.in + ", post="
				+ this.post + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.moduleName == null) ? 0 : this.moduleName.hashCode());
		result = prime * result + ((this.pre == null) ? 0 : this.pre.hashCode());
		result = prime * result + ((this.post == null) ? 0 : this.post.hashCode());
		result = prime * result + ((this.in == null) ? 0 : this.in.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof ModuleTest)) {
			return false;
		}
		ModuleTest other = (ModuleTest) obj;
		if (this.moduleName == null) {
			if (other.moduleName != null) {
				return false;
			}
		} else if (!this.moduleName.equals(other.moduleName)) {
			return false;
		}
		if (this.pre == null) {
			if (other.pre != null) {
				return false;
			}
		} else if (!this.pre.equals(other.pre)) {
			return false;
		}
		if (this.post == null) {
			if (other.post != null) {
				return false;
			}
		} else if (!this.post.equals(other.post)) {
			return false;
		}
		if (this.in == null) {
			if (other.in != null) {
				return false;
			}
		} else if (!this.in.equals(other.in)) {
			return false;
		}
		return true;
	}
}