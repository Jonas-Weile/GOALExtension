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

package languageTools.program.mas;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import krTools.parser.SourceInfo;
import languageTools.program.GoalParsedObject;
import languageTools.utils.Extension;
import languageTools.utils.ReferenceResolver;

/**
 * Container for use clauses specified in the definition section of an agent in
 * a MAS file, in a module file, or an action specification file. Stores the
 * original reference, the resolved reference (i.e. one or more files), and the
 * use case type.
 */
public class UseClause extends GoalParsedObject {
	/**
	 * Reference of use clause.
	 */
	private final String reference;
	/**
	 * Use case of use clause.
	 */
	private UseCase useCase;

	/**
	 * Path to directory where source files which contains this use clause is
	 * located. This path is used to resolve references and locate the associated
	 * file(s).
	 */
	private final String relativePath;
	/**
	 * Resolved references, i.e. one more files the reference can be resolved.
	 */
	private List<URI> resolvedReferences = null;

	/**
	 * Creates a use clause.
	 *
	 * @param reference    A reference.
	 * @param useCase      A use case. Can be {@code null}; in that case, use case
	 *                     is assumed to be either an (auxiliary) module or an
	 *                     action specification.
	 * @param relativePath A relative path (path to directory where source file
	 *                     which contains the use clause is located).
	 */
	public UseClause(String reference, UseCase useCase, String relativePath, SourceInfo info) {
		super(info);
		this.reference = reference;
		this.useCase = useCase;
		this.relativePath = relativePath;
	}

	/**
	 * @return The reference of this use clause.
	 */
	public String getReference() {
		return this.reference;
	}

	/**
	 * Resolves reference in use clause to some generic URI.
	 *
	 * @return URI if reference could be resolved, {@code null} otherwise.
	 */
	public List<URI> getResolvedUriReference() {
		if (this.resolvedReferences == null) {
			resolveReference();
		}
		return this.resolvedReferences;
	}

	/**
	 * Resolves reference in use clause to a file.
	 *
	 * @return File if reference could be resolved, {@code null} otherwise.
	 */
	public List<File> getResolvedReference() {
		List<URI> references = getResolvedUriReference();
		List<File> returned = new LinkedList<>();
		for (URI reference : references) {
			if (isLocalFile(reference)) {
				try {
					File uri = new File(reference);
					returned.add(new File(uri.getCanonicalPath()));
				} catch (IOException e) {
					// FIXME
				}
			}
		}
		return returned;
	}

	/**
	 * @return The use case of this use clause.
	 */
	public UseCase getUseCase() {
		return this.useCase;
	}

	/**
	 * @return {@code true} if use case is for KR.
	 */
	public boolean hasKRUseCase() {
		if (this.useCase != null) {
			switch (this.useCase) {
			case KNOWLEDGE:
			case BELIEFS:
			case GOALS:
				return true;
			default:
			}
		}
		return false;
	}

	/**
	 * Resolves the reference, and, if successful, stores the file found.
	 *
	 * @return A list of files that matches the reference.
	 */
	public List<URI> resolveReference() {
		List<URI> files = new LinkedList<>();
		if (this.useCase == null) { // Assume reference is to module or action
			// specification file.
			for (File f : ReferenceResolver.resolveReference(this.reference, Extension.MOD2G, this.relativePath)) {
				files.add(f.toURI());
			}
			for (File f : ReferenceResolver.resolveReference(this.reference, Extension.ACT2G, this.relativePath)) {
				files.add(f.toURI());
			}
			for (File f : ReferenceResolver.resolveReference(this.reference, Extension.MAS2G, this.relativePath)) {
				files.add(f.toURI());
			}
		} else {
			switch (this.useCase) {
			case MODULE:
			case INIT:
			case EVENT:
			case MAIN:
			case SHUTDOWN:
				// Process reference for module indicators.
				for (File f : ReferenceResolver.resolveReference(this.reference, Extension.MOD2G, this.relativePath)) {
					files.add(f.toURI());
				}
				break;
			case ACTIONSPEC:
				// Process reference for actionspec indicators.
				for (File f : ReferenceResolver.resolveReference(this.reference, Extension.ACT2G, this.relativePath)) {
					files.add(f.toURI());
				}
				break;
			case PLANNER:
				// Process reference for plan indicators.
				for (File f : ReferenceResolver.resolveReference(this.reference, Extension.PLAN2G, this.relativePath)) {
					files.add(f.toURI());
				}
				break;
			case MAS:
				// Process reference for mas indicators (from test2g).
				for (File f : ReferenceResolver.resolveReference(this.reference, Extension.MAS2G, this.relativePath)) {
					files.add(f.toURI());
				}
				break;
			case KNOWLEDGE:
			case BELIEFS:
			case GOALS:
				// Process reference for KR files.
				List<File> kr = ReferenceResolver.resolveKRReference(this.reference, this.relativePath);
				for (File krFile : kr) {
					files.add(krFile.toURI());
				}
				if (files.isEmpty()) {
					try {
						URI uri = new URI(this.reference);
						if (uri.isAbsolute()) {
							files.add(uri);
						}
					} catch (URISyntaxException | NullPointerException e) {
						// Ignored; empty files list will be reported.
					}
				}
				break;
			default:
				break;
			}
		}

		this.resolvedReferences = files;

		if (this.useCase == null) {
			for (final URI i : files) {
				if (isLocalFile(i)) {
					File local = new File(i);
					if (Extension.getFileExtension(local) == Extension.MOD2G) {
						this.useCase = UseCase.MODULE;
						break;
					} else if (Extension.getFileExtension(local) == Extension.ACT2G) {
						this.useCase = UseCase.ACTIONSPEC;
						break;
					} else if (Extension.getFileExtension(local) == Extension.PLAN2G) {
						this.useCase = UseCase.PLANNER;
						break;
					} else if (Extension.getFileExtension(local) == Extension.MAS2G) {
						this.useCase = UseCase.MAS;
						break;
					}
				}
			}
		}

		return files;
	}

	private static boolean isLocalFile(URI uri) {
		String scheme = uri.getScheme();
		return scheme != null && scheme.equalsIgnoreCase("file") && !hasHost(uri);
	}

	private static boolean hasHost(URI uri) {
		String host = uri.getHost();
		return host != null && !host.isEmpty();
	}

	@Override
	public String toString() {
		return "<Use clause: " + this.reference + ", " + this.useCase + ">\n";
	}

	// -------------------------------------------------------------
	// Use Case enum class
	// -------------------------------------------------------------

	/**
	 * Enum for use cases of references of use clauses.
	 */
	public enum UseCase {
		/**
		 * Use case for (static) knowledge base of agent.
		 */
		KNOWLEDGE("knowledge"),
		/**
		 * Use case for (dynamic) belief base of agent.
		 */
		BELIEFS("beliefs"),
		/**
		 * Use case for goal base of agent.
		 */
		GOALS("goals"),
		/**
		 * Use case for module that initializes agent.
		 */
		INIT("init module"),
		/**
		 * Use case for module that processes events received by agent.
		 */
		EVENT("event module"),
		/**
		 * Use case for module for main decision making of agent.
		 */
		MAIN("main module"),
		/**
		 * Use case for module when shutting down the agent.
		 */
		SHUTDOWN("shutdown module"),
		/**
		 * Use case for auxiliary module.
		 */
		MODULE("module"),
		/**
		 * Use case for action specifications.
		 */
		ACTIONSPEC("action specification"),
		/**
		 * Use case for action specifications.
		 */
		PLANNER("plan specification"),
		/**
		 * Use case for referencing a MAS in a test.
		 */
		MAS("multi-agent system");

		private String typeLabel;

		private UseCase(String typeLabel) {
			this.typeLabel = typeLabel;
		}

		@Override
		public String toString() {
			return this.typeLabel;
		}

		/**
		 * @param usecase A string representing a use case type.
		 * @return The use case type, or {@code null} if the string cannot be resolved
		 *         into a use case type.
		 */
		public static UseCase getUseCase(String usecase) {
			try {
				return UseCase.valueOf(usecase.toUpperCase());
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	@Override
	public int hashCode() {
		if (this.resolvedReferences == null) {
			resolveReference();
		}
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.useCase == null) ? 0 : this.useCase.hashCode());
		result = prime * result + ((this.resolvedReferences == null) ? 0 : this.resolvedReferences.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.resolvedReferences == null) {
			resolveReference();
		}
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof UseClause)) {
			return false;
		}
		UseClause other = (UseClause) obj;
		// if (this.useCase != other.useCase) {
		// return false;
		// }
		if (this.resolvedReferences == null) {
			if (other.resolvedReferences != null) {
				return false;
			}
		} else if (!this.resolvedReferences.equals(other.resolvedReferences)) {
			return false;
		}
		return true;
	}

}
