package mentalState;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import krTools.language.DatabaseFormula;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;

/**
 * Result contains the changes on a database after it was modified: which
 * formulas were added and deleted, what is the new focus.
 *
 */
abstract public class Result {
	private final BASETYPE base;
	private String focus;
	private final List<DatabaseFormula> added;
	private final List<DatabaseFormula> removed;

	protected Result(BASETYPE base, String focus) {
		this.base = base;
		this.focus = focus;
		this.added = new LinkedList<>();
		this.removed = new LinkedList<>();
	}

	public void added(List<DatabaseFormula> dbfs) {
		this.added.addAll(dbfs);
	}

	public void added(DatabaseFormula dbf) {
		this.added.add(dbf);
	}

	public void removed(List<DatabaseFormula> dbfs) {
		this.removed.addAll(dbfs);
	}

	public void removed(DatabaseFormula dbf) {
		this.removed.add(dbf);
	}

	public BASETYPE getBaseType() {
		return this.base;
	}

	public String getFocus() {
		return this.focus;
	}

	public List<DatabaseFormula> getAdded() {
		return this.added;
	}

	public List<DatabaseFormula> getRemoved() {
		return this.removed;
	}

	public void merge(Result other) {
		if (getBaseType() == other.getBaseType() && (getFocus() == null || getFocus().equals(other.getFocus()))) {
			this.focus = other.getFocus();
			added(other.getAdded());
			removed(other.getRemoved());
		} else {
			throw new RuntimeException("incompatible: " + getBaseType() + "," + other.getBaseType() + " and "
					+ getFocus() + "," + other.getFocus() + ".");
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (!this.added.isEmpty()) {
			String added = (this.base == BASETYPE.GOALBASE) ? "adopted" : "inserted";
			builder.append(added).append(" ").append(listToString(this.added)).append(" into ");
		}
		if (!this.removed.isEmpty()) {
			if (builder.length() > 0) {
				builder.append("and ");
			}
			String removed = (this.base == BASETYPE.GOALBASE) ? "dropped" : "deleted";
			builder.append(removed).append(" ").append(listToString(this.removed)).append(" from ");
		}
		if (builder.length() < 1) {
			builder.append("empty update on ");
		}
		String focus = this.focus.startsWith("(") ? this.focus.substring(1, this.focus.length() - 1) : this.focus;
		builder.append(this.base).append(" ").append(focus).append(".");
		return builder.toString();
	}

	protected abstract Translator getTranslator();

	protected char[] listToString(List<DatabaseFormula> formulas) {
		List<?> list;
		switch (this.base) {
		case PERCEPTBASE:
			Translator translator1 = getTranslator();
			if (translator1 == null) {
				list = formulas;
			} else {
				List<Object> list1 = new ArrayList<>(formulas.size());
				for (DatabaseFormula formula : formulas) {
					try {
						list1.add(translator1.convertPercept(formula).toProlog());
					} catch (MSTTranslationException e) {
						list1.add(e.getMessage());
					}
				}
				list = list1;
			}
			break;
		case MESSAGEBASE:
			Translator translator2 = getTranslator();
			if (translator2 == null) {
				list = formulas;
			} else {
				List<Object> list2 = new ArrayList<>(formulas.size());
				for (DatabaseFormula formula : formulas) {
					try {
						list2.add(translator2.convertMessage(formula).toString());
					} catch (MSTTranslationException e) {
						list2.add(e.getMessage());
					}
				}
				list = list2;
			}
			break;
		default:
			list = formulas;
			break;
		}
		// replace [..] with '..'
		final char[] removedlist = list.toString().toCharArray();
		removedlist[0] = '\'';
		removedlist[removedlist.length - 1] = removedlist[0];
		return removedlist;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.base == null) ? 0 : this.base.hashCode());
		result = prime * result + ((this.focus == null) ? 0 : this.focus.hashCode());
		result = prime * result + ((this.added == null) ? 0 : this.added.hashCode());
		result = prime * result + ((this.removed == null) ? 0 : this.removed.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof Result)) {
			return false;
		}
		Result other = (Result) obj;
		if (this.base != other.base) {
			return false;
		}
		if (this.focus == null) {
			if (other.focus != null) {
				return false;
			}
		} else if (!this.focus.equals(other.focus)) {
			return false;
		}
		if (this.added == null) {
			if (other.added != null) {
				return false;
			}
		} else if (!this.added.equals(other.added)) {
			return false;
		}
		if (this.removed == null) {
			if (other.removed != null) {
				return false;
			}
		} else if (!this.removed.equals(other.removed)) {
			return false;
		}
		return true;
	}
}
