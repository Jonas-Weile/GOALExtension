package goal.tools.eclipse;

import krTools.parser.SourceInfo;

class StackInfo {
	private final String name;
	private final SourceInfo source;

	public StackInfo(String name, SourceInfo source) {
		this.name = name;
		this.source = source;
	}

	public String getName() {
		return this.name;
	}

	public SourceInfo getSource() {
		return this.source;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.source == null) ? 0 : this.source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof StackInfo)) {
			return false;
		}
		StackInfo other = (StackInfo) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		} else if (this.source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!this.source.equals(other.source)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (this.name != null) {
			builder.append(this.name).append(" ");
		}
		if (this.source != null) {
			builder.append("at ").append(this.source);
		}
		return builder.toString();
	}
}
