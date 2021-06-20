package languageTools.errors;

import java.io.Serializable;

import krTools.parser.SourceInfo;

public abstract class Message implements Serializable, Comparable<Message> {

	private static final long serialVersionUID = 6396169798566643352L;
	private SourceInfo source = null;

	public interface ValidatorMessageType {
		public String toReadableString(String... args);
	}

	protected final ValidatorMessageType type;
	protected String[] args;

	public Message(ValidatorMessageType type, SourceInfo source, String... args) {
		this.type = type;
		this.source = source;
		this.args = args;
	}

	public ValidatorMessageType getType() {
		return this.type;
	}

	public SourceInfo getSource() {
		return this.source;
	}

	public void setSource(SourceInfo source) {
		this.source = source;
	}

	public String[] getArguments() {
		return this.args;
	}

	public void setArguments(String... args) {
		this.args = args;
	}

	public String toShortString() {
		final String[] shortargs = new String[this.args.length];
		for (int i = 0; i < this.args.length; i++) {
			final String arg = this.args[i];
			// if (arg.length() > 1500) {
			// shortargs[i] = arg.substring(0, 1500) + "...";
			// } else {
			shortargs[i] = arg;
			// }
		}
		return this.type.toReadableString(shortargs);
	}

	@Override
	public abstract String toString();

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof Message)) {
			return false;
		} else {
			Message other = (Message) obj;
			if (this.source == null) {
				if (other.getSource() != null) {
					return false;
				}
			} else if (!this.source.equals(other.getSource())) {
				return false;
			}
			if (this.type == null) {
				if (other.getType() != null) {
					return false;
				}
			} else if (!this.type.equals(other.getType())) {
				return false;
			}
			return true;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.source == null) ? 0 : this.source.hashCode());
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
		return result;
	}

	@Override
	public int compareTo(Message other) {
		if (equals(other)) {
			return 0;
		} else if (other.getSource() == null) {
			return (this.source == null) ? 0 : -1;
		} else if (this.source == null) {
			return 1;
		} else {
			return before(this.source, other.getSource()) ? -1 : 1;
		}
	}

	/**
	 * @param info1
	 *            A source info object.
	 * @param info2
	 *            A source info object.
	 * @return {@code true} if source position of info1 object occurs before
	 *         position of info2 object.
	 */
	private static boolean before(SourceInfo info1, SourceInfo info2) {
		boolean source = info1.getSource() != null && info2.getSource() != null
				&& (info1.getSource().compareTo(info2.getSource()) < 0);
		boolean sourceEqual = info1.getSource() != null && info2.getSource() != null
				&& (info1.getSource().compareTo(info2.getSource()) == 0);
		boolean lineNr = sourceEqual && (info1.getLineNumber() < info2.getLineNumber());
		boolean lineNrEqual = (info1.getLineNumber() == info2.getLineNumber());
		boolean position = sourceEqual && lineNrEqual && (info1.getCharacterPosition() < info2.getCharacterPosition());
		return source || lineNr || position;
	}

}
