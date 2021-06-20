package swiprolog;

/**
 * Determines the system we are running on.
 *
 * @author W.Pasman 1dec14
 */
enum SupportedSystem {
	/* ! these are matching the filenames in lib directory */
	win32, win64, mac, linux;

	public static SupportedSystem getSystem() {
		final String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			if (System.getProperty("os.arch").contains("64")) {
				return win64;
			} else {
				return win32;
			}
		} else if (os.contains("mac")) {
			return mac;
		} else {
			return linux;
		}
	}
}