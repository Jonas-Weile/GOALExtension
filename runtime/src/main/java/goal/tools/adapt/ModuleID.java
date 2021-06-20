package goal.tools.adapt;

import java.io.Serializable;

/**
 * unique ID for each learned module. String based to avoid dependencies of
 * learner on core functionality.
 * 
 * @author W.Pasman
 *
 */
@SuppressWarnings("serial")
public class ModuleID implements Serializable {
	private final String signature;

	/**
	 * create ModuleID based on signature.
	 * 
	 * @param signature
	 *            signature of module, eg build/1.
	 */
	public ModuleID(String signature) {
		if (signature == null) {
			throw new NullPointerException("null signature");
		}
		if (!(signature.contains("/"))) {
			throw new IllegalArgumentException("module signature must have arity");
		}
		this.signature = signature;
	}

	@Override
	public int hashCode() {
		return signature.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ModuleID)) {
			return false;
		}
		return signature.equals(((ModuleID) obj).signature);
	}

	@Override
	public String toString() {
		return signature;
	}

	/**
	 * 
	 * @return a filename based on the signature.
	 */
	public String makeFileName() {
		return signature.replaceAll("/", ".");
	}
}
