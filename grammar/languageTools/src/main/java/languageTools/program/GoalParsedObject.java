package languageTools.program;

import krTools.parser.ParsedObject;
import krTools.parser.SourceInfo;

public abstract class GoalParsedObject implements ParsedObject {
	protected final SourceInfo info;

	protected GoalParsedObject(SourceInfo info) {
		this.info = info;
	}

	@Override
	public final SourceInfo getSourceInfo() {
		return this.info;
	}

	@Override
	public abstract String toString();

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);
}
