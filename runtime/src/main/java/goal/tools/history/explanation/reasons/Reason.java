package goal.tools.history.explanation.reasons;

import krTools.parser.SourceInfo;

public abstract class Reason {
	protected final SourceInfo location;
	protected final int state;

	protected Reason(final SourceInfo location, final int state) {
		this.location = location;
		this.state = state;
	}

	@Override
	public abstract String toString();
}
