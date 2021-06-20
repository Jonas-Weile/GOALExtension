package swiprolog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jpl7.JPL;

import net.harawata.appdirs.AppDirsFactory;

/**
 * call init() once to install the libraries and prepare SWI for use.
 *
 * @author W.Pasman 1dec2014
 */
public final class SwiInstaller {
	private final static SupportedSystem system = SupportedSystem.getSystem();
	private final static Logger logger = Logger.getLogger("KRLogger");
	private static boolean initialized = false;
	private static String override = null;
	private static File SwiPath;

	/**
	 * This is a utility class. Just call init().
	 */
	private SwiInstaller() {
	}

	/**
	 * Overrides the installation directory. Useful if applicationdata can not be
	 * used by the application.
	 *
	 * @param dir the new target directory.
	 */
	public static void overrideDirectory(final String dir) {
		override = dir;
	}

	/**
	 * see {@link #init(boolean)} where boolean=false.
	 */
	public static void init() {
		init(false);
	}

	/**
	 * initialize SWI prolog for use. Unzips dlls and connects them to the system.
	 * This static function needs to be called once, to get SWI hooked up to the
	 * java system.
	 *
	 * This call will unzip required system dynamic link libraries to a temp folder,
	 * pre-load them, and set the paths such that SWI can find its files.
	 *
	 * The temp folder will be removed automatically if the JVM exits normally.
	 *
	 * @throws RuntimeException if initialization failed (see nested exception).
	 */
	public static synchronized void init(final boolean force) {
		if (initialized && !force) {
			return;
		}

		// Unpack everything and load the relevant dependencies for JPL.
		unzipSWI(force);
		loadDependencies();

		// Let JPL know where it's library is located.
		JPL.setNativeLibraryDir(SwiPath.getAbsolutePath());
		// Don't Tell Me Mode needs to be false as it ensures that variables
		// with initial '_' are treated as regular variables.
		JPL.setDTMMode(false);
		// Let JPL know which SWI_HOME_DIR we're using; this negates the need
		// for a SWI_HOME_DIR environment var. Pass some required options too.
		JPL.init(new String[] { "pl", "--home=" + SwiPath, "--quiet", "--nosignals", "--nodebug" });
		new org.jpl7.Query("set_prolog_flag(debug_on_error,false).").allSolutions();

		// Finished
		initialized = true;
		// logger.log(Level.INFO, "SWI Prolog " + Prolog.get_c_lib_version());
	}

	public static void unzipSWI(final boolean force) throws RuntimeException {
		try {
			final File basedir = unzip(system + ".zip", force);
			SwiPath = new File(basedir, system.toString());
		} catch (final IOException e) {
			throw new RuntimeException("failed to install SWI: ", e);
		}
	}

	/**
	 * Loads all dynamic libraries for the current operating system.
	 */
	private static void loadDependencies() {
		// Dirty system-dependent stuff...
		switch (system) {
		case linux:
			load("libtinfo.so.6");
			load("libgmp.so.10");
			load("libswipl.so.8.3.15");
			break;
		case mac:
			load("libreadline.8.dylib");
			load("libgcc_s.1.dylib");
			load("libgmp.10.dylib");
			load("libswipl.8.3.15.dylib");
			break;
		case win32:
			load("libwinpthread-1.dll");
			load("libgcc_s_sjlj-1.dll");
			load("libgmp-10.dll");
			load("zlib1.dll");
			load("libswipl.dll");
			break;
		case win64:
			load("libwinpthread-1.dll");
			load("libgcc_s_seh-1.dll");
			load("libgmp-10.dll");
			load("zlib1.dll");
			load("libswipl.dll");
			break;
		}
	}

	/**
	 * pre-loads a system dynamic library.
	 *
	 * @param libname
	 */
	private static void load(final String libname) {
		System.load(new File(SwiPath, libname).getAbsolutePath());
	}

	/**
	 * Unzip a given zip file
	 *
	 * @param zipfilename
	 * @return temp directory where swi files are contained.
	 * @throws IOException
	 */
	private static File unzip(final String zipfilename, final boolean force) throws IOException {
		String appDataDir = null;
		try {
			appDataDir = AppDirsFactory.getInstance().getUserDataDir("swilibs", getVersion(), "GOAL");
		} catch (final Throwable e) {
			appDataDir = System.getProperty("java.io.tmpdir") + File.separator + "swilibs" + getVersion();
		}
		final Path path = (override == null) ? Paths.get(appDataDir) : Paths.get(override);
		final File base = path.toFile();
		if (base.exists()) {
			if (force) {
				deleteFolder(base);
			} else {
				return base;
			}
		}

		logger.log(Level.INFO, "unzipping SWI Prolog libraries (" + zipfilename + ") to " + base);
		base.mkdir();

		final InputStream fis = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("swiprolog/" + zipfilename);
		final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry = null;
		while ((entry = zis.getNextEntry()) != null) {
			final File fileInDir = new File(base, entry.getName());
			if (entry.isDirectory()) {
				fileInDir.mkdirs();
			} else if (!fileInDir.canRead()) {
				final int size = (int) entry.getSize();
				final byte[] bytes = new byte[size];
				int read = 0;
				while (read < size) {
					read += zis.read(bytes, read, (size - read));
				}
				Files.write(fileInDir.toPath(), bytes, StandardOpenOption.CREATE);
			}
			zis.closeEntry();
		}
		zis.close();
		fis.close();

		return base;
	}

	/**
	 * @return a unique number for the current source code, that changes when the
	 *         GOAL version changes. the maven version number of this SWI installer,
	 *         or the modification date of this class if no maven info is
	 *         available..
	 * @throws IOException
	 */
	private static String getVersion() throws IOException {
		String version = null;
		try { // try to load from maven properties first
			final Properties p = new Properties();
			final InputStream is = SwiInstaller.class.getResourceAsStream(
					"/META-INF/maven/org.bitbucket.goalhub.krTools.krLanguages/swiPrologEnabler/pom.properties");
			if (is != null) {
				p.load(is);
				version = p.getProperty("version", "");
			}
		} catch (final Exception ignore) {
		}
		if (version == null || version.isEmpty()) { // fallback to using Java API
			final String srcpath1 = SwiInstaller.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			final String srcpath = URLDecoder.decode(srcpath1, "UTF-8");
			final File srcfile = new File(srcpath);
			version = Long.toString(srcfile.lastModified());
		}

		return version;
	}

	private static void deleteFolder(final File folder) {
		final File[] files = folder.listFiles();
		if (files != null) {
			for (final File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}
}